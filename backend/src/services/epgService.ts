import axios from 'axios';
import zlib from 'zlib';
import { EpgProgram } from '../models/types';

export class EpgService {
  private static cache: Map<string, EpgProgram[]> = new Map();

  /**
   * XMLTV dosyasını (Gzip veya düz XML) indirip ayrıştırır.
   */
  public static async fetchAndParse(epgUrl: string): Promise<Map<string, EpgProgram[]>> {
    const isGzip = epgUrl.endsWith('.gz');

    const response = await axios.get(epgUrl, {
      responseType: isGzip ? 'arraybuffer' : 'text',
      timeout: 30000,
      headers: { 'User-Agent': 'StreamFlow IPTV EPG Engine' }
    });

    let xmlText = '';
    if (isGzip) {
      const buffer = Buffer.from(response.data);
      xmlText = zlib.gunzipSync(buffer).toString('utf-8');
    } else {
      xmlText = response.data;
    }

    // Basit ve hızlı regex tabanlı XMLTV ayrıştırıcı
    const programRegex = /<programme\s+start="([^"]+)"\s+stop="([^"]+)"\s+channel="([^"]+)">[\s\S]*?<title[^>]*>([\s\S]*?)<\/title>([\s\S]*?)<\/programme>/g;
    let match;
    const result = new Map<string, EpgProgram[]>();

    while ((match = programRegex.exec(xmlText)) !== null) {
      const [, startRaw, stopRaw, channelId, title, extra] = match;
      const descMatch = extra ? extra.match(/<desc[^>]*>([\s\S]*?)<\/desc>/) : null;
      const description = descMatch ? descMatch[1].trim() : '';

      const prog: EpgProgram = {
        id: `${channelId}_${startRaw}`,
        channelId: channelId.trim(),
        title: title.trim(),
        description: description,
        start: startRaw.trim(),
        stop: stopRaw.trim()
      };

      if (!result.has(prog.channelId)) {
        result.set(prog.channelId, []);
      }
      result.get(prog.channelId)!.push(prog);
    }

    this.cache = result;
    return result;
  }

  public static getChannelEpg(channelId: string): EpgProgram[] {
    return this.cache.get(channelId) || [];
  }
}
