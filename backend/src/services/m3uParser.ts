import { Channel } from '../models/types';

export class M3uParser {
  /**
   * M3U / M3U8 metnini ayrıştırarak Channel nesneleri dizisine dönüştürür.
   */
  public static parse(content: string): Channel[] {
    const lines = content.split(/\r?\n/);
    const channels: Channel[] = [];
    let currentChannel: Partial<Channel> | null = null;
    let index = 1;

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i].trim();

      if (line.startsWith('#EXTINF:')) {
        currentChannel = {};
        
        // tvg-id="xyz"
        const tvgIdMatch = line.match(/tvg-id="([^"]*)"/i);
        if (tvgIdMatch) currentChannel.tvgId = tvgIdMatch[1];

        // tvg-name="xyz"
        const tvgNameMatch = line.match(/tvg-name="([^"]*)"/i);
        if (tvgNameMatch) currentChannel.tvgName = tvgNameMatch[1];

        // tvg-logo="xyz"
        const logoMatch = line.match(/tvg-logo="([^"]*)"/i);
        if (logoMatch) currentChannel.logoUrl = logoMatch[1];

        // group-title="xyz"
        const groupMatch = line.match(/group-title="([^"]*)"/i);
        if (groupMatch) currentChannel.groupTitle = groupMatch[1];

        // Kanal adı (satırın virgülden sonraki son kısmı)
        const commaIndex = line.lastIndexOf(',');
        if (commaIndex !== -1) {
          currentChannel.name = line.substring(commaIndex + 1).trim();
        } else {
          currentChannel.name = currentChannel.tvgName || `Kanal ${index}`;
        }

      } else if (line.length > 0 && !line.startsWith('#') && currentChannel) {
        // Akış URL satırı
        currentChannel.streamUrl = line;
        currentChannel.id = currentChannel.tvgId || `channel_${index}`;
        if (!currentChannel.groupTitle) {
          currentChannel.groupTitle = 'Genel';
        }
        
        // VOD tespiti (mp4, mkv vb.)
        const isVod = line.includes('.mp4') || line.includes('.mkv') || line.includes('/movie/');
        currentChannel.streamType = isVod ? 'vod' : 'live';

        channels.push(currentChannel as Channel);
        currentChannel = null;
        index++;
      }
    }

    return channels;
  }
}
