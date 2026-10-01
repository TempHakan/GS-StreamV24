import { Request, Response } from 'express';
import axios from 'axios';

export class StreamProxy {
  /**
   * TS / HLS veri aktarım tüneli (CORS & Headers spoofing).
   * IPTV sağlayıcılarının CORS ve User-Agent kısıtlamalarını aşar.
   */
  public static async handleProxy(req: Request, res: Response): Promise<void> {
    const targetUrl = req.query.url as string;
    if (!targetUrl) {
      res.status(400).send('Hata: "url" parametresi zorunludur.');
      return;
    }

    try {
      const response = await axios({
        method: 'get',
        url: targetUrl,
        responseType: 'stream',
        headers: {
          'User-Agent': 'VLC/3.0.18 LibVLC/3.0.18',
          'Accept': '*/*',
          'Connection': 'keep-alive'
        },
        timeout: 20000
      });

      // Gelen akış başlıklarını istemciye ilet
      const contentType = response.headers['content-type'] || 'video/mp2t';
      res.setHeader('Content-Type', contentType);
      res.setHeader('Access-Control-Allow-Origin', '*');
      res.setHeader('Access-Control-Allow-Headers', '*');

      response.data.pipe(res);

      req.on('close', () => {
        if (response.data && typeof response.data.destroy === 'function') {
          response.data.destroy();
        }
      });
    } catch (err: any) {
      console.error('Stream proxy hatası:', err.message);
      if (!res.headersSent) {
        res.status(502).json({ error: 'Yayın aktarılamadı', details: err.message });
      }
    }
  }
}
