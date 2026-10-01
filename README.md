# StreamFlow IPTV (Android & Android TV + Backend)

[![Download APK](https://img.shields.io/badge/İndir-StreamFlow--v2.4.0--VK01.apk-00E5FF?style=for-the-badge&logo=android&logoColor=black)](apk/StreamFlow-IPTV-v2.4.0-VK01.apk)
[![Version](https://img.shields.io/badge/Sürüm-v2.4.0--VK01-7C4DFF?style=for-the-badge)](version.properties)
[![Platform](https://img.shields.io/badge/Platform-Mobile%20%7C%20Android%20TV%2016%3A9-FF007F?style=for-the-badge&logo=android)](app)
[![License](https://img.shields.io/badge/Lisans-MIT-green?style=for-the-badge)](LICENSE)

---

## 📥 1. Android APK Doğrudan İndirme

StreamFlow IPTV derlenmiş hazır APK dosyasını aşağıdaki bağlantılardan hemen indirebilirsiniz:

| Sürüm | Dosya | Boyut | İndirme Bağlantısı |
| :--- | :--- | :--- | :--- |
| **v2.4.0-VK01 (Stabil)** | `StreamFlow-IPTV-v2.4.0-VK01.apk` | **~26 MB** | [⬇️ **Doğrudan Depodan İndir**](apk/StreamFlow-IPTV-v2.4.0-VK01.apk) |
| **GitHub Raw Linki** | `StreamFlow-IPTV-v2.4.0-VK01.apk` | **~26 MB** | [🌐 **Raw İndirme Linki**](https://github.com/vyslkrc/streamflow-iptv/raw/main/apk/StreamFlow-IPTV-v2.4.0-VK01.apk) |
| **GitHub Releases** | Son Sürüm Otomatik Dağıtım | **~26 MB** | [🚀 **GitHub Releases Sayfası**](https://github.com/vyslkrc/streamflow-iptv/releases) |

> 💡 **Kurulum Notu:** İndirdiğiniz APK'yı Android telefonunuza veya Android TV / TV Box cihazınıza aktararak tek tıkla kurabilirsiniz ("Bilinmeyen kaynaklara izin ver" seçeneğini aktif ediniz).

---

## 🚀 2. GitHub'a Push ve Otomatik Pipeline

Aşağıdaki komutları terminalinizde çalıştırarak projeyi ve hazır APK'yı doğrudan GitHub reponuza gönderebilirsiniz:

```bash
# 1. Ana dalı main olarak belirleyin
git branch -M main

# 2. Değişiklikleri ekleyin ve ilk sürümü commit yapın
git add .
git commit -m "feat: StreamFlow IPTV v2.4.0-VK01 release with ready APK, backend and TV support"

# 3. GitHub deponuzu bağlayın (Kendi GitHub repo URL'nizi girin):
git remote add origin https://github.com/vyslkrc/streamflow-iptv.git

# 4. Kodları ve APK'yı GitHub'a pushlayın:
git push -u origin main
```

> **GitHub Actions CI/CD:** Projeyi pushladığınızda `.github/workflows/build-apk.yml` otomatik çalışır; hem GitHub Release oluşturur hem de yeni APK dosyalarını her zaman güncel olarak dağıtır.

---

## 🔄 3. VK Versiyon Güncelleme Mekanizması (`VK01` ➔ `VK02` ➔ `VK03` ...)

Kullanıcı talebi doğrultusunda geliştirilen **otomatik sürüm artırma mekanizması**:

1. **Komut Satırından / Terminalden Artırma:**
   ```bash
   ./bump_version.sh
   # Çıktı: [StreamFlow] Versiyon Başarıyla Artırıldı! Önceki: VK01 -> Yeni: VK02 (2.4.0-VK02)
   ```

2. **Gradle Görevi ile Artırma:**
   ```bash
   gradle bumpVkVersion
   ```

3. **Uygulama İçi Canlı Güncelleme:**
   - Uygulama içinde üst barda ve **Ayarlar** sekmesinde güncel versiyon etiketi (**VK01**) görünür.
   - Yeni bir versiyon çıktığında açılan diyalog ile **"APK İndir ve Güncelle (VK02)"** işlemi başlatılır.
   - Ayarlar ekranındaki test butonu ile anında bir sonraki VK sürümü simüle edilebilir.

---

## 📱 4. Uygulama Yetenekleri & Ekranlar

1. **Çift Form Faktörü:**
   - **Mobil:** Dikey dokunmatik gezinme (Canlı TV, VOD, Oynatıcı, Kaynaklar, Ayarlar sekmeleri).
   - **Android TV / Google TV (16:9 Leanback):** D-Pad uzaktan kumanda odak efektleri, geniş ekran video ızgarası ve TV Başlatıcı simgesi (`tv_banner`).
2. **Medya Motoru:**
   - Dahili **AndroidX Media3 (ExoPlayer)** ile HLS (`.m3u8`), TS ve MP4 akış desteği.
   - En-boy oranı değişimi (Sığdır, Doldur, 16:9, 4:3).
   - **VLC Media Player Intent Entegrasyonu (`org.videolan.vlc`):** Tek tıkla harici VLC uygulamasını açma ve akışı aktarma.
3. **EPG & Zaman Çizelgesi:**
   - Kanalların yayın akışı, anlık program ilerleme çubuğu ve 7 günlük Catch-up (Geriye Sarma) desteği.
4. **Dinamik Temalar:**
   - **Dark OLED (`#0B0E14`)**
   - **Vibrant Neon (Cyberpunk Glow)**
   - **Clean Light (Temiz Açık)**
5. **Yerel Veritabanı:**
   - Favoriler, geçmiş ve çalma listeleri için **Room SQLite** entegrasyonu.

---

## 🌐 5. Backend Proxy Servisi (`/backend`)

CORS engellerini aşmak ve User-Agent spoofing yapmak için hazır Node.js servisi:

```bash
cd backend
npm install
npm run dev
# veya Docker ile:
docker-compose up -d --build
```
* **Stream Proxy:** `http://localhost:3000/api/v1/proxy/stream?url=<AKIS_URL>`
* **M3U Ayrıştırıcı:** `POST http://localhost:3000/api/v1/m3u/parse`
* **Xtream Codes API:** `http://localhost:3000/api/v1/xtream/channels`
* **EPG XMLTV İndirici:** `http://localhost:3000/api/v1/epg?url=<XMLTV_URL>`
* **VK Versiyon API:** `http://localhost:3000/api/v1/version/check`
