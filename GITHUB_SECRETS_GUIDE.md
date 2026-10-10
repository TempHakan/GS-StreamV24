# 🚀 GitHub Actions CI/CD ve İmzalama Kılavuzu

Bu proje için hazırlanan `.github/workflows/build-release.yml` iş akışı, depoya her `push` yapıldığında otomatik olarak çalışır, APK'yı derler, imzalar ve GitHub Releases üzerinde **Taslak (Draft)** bir sürüm oluşturur.

---

## 🔑 GitHub Secrets (Ortam Gizli Değişkenleri) Yapılandırması

Uygulamanızı kendi özel Android imzalama anahtarınızla imzalamak için GitHub deponuzun **Settings > Secrets and variables > Actions** sayfasına aşağıdaki gizli değişkenleri ekleyebilirsiniz:

| Secret Adı | Açıklama | Örnek Değer |
| :--- | :--- | :--- |
| `RELEASE_KEYSTORE_BASE64` | `.jks` veya `.keystore` dosyanızın Base64 kodlanmış hali | `cat my-key.jks \| base64 -w 0` çıktısı |
| `STORE_PASSWORD` | Keystore anahtar deposu şifresi | `super_secret_password` |
| `KEY_PASSWORD` | Anahtar (alias) şifresi | `super_secret_password` |

> 💡 **Otomatik Fallback (Yedek Mekanizma):**  
> Eğer deponuzda henüz bu secret değişkenleri tanımlanmamışsa, iş akışı derleme hatası vermek yerine otomatik olarak CI release anahtarı üretip APK'yı başarıyla imzalar ve taslak sürümü oluşturur.

---

## ⚙️ İş Akışı Özellikleri

1. **Tetikleyiciler (Triggers):**
   * `main` veya `master` dallarına yapılan her `push`
   * `v*` veya `VK*` etiketleri (Tags)
   * `workflow_dispatch` (GitHub Actions sekmesinden manuel tek tıkla çalıştırma)
2. **JDK & Gradle:**
   * Java 21 (Temurin) ve Gradle 9.3.1
3. **Dinamik Sürüm Çıkarma:**
   * `version.properties` dosyasındaki `VK_TAG`, `VK_CODE` ve `VERSION_BASE` değerlerini otomatik okur.
4. **İmzalı APK ve SHA-256 Hash:**
   * `StreamFlow-IPTV-v{VERSION_BASE}-{VK_TAG}.apk` dosyasını oluşturur ve SHA-256 doğrulama kodunu hesaplar.
5. **Otomatik Taslak Sürüm (Draft Release):**
   * GitHub Releases bölümüne APK'yı ekleyerek bir taslak oluşturur. İnceledikten sonra tek dokunuşla yayınlayabilirsiniz.
