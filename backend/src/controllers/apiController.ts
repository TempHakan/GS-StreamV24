import { Request, Response } from 'express';
import axios from 'axios';
import { M3uParser } from '../services/m3uParser';
import { XtreamClient } from '../services/xtreamClient';
import { StbPortalService } from '../services/stbPortalService';
import { EpgService } from '../services/epgService';
import { StreamProxy } from '../services/streamProxy';
import { VersionInfo } from '../models/types';

// Bellekte saklanan güncel versiyon durumu (VK mekanizması)
let currentVersionState: VersionInfo = {
  currentVersion: 'VK05',
  latestVersion: 'VK06',
  versionCode: 6,
  downloadUrl: 'https://github.com/vyslkrc/streamflow-iptv/raw/main/apk/StreamFlow-IPTV-v2.4.0-VK06.apk',
  changelog: [
    'Ekran üzerinde dikey kaydırma ile Ses (sağ) ve Parlaklık (sol) jest kontrolü',
    'VOD videoları için akıcı ilerleme çubuğu (Scrubbing Seekbar), +10s / -10s ileri/geri sarma',
    'M3U listelerindeki Film ve Dizi içeriklerini otomatik algılayıp VOD kütüphanesine ayırma',
    'GitHub Private (Gizli) depolar için Token desteği ve tarayıcıdan indirme köprüsü',
    'Yeni v2.4.0-VK06 sürümü derlendi'
  ],
  mandatory: false,
  releaseDate: new Date().toISOString().split('T')[0]
};

export class ApiController {
  // M3U Ayrıştırma Uç Noktası
  public static async parseM3u(req: Request, res: Response): Promise<void> {
    const { url, raw } = req.body;
    try {
      let content = raw;
      if (url) {
        const response = await axios.get(url, {
          timeout: 20000,
          headers: { 'User-Agent': 'StreamFlow IPTV Engine/2.4.0' }
        });
        content = response.data;
      }
      if (!content) {
        res.status(400).json({ error: 'M3U içeriği veya URL belirtilmedi.' });
        return;
      }
      const channels = M3uParser.parse(content);
      res.json({ total: channels.length, channels });
    } catch (error: any) {
      res.status(500).json({ error: 'M3U ayrıştırılamadı', details: error.message });
    }
  }

  // Xtream Codes Kanalları
  public static async getXtreamChannels(req: Request, res: Response): Promise<void> {
    const { host, username, password, action, category_id } = req.query;
    if (!host || !username || !password) {
      res.status(400).json({ error: 'host, username ve password parametreleri zorunludur.' });
      return;
    }

    try {
      const client = new XtreamClient(String(host), String(username), String(password));
      if (action === 'get_live_categories') {
        const categories = await client.getLiveCategories();
        res.json(categories);
      } else if (action === 'get_vod_streams') {
        const vods = await client.getVodStreams(category_id ? String(category_id) : undefined);
        res.json(vods);
      } else {
        const channels = await client.getLiveStreams(category_id ? String(category_id) : undefined);
        res.json(channels);
      }
    } catch (error: any) {
      res.status(500).json({ error: 'Xtream bağlantı hatası', details: error.message });
    }
  }

  // STB / MAC Portal Kanalları
  public static async getStbChannels(req: Request, res: Response): Promise<void> {
    const { portalUrl, mac } = req.query;
    if (!portalUrl || !mac) {
      res.status(400).json({ error: 'portalUrl ve mac parametreleri zorunludur.' });
      return;
    }

    try {
      const stbService = new StbPortalService(String(portalUrl), String(mac));
      const channels = await stbService.getAllChannels();
      res.json({ total: channels.length, channels });
    } catch (error: any) {
      res.status(500).json({ error: 'STB portal bağlantı hatası', details: error.message });
    }
  }

  // EPG Ayrıştırma
  public static async fetchEpg(req: Request, res: Response): Promise<void> {
    const epgUrl = req.query.url as string;
    if (!epgUrl) {
      res.status(400).json({ error: 'EPG URL parametresi eksik' });
      return;
    }
    try {
      const epgMap = await EpgService.fetchAndParse(epgUrl);
      const output: Record<string, any> = {};
      epgMap.forEach((v, k) => { output[k] = v; });
      res.json({ totalChannelsWithEpg: epgMap.size, data: output });
    } catch (error: any) {
      res.status(500).json({ error: 'EPG indirilemedi', details: error.message });
    }
  }

  // Stream Proxy Relay
  public static async proxyStream(req: Request, res: Response): Promise<void> {
    await StreamProxy.handleProxy(req, res);
  }

  // Versiyon Kontrolü (VK Mekanizması)
  public static checkVersion(req: Request, res: Response): void {
    const clientVersion = req.query.client_version as string || 'VK01';
    const isUpdateAvailable = clientVersion !== currentVersionState.latestVersion;
    res.json({
      ...currentVersionState,
      isUpdateAvailable,
      clientVersion
    });
  }

  // Yeni Versiyon Tetikleme (VK01 -> VK02 -> VK03 ...)
  public static bumpVersion(req: Request, res: Response): void {
    const nextCode = currentVersionState.versionCode + 1;
    const nextTag = `VK${nextCode < 10 ? '0' + nextCode : nextCode}`;
    
    currentVersionState = {
      currentVersion: currentVersionState.latestVersion,
      latestVersion: nextTag,
      versionCode: nextCode,
      downloadUrl: `https://github.com/vyslkrc/streamflow-iptv/releases/download/v2.4.0-${nextTag}/streamflow-v2.4.0-${nextTag}.apk`,
      changelog: [
        `Yeni versiyon ${nextTag} yayınlandı`,
        'Performans optimizasyonu ve kararlılık artışları',
        'Canlı akış gecikme süreleri azaltıldı'
      ],
      mandatory: false,
      releaseDate: new Date().toISOString().split('T')[0]
    };

    res.json({
      message: `Versiyon başarıyla artırıldı: ${nextTag}`,
      version: currentVersionState
    });
  }

  // APK Doğrudan İndirme Servisi
  public static downloadApk(req: Request, res: Response): void {
    const fs = require('fs');
    const path = require('path');
    const apkDir = path.resolve(__dirname, '../../../apk');
    if (fs.existsSync(apkDir)) {
      const files = fs.readdirSync(apkDir).filter((f: string) => f.endsWith('.apk'));
      if (files.length > 0) {
        const latestApk = files[files.length - 1];
        const filePath = path.join(apkDir, latestApk);
        res.download(filePath, latestApk);
        return;
      }
    }
    res.status(404).json({ error: 'APK dosyası sunucuda bulunamadı.' });
  }
}

