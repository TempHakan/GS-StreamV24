# StreamFlow IPTV (Android & Android TV + Backend)

[![Download APK](https://img.shields.io/badge/İndir-StreamFlow--v2.4.0--VK05.apk-00E5FF?style=for-the-badge&logo=android&logoColor=black)](apk/StreamFlow-IPTV-v2.4.0-VK05.apk)
[![Version](https://img.shields.io/badge/Sürüm-v2.4.0--VK05-7C4DFF?style=for-the-badge)](version.properties)
[![Platform](https://img.shields.io/badge/Platform-Mobile%20%7C%20Android%20TV%2016%3A9-FF007F?style=for-the-badge&logo=android)](app)
[![License](https://img.shields.io/badge/Lisans-MIT-green?style=for-the-badge)](LICENSE)

---

## 📥 1. Android APK Doğrudan İndirme

StreamFlow IPTV güncel derlenmiş hazır APK dosyasını aşağıdaki bağlantılardan hemen indirebilirsiniz:

| Sürüm | Dosya Adı | Boyut | Durum | İndirme Bağlantısı |
| :--- | :--- | :--- | :--- | :--- |
| **v2.4.0-VK05 (Güncel Son Sürüm)** | `StreamFlow-IPTV-v2.4.0-VK05.apk` | **~27 MB** | 🚀 **En Yeni** | [⬇️ **Doğrudan APK İndir**](apk/StreamFlow-IPTV-v2.4.0-VK05.apk) \| [🌐 Raw İndir](https://raw.githubusercontent.com/vyslkrc/streamflow-iptv/main/apk/StreamFlow-IPTV-v2.4.0-VK05.apk) |
| **v2.4.0-VK04 (Önceki Sürüm)** | `StreamFlow-IPTV-v2.4.0-VK04.apk` | **~27 MB** | 📦 Arşiv | [⬇️ APK İndir](apk/StreamFlow-IPTV-v2.4.0-VK04.apk) |
| **v2.4.0-VK03 (Önceki Sürüm)** | `StreamFlow-IPTV-v2.4.0-VK03.apk` | **~26 MB** | 📦 Arşiv | [⬇️ APK İndir](apk/StreamFlow-IPTV-v2.4.0-VK03.apk) |
| **v2.4.0-VK02 (Önceki Sürüm)** | `StreamFlow-IPTV-v2.4.0-VK02.apk` | **~27 MB** | 📦 Arşiv | [⬇️ APK İndir](apk/StreamFlow-IPTV-v2.4.0-VK02.apk) |
| **v2.4.0-VK01 (İlk Sürüm)** | `StreamFlow-IPTV-v2.4.0-VK01.apk` | **~27 MB** | 📦 Arşiv | [⬇️ APK İndir](apk/StreamFlow-IPTV-v2.4.0-VK01.apk) |
| **GitHub Releases** | Otomatik CI/CD Dağıtımı | **~27 MB** | ⚡ Canlı | [🚀 **GitHub Releases Sayfası**](https://github.com/vyslkrc/streamflow-iptv/releases) |

> 💡 **Kurulum Notu:** İndirdiğiniz APK dosyasını Android telefonunuza veya Android TV / TV Box cihazınıza aktararak tek tıkla kurabilirsiniz ("Bilinmeyen kaynaklara izin ver" seçeneğini aktif ediniz).

---

## 📋 2. Sürüm Değişiklik Günlüğü (Changelog)

### 🌟 Sürüm v2.4.0-VK05 (Son Sürüm Değişiklikleri)
* 📂 **Cihaz Depolamasından / Yerel (Local) M3U, M3U8, Xtream ve STB İçe Aktarma:**
  * Kaynak ekle penceresine **"Yerel Dosya"** sekmesi eklendi.
  * Android Yerel Dosya Yöneticisi (`ActivityResultContracts.GetContent`) entegrasyonu ile telefondaki veya TV'deki `.m3u`, `.m3u8` veya `.txt` çalma listesi dosyaları doğrudan seçilip içe aktarılabilir.
  * Dosyadaki kanal sayısı ve dosya adı anında tespit edilip arayüzde gösterilir.
  * **Xtream Codes** ve **STB / MAC** sekmelerine cihazdaki `.json` veya `.txt` yapılandırma dosyalarından hesap bilgilerini tek tıkla otomatik doldurma seçeneği eklendi.
* 🖥️ **Tam Ekran (Fullscreen & Immersive Mode) Oynatıcı:**
  * Oynatıcı ekranına **çift dokunma (Double Tap)** veya kontrol panelindeki **Tam Ekran Butonu** (`Fullscreen` / `FullscreenExit`) ile video tam ekran moduna geçer.
  * Sistem barları (Status Bar ve Navigasyon Barı) gizlenerek videonun ekranın %100'ünü kaplaması sağlandı; donanımsal **Geri Tuşu (`BackHandler`)** ile tam ekrandan çıkılır.
* 🔕 **Akıllı Güncelleme Motoru (Kurunca Gereksiz Ekran Çıkmaz):**
  * Uygulama güncel olduğunda (`VK05`) açılışta asla güncelleme penceresi çıkmaz (`isAvailable = false`).
  * Telefonda önceki sürüm (`VK04` vb.) kurulu olan kullanıcılara GitHub ve Backend üzerinden **v2.4.0-VK05 güncelleme bildirimi** otomatik olarak gönderilir.
* 📝 **Kanal İsimleri Genişletildi:**
  * Kanal kartlarında (`ChannelListItem`) kanal adları 2 satıra kadar tam genişlikte gösterilerek uzun isimlerin kesilmesi önlendi.
* 📦 **v2.4.0-VK05 APK Derlendi:**
  * Versiyon kodu `5` ve sürüm etiketi `VK05` olarak güncellendi.

---

### 🌟 Sürüm v2.4.0-VK04
* 🖥️ Oynatıcı tam ekran modu ve çift dokunma desteği.
* 🔕 Sürekli açılan güncelleme penceresi hatası giderildi.
* 📝 Kanal isimleri için 2 satırlı genişletilmiş kart görünümü.
* 🛠️ TextureView ve ExoPlayer decoder fallback entegrasyonu.

---

### 🌟 Sürüm v2.4.0-VK03
* 🔊 Oynatıcı üzerinde ses açma/kapama (`VolumeUp`/`VolumeOff`) ve sessiz durum göstergesi.
* 📋 Canlı TV ekranında Çalma Listesi / Kaynak Seçici (M3U, Xtream, STB filtreleme).
* 🔄 Otomatik `bump_version.sh` ve `README.md` senkronizasyonu.

---

### 🌟 Sürüm v2.4.0-VK02
* 📺 STB / MAC Portal (MAG 250 & 322) entegrasyonu.
* 🔄 GitHub Releases API ve fail-safe APK kurulumu.
* 🛠️ Kaynaklar ekranı UI düzeltmesi ve FAB butonu.

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
git commit -m "feat: release v2.4.0-VK05 with local file import for m3u, m3u8, xtream, stb and smart updater"

# 3. GitHub deponuzu bağlayın (Kendi GitHub repo URL'nizi girin):
git remote add origin https://github.com/vyslkrc/streamflow-iptv.git

# 4. Kodları ve APK'yı GitHub'a pushlayın:
git push -u origin main
```

---

## 🔄 4. Otomatik Sürüm Artırma (`VK01` ➔ ... ➔ `VK05` ➔ ...)

Uygulamanın sürümleme sistemi `version.properties` ve `bump_version.sh` üzerinden dinamik ve otomatik çalışır:

```bash
bash bump_version.sh
# Çıktı:
# ==========================================
#  [StreamFlow] Versiyon Başarıyla Artırıldı! 
#  Önceki Sürüm : VK04 (v2.4.0-VK04)
#  Yeni Sürüm   : VK05 (v2.4.0-VK05)
#  Versiyon Kodu: 5
#  README.md otomatik senkronize edildi.
# ==========================================
```

---

## 🛡️ 5. Android "Sistem Güvenmiyor / Play Protect Engelledi" Uyarısını Geçme Adımları

Google Play Store dışından doğrudan indirilen (sideloaded) tüm açık kaynaklı APK dosyalarında Android ve Google Play Protect sistemi varsayılan olarak *"Geliştirici doğrulanamadı"* veya *"Güvenilmeyen kaynak"* uyarısı çıkarabilir.

Uygulama tamamen açık kaynaklı olup hiçbir zararlı kod içermez. Bu uyarıyı geçerek saniyeler içinde kurulumu tamamlamak için:

1. **Kurulum Ekranında:** Karşınıza *"Play Protect bu uygulamanın geliştiricisini tanımıyor"* veya *"Sistem bu uygulamaya güvenmiyor"* uyarısı çıktığında:
   * Ekrandaki **"Daha fazla ayrıntı" (More details)** yazısına dokunun.
   * Açılan ekranda **"Yine de yükle" (Install anyway)** butonuna basın.
2. **Bilinmeyen Kaynak İzni:** Eğer telefonunuz ilk defa APK yüklüyorsa:
   * Çıkan pencerede *"Ayarlar"* butonuna tıklayın.
   * *"Bu kaynaktan izin ver" (Allow from this source)* seçeneğini aktif edip geri dönerek kurulumu tamamlayın.
3. Uygulama hemen kurulacak ve tüm özellikleriyle güvenle çalışacaktır.
