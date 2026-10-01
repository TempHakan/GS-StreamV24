# StreamFlow IPTV Backend & Proxy Service

StreamFlow IPTV Android ve Android TV uygulaması için tasarlanmış yüksek performanslı arka uç servisi.

## Özellikler
- **Stream Relay Proxy (`/api/v1/proxy/stream`):** TS ve HLS canlı yayınlarını aktarır, CORS ve User-Agent kısıtlamalarını aşar.
- **M3U / M3U8 Ayrıştırıcı (`/api/v1/m3u/parse`):** Çalma listelerini JSON formatına dönüştürür.
- **Xtream Codes API Köprüsü (`/api/v1/xtream/channels`):** Canlı yayın ve VOD akışlarını çeker.
- **EPG XMLTV İndirici & Ayrıştırıcı (`/api/v1/epg`):** Gzip/XMLTV formatını ayrıştırır.
- **VK Versiyon Güncelleme Mekanizması (`/api/v1/version/check` & `/api/v1/version/bump`):** Her yeni derlemede versiyonu `VK01` -> `VK02` -> `VK03` şeklinde artırır.

## Hızlı Başlangıç

### 1. Yerel Olarak Çalıştırma:
```bash
cd backend
npm install
npm run dev
```

### 2. Docker ile Çalıştırma:
```bash
cd backend
docker-compose up -d --build
```
Sunucu varsayılan olarak `http://localhost:3000` adresinde çalışacaktır.
