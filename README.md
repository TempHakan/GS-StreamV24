# StreamFlow IPTV (Android & Android TV + Backend)

[![Download APK](https://img.shields.io/badge/İndir-StreamFlow--v2.4.0--VK03.apk-00E5FF?style=for-the-badge&logo=android&logoColor=black)](apk/StreamFlow-IPTV-v2.4.0-VK03.apk)
[![Version](https://img.shields.io/badge/Sürüm-v2.4.0--VK03-7C4DFF?style=for-the-badge)](version.properties)
[![Platform](https://img.shields.io/badge/Platform-Mobile%20%7C%20Android%20TV%2016%3A9-FF007F?style=for-the-badge&logo=android)](app)
[![License](https://img.shields.io/badge/Lisans-MIT-green?style=for-the-badge)](LICENSE)

---

## 📥 1. Android APK Doğrudan İndirme

StreamFlow IPTV güncel derlenmiş hazır APK dosyasını aşağıdaki bağlantılardan hemen indirebilirsiniz:

| Sürüm | Dosya Adı | Boyut | Durum | İndirme Bağlantısı |
| :--- | :--- | :--- | :--- | :--- |
| **v2.4.0-VK03 (Güncel Son Sürüm)** | `StreamFlow-IPTV-v2.4.0-VK03.apk` | **~26 MB** | 🚀 **En Yeni** | [⬇️ **Doğrudan APK İndir**](apk/StreamFlow-IPTV-v2.4.0-VK03.apk) \| [🌐 Raw İndir](https://raw.githubusercontent.com/vyslkrc/streamflow-iptv/main/apk/StreamFlow-IPTV-v2.4.0-VK03.apk) |
| **v2.4.0-VK02 (Önceki Sürüm)** | `StreamFlow-IPTV-v2.4.0-VK02.apk` | **~27 MB** | 📦 Arşiv | [⬇️ APK İndir](apk/StreamFlow-IPTV-v2.4.0-VK02.apk) |
| **v2.4.0-VK01 (İlk Sürüm)** | `StreamFlow-IPTV-v2.4.0-VK01.apk` | **~27 MB** | 📦 Arşiv | [⬇️ APK İndir](apk/StreamFlow-IPTV-v2.4.0-VK01.apk) |
| **GitHub Releases** | Otomatik CI/CD Dağıtımı | **~26 MB** | ⚡ Canlı | [🚀 **GitHub Releases Sayfası**](https://github.com/vyslkrc/streamflow-iptv/releases) |

> 💡 **Kurulum Notu:** İndirdiğiniz APK dosyasını Android telefonunuza veya Android TV / TV Box cihazınıza aktararak tek tıkla kurabilirsiniz ("Bilinmeyen kaynaklara izin ver" seçeneğini aktif ediniz).

---

## 📋 2. Sürüm Değişiklik Günlüğü (Changelog)

### 🌟 Sürüm v2.4.0-VK03 (Son Sürüm Değişiklikleri)
* 🔊 **Oynatıcı Üzerinde Ses Açma / Kapama (Mute / Unmute):**
  * ExoPlayer oynatıcısının hem üst kontrol barına hem de oynatıcı altındaki hızlı aksiyon çubuğuna tek dokunuşla çalışan ses açma/kapama (`VolumeUp` / `VolumeOff`) butonu eklendi.
  * Ses kapatıldığında ekranda şık *"Sessiz"* durumu rozeti gösterilir.
* 📋 **Canlı TV Ekranında Çalma Listesi / Kaynak Seçici (Playlist Filter):**
  * Canlı TV sekmesine ekli tüm çalma listelerini (M3U, Xtream Codes, STB Portalları) gösteren dinamik yatay kaydırılabilir kaynak filtre çipleri entegre edildi.
  * Kullanıcı tek tıkla yalnızca seçtiği listenin/portalın kanallarını filtreleyebilir; kategoriler de seçilen listeye göre otomatik filtrelenir.
  * Aktif liste gösterge rozeti ve "Tümünü Göster" sıfırlama seçeneği eklendi.
* 🔄 **Otomatik README Senkronizasyonlu Sürüm Artırma:**
  * `bump_version.sh` betiği her versiyon artışında (`VK02` ➔ `VK03` vb.) `version.properties` ve `README.md` dosyasındaki tüm sürüm etiketlerini ve indirme bağlantılarını otomatik güncelleyecek şekilde programlandı.
* 🛡️ **Çok Katmanlı İndirme & Kesintisiz Güncelleme Motoru:**
  * "İndirme başarısız oldu" sorununu önlemek için GitHub Releases, GitHub Raw, Backend Proxy ve yerel fail-safe paket hazırlama mekanizması devrededir.

---

### 🌟 Sürüm v2.4.0-VK02
* 📺 **STB / MAC Portal Desteği (Stalker Middleware / MAG 250 & 322):**
  * Kaynaklar bölümüne **STB / MAC Portal** sekmesi eklendi.
  * MAG portal adresi (`/c/`) ve cihaz MAC adresi (`00:1A:79:XX:XX:XX`) ile bağlanma sağlandı.
  * Test için tek dokunuşla "Rastgele MAC Üret" butonu ve örnek portal şablonu eklendi.
  * Node.js backend servisine Stalker Handshake ve kanal çekme uç noktası (`/api/v1/stb/channels`) eklendi.
* 🔄 **GitHub Otomatik APK İndirme ve Telefon Kurulum Motoru:**
  * GitHub Releases API kontrol edilerek yeni sürüm çıktığında telefonlarda otomatik indirme ve `FileProvider` üzerinden paket kurulumu başlatıldı.
* 🛠️ **Kaynaklar Ekranı UI Düzeltmesi:**
  * "Yeni Ekle" butonunun dar telefon ekranlarında taşması düzeltildi; "Kaynak Ekle" FAB butonu eklendi.

---

### 📌 Sürüm v2.4.0-VK01 (İlk Sürüm)
* Android TV (16:9 D-Pad Leanback) ve Mobil çift arayüz desteği.
* AndroidX Media3 (ExoPlayer) ile HLS (`.m3u8`), TS ve MP4 akış desteği.
* VLC Media Player Intent entegrasyonu (`org.videolan.vlc`).
* M3U / M3U8 ve Xtream Codes API çalma listesi desteği.
* EPG ve 7 günlük Catch-up yayın akışı rehberi.
* Dark OLED (`#0B0E14`), Clean Light ve Vibrant Neon temaları.
* Node.js Stream Proxy backend servisi.

---

## 🚀 3. GitHub'a Push ve Otomatik Pipeline

Aşağıdaki komutları terminalinizde çalıştırarak projeyi ve güncel APK'yı doğrudan GitHub reponuza gönderebilirsiniz:

```bash
# 1. Ana dalı main olarak belirleyin
git branch -M main

# 2. Değişiklikleri ekleyin ve commit yapın
git add .
git commit -m "feat: release v2.4.0-VK03 with audio mute toggle, live tv playlist selector and auto README sync"

# 3. GitHub deponuzu bağlayın (Kendi GitHub repo URL'nizi girin):
git remote add origin https://github.com/vyslkrc/streamflow-iptv.git

# 4. Kodları ve APK'yı GitHub'a pushlayın:
git push -u origin main
```

---

## 🔄 4. Otomatik Sürüm Artırma (`VK01` ➔ `VK02` ➔ `VK03` ...)

Uygulamanın sürümleme sistemi `version.properties` ve `bump_version.sh` üzerinden dinamik ve otomatik çalışır:

```bash
./bump_version.sh
# Çıktı:
# ==========================================
#  [StreamFlow] Versiyon Başarıyla Artırıldı! 
#  Önceki Sürüm : VK02 (v2.4.0-VK02)
#  Yeni Sürüm   : VK03 (v2.4.0-VK03)
#  Versiyon Kodu: 3
#  README.md otomatik senkronize edildi.
# ==========================================
```
