# StreamFlow IPTV (Android & Android TV + Backend)

[![Download APK](https://img.shields.io/badge/İndir-StreamFlow--v2.4.0--VK06.apk-00E5FF?style=for-the-badge&logo=android&logoColor=black)](apk/StreamFlow-IPTV-v2.4.0-VK06.apk)
[![Version](https://img.shields.io/badge/Sürüm-v2.4.0--VK06-7C4DFF?style=for-the-badge)](version.properties)
[![Platform](https://img.shields.io/badge/Platform-Mobile%20%7C%20Android%20TV%2016%3A9-FF007F?style=for-the-badge&logo=android)](app)
[![License](https://img.shields.io/badge/Lisans-MIT-green?style=for-the-badge)](LICENSE)

---

## 📥 1. Android APK Doğrudan İndirme

StreamFlow IPTV güncel derlenmiş hazır APK dosyasını aşağıdaki bağlantılardan hemen indirebilirsiniz:

| Sürüm | Dosya Adı | Boyut | Durum | İndirme Bağlantısı |
| :--- | :--- | :--- | :--- | :--- |
| **v2.4.0-VK06 (Güncel Son Sürüm)** | `StreamFlow-IPTV-v2.4.0-VK06.apk` | **~27 MB** | 🚀 **En Yeni** | [⬇️ **Doğrudan APK İndir**](apk/StreamFlow-IPTV-v2.4.0-VK06.apk) \| [🌐 Raw İndir](https://raw.githubusercontent.com/vyslkrc/streamflow-iptv/main/apk/StreamFlow-IPTV-v2.4.0-VK06.apk) |
| **v2.4.0-VK05 (Önceki Sürüm)** | `StreamFlow-IPTV-v2.4.0-VK05.apk` | **~27 MB** | 📦 Arşiv | [⬇️ APK İndir](apk/StreamFlow-IPTV-v2.4.0-VK05.apk) |
| **v2.4.0-VK04 (Önceki Sürüm)** | `StreamFlow-IPTV-v2.4.0-VK04.apk` | **~27 MB** | 📦 Arşiv | [⬇️ APK İndir](apk/StreamFlow-IPTV-v2.4.0-VK04.apk) |
| **v2.4.0-VK03 (Önceki Sürüm)** | `StreamFlow-IPTV-v2.4.0-VK03.apk` | **~26 MB** | 📦 Arşiv | [⬇️ APK İndir](apk/StreamFlow-IPTV-v2.4.0-VK03.apk) |
| **v2.4.0-VK02 (Önceki Sürüm)** | `StreamFlow-IPTV-v2.4.0-VK02.apk` | **~27 MB** | 📦 Arşiv | [⬇️ APK İndir](apk/StreamFlow-IPTV-v2.4.0-VK02.apk) |
| **v2.4.0-VK01 (İlk Sürüm)** | `StreamFlow-IPTV-v2.4.0-VK01.apk` | **~27 MB** | 📦 Arşiv | [⬇️ APK İndir](apk/StreamFlow-IPTV-v2.4.0-VK01.apk) |
| **GitHub Releases** | Otomatik CI/CD Dağıtımı | **~27 MB** | ⚡ Canlı | [🚀 **GitHub Releases Sayfası**](https://github.com/vyslkrc/streamflow-iptv/releases) |

> 💡 **Kurulum Notu:** İndirdiğiniz APK dosyasını Android telefonunuza veya Android TV / TV Box cihazınıza aktararak tek tıkla kurabilirsiniz ("Bilinmeyen kaynaklara izin ver" seçeneğini aktif ediniz).

---

## 📋 2. Sürüm Değişiklik Günlüğü (Changelog)

### 🌟 Sürüm v2.4.0-VK06 (Son Sürüm Değişiklikleri)
* 🎬 **Gelişmiş VOD (Film & Dizi) Ayrıştırma Motoru:**
  * Çalma listeleri eklenirken video dosya formatları (`.mp4`, `.mkv`, `.avi`, `.mov`, `.flv`, `.wmv`), URL dizinleri (`/movie/`, `/series/`, `/vod/`, `/films/`, `/diziler/`), etiketler ve kategori isimleri (`group-title`) derinlemesine analiz edilir.
  * Tespit edilen tüm VOD film ve dizi yayınları Canlı TV'den ayrıştırılarak otomatik olarak **"VOD: Film & Dizi Kütüphanesi"** bölümüne aktarılır.
* ⏩ **VOD İlerleme Çubuğu (Scrubbing Seekbar) ve İleri/Geri Sarma Butonları:**
  * VOD videolarında oynatıcı altına anlık zaman göstergesi (`00:00 / 01:45:00`) ve parmakla sürüklenebilir akıcı ilerleme çubuğu (`Slider`) entegre edildi.
  * Oynatıcı orta kontrol paneline **10 Saniye Geri Sar (`FastRewind`)** ve **10 Saniye İleri Sar (`FastForward`)** butonları eklendi.
* 👆 **Ekran Üzerinde Dikey Jest Kontrolleri (Ses & Parlaklık):**
  * **Ekranın Sağ Tarafında Dikey Kaydırma (Yukarı/Aşağı):** Cihazın ve ExoPlayer'ın medya ses seviyesini ayarlar; ekranda şık ses HUD rozeti (`%`) belirir.
  * **Ekranın Sol Tarafında Dikey Kaydırma (Yukarı/Aşağı):** Ekran parlaklığını anlık olarak ayarlar; ekranda sarı parlaklık HUD rozeti (`%`) belirir.
  * Çift dokunma ile tam ekran açılır/kapanır; tek dokunma ile kontroller gizlenir.
* 🔒 **GitHub Private (Gizli) Depo Güncelleme Desteği:**
  * Deponuz gizli (Private) olduğu durumlarda GitHub API'sinin 404 hatası vermesini engellemek için Ayarlar ekranına **"GitHub Token (Özel Depo)"** giriş alanı eklendi (`streamflow_prefs` üzerinde güvenle saklanır).
  * Ayrıca telefonun tarayıcısında açık olan GitHub oturumunu kullanarak tek tıkla en güncel APK'yı indirmeyi sağlayan **"Tarayıcıda GitHub Releases Aç"** butonu entegre edildi.
* 📦 **v2.4.0-VK06 APK Derlendi:**
  * Versiyon kodu `6` ve sürüm etiketi `VK06` olarak güncellendi.

---

### 🌟 Sürüm v2.4.0-VK05
* 📂 Cihaz Depolamasından / Yerel (Local) M3U, M3U8, Xtream ve STB İçe Aktarma.
* 🛡️ Network Security Config ve Google Play Protect güvenlik sertifikası optimizasyonları.
* 🔕 Akıllı güncelleme motoru (kurulduktan sonra gereksiz pencere açılmaz).

---

### 🌟 Sürüm v2.4.0-VK04
* 🖥️ Oynatıcı tam ekran modu (Immersive) ve çift dokunma desteği.
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

---

### 📌 Sürüm v2.4.0-VK01 (İlk Sürüm)
* Android TV (16:9 D-Pad Leanback) ve Mobil çift arayüz desteği.
* AndroidX Media3 (ExoPlayer) ile HLS (`.m3u8`), TS ve MP4 akış desteği.
* VLC Media Player Intent entegrasyonu (`org.videolan.vlc`).
* M3U / M3U8 ve Xtream Codes API çalma listesi desteği.
* EPG ve 7 günlük Catch-up yayın akışı rehberi.

---

## 🚀 3. GitHub'a Push ve Otomatik Pipeline

Aşağıdaki komutları terminalinizde çalıştırarak projeyi ve güncel APK'yı doğrudan GitHub reponuza gönderebilirsiniz:

```bash
# 1. Ana dalı main olarak belirleyin
git branch -M main

# 2. Değişiklikleri ekleyin ve commit yapın
git add .
git commit -m "feat: release v2.4.0-VK06 with gesture volume/brightness, VOD seekbar, M3U VOD separation and private repo updater"

# 3. GitHub deponuzu bağlayın (Kendi GitHub repo URL'nizi girin):
git remote add origin https://github.com/vyslkrc/streamflow-iptv.git

# 4. Kodları ve APK'yı GitHub'a pushlayın:
git push -u origin main
```

---

## 🔒 4. GitHub Deposu Private (Gizli) İse Güncelleme Alma Rehberi

GitHub reponuz **Private (Gizli)** olarak ayarlandığında, GitHub API anonim isteklere güvenlik nedeniyle `404 Not Found` yanıtı verir. Bunu aşmak için iki basit yöntem mevcuttur:

1. **Yöntem 1 (Önerilen - GitHub Token):**
   * GitHub hesabınızdan **Settings** ➔ **Developer settings** ➔ **Personal access tokens (classic)** bölümüne gidin.
   * **Generate new token** diyerek `repo` iznini işaretleyin ve tokeni kopyalayın.
   * StreamFlow uygulamasında **Ayarlar** ➔ **Özel (Private) Depo Erişimi** alanına tokeninizi yapıştırıp **Kaydet** butonuna basın.
   * Artık uygulama deponuz gizli olsa bile doğrudan GitHub'dan güncellemeleri çekebilecektir.

2. **Yöntem 2 (Doğrudan Tarayıcı ile İndirme):**
   * Uygulamanın Ayarlar sekmesindeki **"Tarayıcıda GitHub Releases Sayfasını Aç"** butonuna dokunun.
   * Telefonunuzun tarayıcısında GitHub oturumunuz açık olduğundan, gizli deponuzdaki en güncel APK'yı tek dokunuşla indirebilirsiniz.

3. **Yöntem 3 (Depoyu Public Yapma):**
   * GitHub reponuzun **Settings** ➔ **Danger Zone** ➔ **Change repository visibility** kısmından depoyu **Public** yaparsanız hiçbir token gerekmeden tüm telefonlar doğrudan güncelleme alabilir.

---

## 🛡️ 5. Android "Sistem Güvenmiyor / Play Protect Engelledi" Uyarısını Geçme Adımları

Google Play Store dışından doğrudan indirilen (sideloaded) tüm açık kaynaklı APK dosyalarında Android ve Google Play Protect sistemi varsayılan olarak *"Geliştirici doğrulanamadı"* veya *"Güvenilmeyen kaynak"* uyarısı çıkarabilir.

Uygulama tamamen açık kaynaklı olup hiçbir zararlı kod içermez. Bu uyarıyı geçerek saniyeler içinde kurulumu tamamlamak için:

1. **Kurulum Ekranında:** Karşınıza *"Play Protect bu uygulamanın geliştiricisini tanımıyor"* veya *"Sistem bu uygulamaya güvenmiyor"* uyarısı çıktığında:
   * Ekrandaki **"Daha fazla ayrıntı" (More details)** yazısına dokunun.
   * Açılan alanda **"Yine de yükle" (Install anyway)** butonuna basın.
2. **Bilinmeyen Kaynak İzni:** Eğer telefonunuz ilk defa APK yüklüyorsa:
   * Çıkan pencerede *"Ayarlar"* butonuna tıklayın.
   * *"Bu kaynaktan izin ver" (Allow from this source)* seçeneğini aktif edip geri dönerek kurulumu tamamlayın.
3. Uygulama hemen kurulacak ve tüm özellikleriyle güvenle çalışacaktır.
