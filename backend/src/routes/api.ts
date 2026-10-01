import { Router } from 'express';
import { ApiController } from '../controllers/apiController';

const router = Router();

// M3U & Xtream Routes
router.post('/m3u/parse', ApiController.parseM3u);
router.get('/xtream/channels', ApiController.getXtreamChannels);
router.get('/epg', ApiController.fetchEpg);

// Streaming Proxy
router.get('/proxy/stream', ApiController.proxyStream);

// Versiyon Güncelleme ve VK Artırma Mekanizması
router.get('/version/check', ApiController.checkVersion);
router.post('/version/bump', ApiController.bumpVersion);

export default router;
