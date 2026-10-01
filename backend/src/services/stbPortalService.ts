import axios from 'axios';
import { Channel } from '../models/types';

export class StbPortalService {
  private portalUrl: string;
  private mac: string;
  private token: string | null = null;

  constructor(portalUrl: string, mac: string) {
    // Portal URL normalize
    this.portalUrl = portalUrl.endsWith('/') ? portalUrl.slice(0, -1) : portalUrl;
    this.mac = mac.trim().toUpperCase();
  }

  private getHeaders(): Record<string, string> {
    const headers: Record<string, string> = {
      'User-Agent': 'Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG250 stbapp ver: 4 rev: 1812 Mobile Safari/533.3',
      'Cookie': `mac=${encodeURIComponent(this.mac)}; stb_lang=en; timezone=Europe%2FIstanbul`,
      'X-User-Agent': 'Model: MAG250; Link: Ethernet',
      'Referer': `${this.portalUrl}/c/`
    };
    if (this.token) {
      headers['Authorization'] = `Bearer ${this.token}`;
    }
    return headers;
  }

  public async handshake(): Promise<{ token: string; random: string }> {
    const url = `${this.portalUrl}/server/load.php?type=stb&action=handshake&JsHttpRequest=1-xml`;
    const res = await axios.get(url, {
      headers: this.getHeaders(),
      timeout: 10000
    });
    const data = res.data?.js || res.data;
    this.token = data?.token || null;
    return data;
  }

  public async getProfile(): Promise<any> {
    if (!this.token) await this.handshake();
    const url = `${this.portalUrl}/server/load.php?type=stb&action=get_profile&JsHttpRequest=1-xml`;
    const res = await axios.get(url, {
      headers: this.getHeaders(),
      timeout: 10000
    });
    return res.data?.js || res.data;
  }

  public async getAllChannels(): Promise<Channel[]> {
    if (!this.token) {
      try {
        await this.handshake();
      } catch (e) {
        // Devam et, bazı portallar doğrudan handshake istemez
      }
    }

    const url = `${this.portalUrl}/server/load.php?type=itv&action=get_all_channels&JsHttpRequest=1-xml`;
    const res = await axios.get(url, {
      headers: this.getHeaders(),
      timeout: 15000
    });

    const data = res.data?.js?.data || res.data?.data || [];
    return data.map((item: any, index: number) => {
      const channelId = String(item.id || item.number || index + 1);
      const cmd = item.cmd || '';
      let streamUrl = cmd.replace(/^ffmpeg\s+/, '').replace(/^auto\s+/, '');
      if (!streamUrl.startsWith('http')) {
        // Link oluşturma proxy url
        streamUrl = `${this.portalUrl}/server/load.php?type=itv&action=create_link&cmd=${encodeURIComponent(cmd)}`;
      }

      return {
        id: `stb_${channelId}`,
        name: item.name || `STB Kanal ${channelId}`,
        streamUrl: streamUrl,
        logoUrl: item.logo ? `${this.portalUrl}/${item.logo}` : undefined,
        groupTitle: item.genre_id ? `Kategori ${item.genre_id}` : 'STB Portalı',
        tvgId: item.xmltv_id,
        streamType: 'live' as const
      };
    });
  }
}
