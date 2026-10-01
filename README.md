# StreamFlow IPTV (Android & Android TV + Backend)

> **Sürüm:** v2.4.0 (VK Serisi: `VK01`, `VK02`...)  
> **Platform Desteği:** Android Mobile, Android TV / Google TV (16:9 Leanback), Tablet  
> **Medya Motoru:** AndroidX Media3 (ExoPlayer) + VLC Media Player Intent (`org.videolan.vlc`)  

---

## 🚀 Proje Yapısı

```
streamflow-iptv/
├── .github/workflows/          # GitHub Actions CI/CD (Otomatik APK derleme ve Release)
├── app/                        # Android Native Jetpack Compose İstemci Uygulaması
│   ├── src/main/java/          # Kotlin MVVM Kaynak Kodları (ExoPlayer, VLC Intent, EPG, Temalar)
│   ├── src/main/res/           # Vektör ikonlar, TV banner, adaptive mipmap'ler
│   └── build.gradle.kts        # VK versiyonlama ve Gradle yapılandırması
├── backend/                    # Node.js Express & TypeScript Arka Uç Servisi
│   ├── src/controllers/        # Xtream, M3U, EPG ve VK Güncelleme denetleyicisi
│   ├── src/services/           # M3U ayrıştırıcı, Xtream istemcisi, Stream proxy, EPG
│   ├── Dockerfile              # Docker konteyner konfigürasyonu
│   └── docker-compose.yml      # Tek komutla ayağa kaldırma
├── version.properties          # Dinamik versiyon takip dosyası (VK_CODE, VK_TAG=VK01)
├── bump_version.sh             # Versiyonu 1 artıran kabuk betiği (VK01 -> VK02)
└── README.md                   # Kurulum, GitHub Push ve Kullanım Kılavuzu
```

---

## 📦 1. GitHub'a Push Rehberi

Aşağıdaki komutları terminalinizde çalıştırarak projeyi doğrudan GitHub deponuza gönderebilirsiniz:

```bash
# 1. Proje ana dizininde Git deposunu başlatın (varsa atlayın)
git init

# 2. Değişiklikleri ekleyin ve ilk commit'i yapın
git add .
git commit -m "feat: StreamFlow IPTV full release with backend, TV/Mobile UI & VK versioning"

# 3. Ana dalı 'main' olarak ayarlayın
git branch -M main

# 4. GitHub'daki deponuzun adresini bağlayın
# (Kendi kullanıcı adınızı ve repo adınızı yazın)
git remote add origin https://github.com/<KULLANICI_ADINIZ>/streamflow-iptv.git

# 5. Kodları GitHub'a gönderin
git push -u origin main
```

---

## 🔄 2. "VK" Versiyon Güncelleme Mekanizması

Kullanıcının talep ettiği **VK01 -> VK02 -> VK03** mekanizması hem yerel derlemede hem backend'de hem de uygulama içinde entegredir:

1. **Komut Satırı / Betik ile Artırma:**
   ```bash
   ./bump_version.sh
   # Çıktı: Versiyon Başarıyla Artırıldı! Önceki: VK01 -> Yeni: VK02
   ```

2. **Gradle Görevi ile Artırma:**
   ```bash
   gradle bumpVkVersion
   ```

3. **Uygulama İçi Canlı Güncelleme:**
   - Ayarlar (Settings) ekranında ve ana ekranda **"VK Sürüm Güncelleme Motoru"** yer alır.
   - Yeni bir versiyon (örneğin VK02) geldiğinde kullanıcıya bildirim/diyalog sunulur.
   - "Güncellemeyi İndir ve Kur" butonu APK indirme ve Android paket yükleyicisi (`FileProvider`) intent'ini tetikler.

---

## 📺 3. Android APK Derleme

```bash
# Debug APK derleme:
gradle assembleDebug

# Çıktı konumu:
# app/build/outputs/apk/debug/app-debug.apk

# Release APK derleme:
gradle assembleRelease
```

---

## 🌐 4. Backend Servisini Çalıştırma

```bash
cd backend
npm install
npm run dev
# veya Docker ile:
docker-compose up -d --build
```
- Stream Proxy: `http://localhost:3000/api/v1/proxy/stream?url=<AKIS_URL>`
- Versiyon Kontrolü: `http://localhost:3000/api/v1/version/check`
- Versiyon Artırma: `POST http://localhost:3000/api/v1/version/bump`
