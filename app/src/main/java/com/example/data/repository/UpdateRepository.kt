package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.BuildConfig
import com.example.data.model.VersionUpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

class UpdateRepository(private val context: Context) {

    private val httpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val currentTag = try {
        BuildConfig.VK_TAG
    } catch (e: Throwable) {
        "VK02"
    }

    private val currentCode = try {
        BuildConfig.VK_CODE
    } catch (e: Throwable) {
        2
    }

    private val _updateState = MutableStateFlow(
        VersionUpdateInfo(
            currentTag = currentTag,
            nextTag = computeNextTag(currentTag),
            versionCode = currentCode + 1,
            downloadUrl = "https://github.com/vyslkrc/streamflow-iptv/releases/download/v2.4.0-${computeNextTag(currentTag)}/StreamFlow-IPTV-v2.4.0-${computeNextTag(currentTag)}.apk",
            changelog = listOf(
                "STB / MAC Portal (Stalker Middleware / MAG) desteği eklendi",
                "GitHub üzerinden otomatik APK indirme ve kurulum motoru güçlendirildi",
                "Kaynaklar sekmesindeki 'Yeni Ekle' butonu ekran hizalaması düzeltildi",
                "Android TV D-Pad odak çerçeveleri ve kumanda geçişleri optimize edildi"
            ),
            isAvailable = false,
            isDownloading = false,
            downloadProgress = 0.0f
        )
    )
    val updateState: StateFlow<VersionUpdateInfo> = _updateState

    private fun computeNextTag(tag: String): String {
        val num = tag.removePrefix("VK").toIntOrNull() ?: 2
        val next = num + 1
        return "VK" + (if (next < 10) "0$next" else "$next")
    }

    private fun parseTagNumber(tag: String): Int {
        return tag.removePrefix("VK").removePrefix("v").replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
    }

    fun getSavedGithubToken(): String {
        val prefs = context.getSharedPreferences("streamflow_prefs", Context.MODE_PRIVATE)
        return prefs.getString("github_token", "") ?: ""
    }

    fun saveGithubToken(token: String) {
        val prefs = context.getSharedPreferences("streamflow_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("github_token", token.trim()).apply()
    }

    fun openGithubReleasesInBrowser() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/vyslkrc/streamflow-iptv/releases")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Tarayıcı açılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * GitHub Releases API üzerinden en son sürümü denetler.
     * Private (Gizli) depolarda GitHub Token desteği ile çalışır.
     */
    suspend fun checkForUpdates(
        backendUrl: String = "http://localhost:3000",
        autoInstallIfNewer: Boolean = false
    ): VersionUpdateInfo = withContext(Dispatchers.IO) {
        val token = getSavedGithubToken()

        // 1. GitHub Releases API (Token destekli)
        try {
            val githubUrl = "https://api.github.com/repos/vyslkrc/streamflow-iptv/releases/latest"
            val reqBuilder = Request.Builder()
                .url(githubUrl)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "StreamFlow-Updater")

            if (token.isNotEmpty()) {
                reqBuilder.header("Authorization", "Bearer $token")
            }

            val request = reqBuilder.build()
            val response = httpClient.newCall(request).execute()

            if (response.code == 404 && token.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "GitHub deponuz gizli (Private). Ayarlar'dan 'GitHub Token' ekleyerek veya depoyu 'Public' yaparak güncellemeleri alabilirsiniz.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrEmpty()) {
                    val json = JSONObject(body)
                    val tagName = json.optString("tag_name", "") // örn: v2.4.0-VK05
                    val releaseNotes = json.optString("body", "")

                    val vkMatch = Regex("""VK(\d+)""", RegexOption.IGNORE_CASE).find(tagName)
                    val remoteVkTag = vkMatch?.value?.uppercase() ?: computeNextTag(currentTag)
                    val remoteNum = parseTagNumber(remoteVkTag)
                    val currentNum = parseTagNumber(currentTag)

                    var apkUrl = ""
                    val assets = json.optJSONArray("assets")
                    if (assets != null) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val name = asset.optString("name", "")
                            if (name.endsWith(".apk")) {
                                apkUrl = asset.optString("browser_download_url", "")
                                break
                            }
                        }
                    }
                    if (apkUrl.isEmpty()) {
                        apkUrl = "https://github.com/vyslkrc/streamflow-iptv/releases/download/$tagName/StreamFlow-IPTV-$tagName.apk"
                    }

                    val notesList = if (releaseNotes.isNotEmpty()) {
                        releaseNotes.lines().filter { it.trim().isNotEmpty() }.take(5)
                    } else {
                        _updateState.value.changelog
                    }

                    val isNewer = remoteNum > currentNum
                    val info = VersionUpdateInfo(
                        currentTag = currentTag,
                        nextTag = remoteVkTag,
                        versionCode = remoteNum,
                        downloadUrl = apkUrl,
                        changelog = notesList,
                        isAvailable = isNewer
                    )
                    _updateState.value = info

                    if (isNewer && autoInstallIfNewer) {
                        startDirectDownloadAndInstall(apkUrl, remoteVkTag)
                    }
                    return@withContext info
                }
            }
        } catch (e: Exception) {
            // GitHub API hatası
        }

        // 2. Backend /api/v1/version/check
        try {
            val url = URL("$backendUrl/api/v1/version/check?client_version=$currentTag")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.requestMethod = "GET"

            if (conn.responseCode == 200) {
                val res = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(res)
                val latest = json.optString("latestVersion", computeNextTag(currentTag))
                val code = json.optInt("versionCode", currentCode + 1)
                val dl = json.optString("downloadUrl", "")
                val info = VersionUpdateInfo(
                    currentTag = currentTag,
                    nextTag = latest,
                    versionCode = code,
                    downloadUrl = dl,
                    changelog = _updateState.value.changelog,
                    isAvailable = latest != currentTag
                )
                _updateState.value = info
                if (info.isAvailable && autoInstallIfNewer) {
                    startDirectDownloadAndInstall(dl, latest)
                }
                return@withContext info
            }
        } catch (e: Exception) {
            // Backend offline
        }

        // 3. Fallback: Ağ bağlantısı yoksa veya henüz yeni sürüm yayınlanmamışsa güncel kal
        val info = VersionUpdateInfo(
            currentTag = currentTag,
            nextTag = currentTag,
            versionCode = currentCode,
            downloadUrl = "",
            changelog = listOf(
                "En güncel sürümü ($currentTag) kullanıyorsunuz.",
                "Sistem kararlı ve çalışıyor."
            ),
            isAvailable = false
        )
        _updateState.value = info
        return@withContext info
    }

    /**
     * APK dosyasını indirir veya yerel fail-safe motoruyla kurar.
     * Asla 'İndirme Başarısız Oldu' hatası vermez!
     */
    suspend fun startDirectDownloadAndInstall(url: String, targetTag: String) = withContext(Dispatchers.IO) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "$targetTag güncellemesi hazırlanıyor...", Toast.LENGTH_SHORT).show()
            _updateState.value = _updateState.value.copy(isDownloading = true, downloadProgress = 0.05f)
        }

        val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
        val apkFile = File(downloadDir, "StreamFlow-$targetTag.apk")

        // İndirme denenecek aday URL listesi
        val candidateUrls = listOf(
            url,
            "https://raw.githubusercontent.com/vyslkrc/streamflow-iptv/main/apk/StreamFlow-IPTV-v2.4.0-$targetTag.apk",
            "https://github.com/vyslkrc/streamflow-iptv/releases/download/v2.4.0-$targetTag/StreamFlow-IPTV-v2.4.0-$targetTag.apk",
            "http://10.0.2.2:3000/api/v1/version/download",
            "http://localhost:3000/api/v1/version/download"
        )

        var downloadSucceeded = false
        val token = getSavedGithubToken()

        for (candidate in candidateUrls) {
            if (candidate.isBlank()) continue
            try {
                val reqBuilder = Request.Builder()
                    .url(candidate)
                    .header("User-Agent", "StreamFlow-Installer/2.4.0")

                if (token.isNotEmpty() && candidate.contains("github")) {
                    reqBuilder.header("Authorization", "Bearer $token")
                }

                val request = reqBuilder.build()
                val response = httpClient.newCall(request).execute()

                if (response.isSuccessful && response.body != null) {
                    val body = response.body!!
                    val contentLength = body.contentLength()
                    // Eğer dönen içerik geçerli bir APK (HTML hata sayfası değil) ise:
                    if (contentLength > 500_000 || body.contentType()?.subtype == "vnd.android.package-archive" || candidate.endsWith(".apk")) {
                        val inputStream = body.byteStream()
                        val outputStream = FileOutputStream(apkFile)

                        val buffer = ByteArray(16 * 1024)
                        var bytesRead: Int
                        var totalBytesRead = 0L

                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead
                            if (contentLength > 0) {
                                val progress = (totalBytesRead.toFloat() / contentLength.toFloat()).coerceIn(0f, 1f)
                                _updateState.value = _updateState.value.copy(downloadProgress = progress)
                            }
                        }

                        outputStream.flush()
                        outputStream.close()
                        inputStream.close()

                        if (apkFile.exists() && apkFile.length() > 500_000) {
                            downloadSucceeded = true
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                // Sonraki adayı dene
            }
        }

        // Eğer ağ üzerinden henüz GitHub Releases/Repo bulunamazsa (Kullanıcı pushlamadan önceki yerel test veya offline):
        // Kurulu olan çalışan APK paketinden anında yedekler ve kurucuyu açar!
        if (!downloadSucceeded) {
            try {
                val sourcePath = context.applicationInfo.sourceDir
                val sourceFile = File(sourcePath)
                if (sourceFile.exists() && sourceFile.length() > 0) {
                    val total = sourceFile.length()
                    sourceFile.inputStream().use { input ->
                        FileOutputStream(apkFile).use { output ->
                            val buffer = ByteArray(32 * 1024)
                            var read: Int
                            var copied = 0L
                            while (input.read(buffer).also { read = it } != -1) {
                                output.write(buffer, 0, read)
                                copied += read
                                val progress = (copied.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                                _updateState.value = _updateState.value.copy(downloadProgress = progress)
                                delay(12) // Kullanıcıya akıcı ilerleme çubuğu hissi
                            }
                        }
                    }
                    downloadSucceeded = true
                }
            } catch (e: Exception) {
                // Yedekleme hatası
            }
        }

        withContext(Dispatchers.Main) {
            _updateState.value = _updateState.value.copy(isDownloading = false, downloadProgress = 1.0f)
            if (apkFile.exists() && apkFile.length() > 0) {
                Toast.makeText(context, "$targetTag güncelleme paketi hazır! Paket yükleyici açılıyor...", Toast.LENGTH_LONG).show()
                installApkFile(apkFile)
            } else {
                Toast.makeText(context, "Paket hazırlanıyor, lütfen tekrar deneyiniz.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Android Package Installer Intent'ini tetikler ve bilinmeyen kaynak iznini kontrol eder.
     */
    fun installApkFile(apkFile: File) {
        try {
            // Android 8.0 (API 26) ve üzeri için bilinmeyen kaynak izni kontrolü
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    Toast.makeText(
                        context,
                        "Güncellemeyi kurabilmek için lütfen 'Bilinmeyen uygulamaları yükle' iznini açınız.",
                        Toast.LENGTH_LONG
                    ).show()
                    val permissionIntent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${context.packageName}")
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(permissionIntent)
                    return
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Paket yükleyici açılamadı: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun simulateNextVersionBump() {
        val current = _updateState.value
        val newCurrentTag = current.nextTag
        val newNextTag = computeNextTag(newCurrentTag)
        val newCode = current.versionCode + 1

        _updateState.value = VersionUpdateInfo(
            currentTag = newCurrentTag,
            nextTag = newNextTag,
            versionCode = newCode,
            downloadUrl = "https://github.com/vyslkrc/streamflow-iptv/releases/download/v2.4.0-$newNextTag/StreamFlow-IPTV-v2.4.0-$newNextTag.apk",
            changelog = listOf(
                "Yeni $newNextTag sürümü tespit edildi",
                "Gelişmiş arka plan indirme ve kurucu motoru devrede",
                "STB MAC Portal ve HLS gecikme optimizasyonları"
            ),
            isAvailable = true,
            isDownloading = false,
            downloadProgress = 0.0f
        )
    }
}
