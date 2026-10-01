import express from 'express';
import cors from 'cors';
import apiRouter from './routes/api';

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ extended: true, limit: '50mb' }));

// Health Check
app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    service: 'StreamFlow IPTV Backend & Proxy',
    version: 'v2.4.0',
    timestamp: new Date().toISOString()
  });
});

// API Routes
app.use('/api/v1', apiRouter);

app.listen(PORT, () => {
  console.log(`=========================================`);
  console.log(`StreamFlow IPTV Backend Aktif`);
  console.log(`Port: ${PORT}`);
  console.log(`Proxy: http://localhost:${PORT}/api/v1/proxy/stream?url=...`);
  console.log(`Version Check: http://localhost:${PORT}/api/v1/version/check`);
  console.log(`=========================================`);
});

export default app;
