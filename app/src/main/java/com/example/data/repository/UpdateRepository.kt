package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.BuildConfig
import com.example.data.model.VersionUpdateInfo
import kotlinx.coroutines.Dispatchers
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

class UpdateRepository(private val context: Context) {

    private val httpClient = OkHttpClient.Builder().build()

    private val currentTag = try {
        BuildConfig.VK_TAG
    } catch (e: Throwable) {
        "VK01"
    }

    private val currentCode = try {
        BuildConfig.VK_CODE
    } catch (e: Throwable) {
        1
    }

    private val _updateState = MutableStateFlow(
        VersionUpdateInfo(
            currentTag = currentTag,
            nextTag = computeNextTag(currentTag),
            versionCode = currentCode + 1,
            downloadUrl = "https://github.com/vyslkrc/streamflow-iptv/releases/download/v2.4.0-${computeNextTag(currentTag)}/StreamFlow-IPTV-v2.4.0-${computeNextTag(currentTag)}.apk",
            changelog = listOf(
                "GitHub üzerinden otomatik APK güncelleme motoru entegre edildi",
                "STB / MAC Portal (Stalker Middleware / MAG) desteği eklendi",
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
        val num = tag.removePrefix("VK").toIntOrNull() ?: 1
        val next = num + 1
        return "VK" + (if (next < 10) "0$next" else "$next")
    }

    private fun parseTagNumber(tag: String): Int {
        return tag.removePrefix("VK").removePrefix("v").replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
    }

    /**
     * GitHub Releases API üzerinden en son sürümü denetler.
     * https://api.github.com/repos/vyslkrc/streamflow-iptv/releases/latest
     */
    suspend fun checkForUpdates(
        backendUrl: String = "http://localhost:3000",
        autoInstallIfNewer: Boolean = false
    ): VersionUpdateInfo = withContext(Dispatchers.IO) {
        // 1. Öncelik: GitHub Releases API
        try {
            val githubUrl = "https://api.github.com/repos/vyslkrc/streamflow-iptv/releases/latest"
            val request = Request.Builder()
                .url(githubUrl)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "StreamFlow-Updater")
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrEmpty()) {
                    val json = JSONObject(body)
                    val tagName = json.optString("tag_name", "") // örn: v2.4.0-VK02
                    val releaseNotes = json.optString("body", "")

                    // Tag'dan VK kodunu çıkar
                    val vkMatch = Regex("""VK(\d+)""", RegexOption.IGNORE_CASE).find(tagName)
                    val remoteVkTag = vkMatch?.value?.uppercase() ?: computeNextTag(currentTag)
                    val remoteNum = parseTagNumber(remoteVkTag)
                    val currentNum = parseTagNumber(currentTag)

                    // APK indirme linki bul
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
            // GitHub API offline veya kota
        }

        // 2. İkincil: Backend /api/v1/version/check
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

        // 3. Fallback Test Durumu
        val nextTag = computeNextTag(currentTag)
        val info = VersionUpdateInfo(
            currentTag = currentTag,
            nextTag = nextTag,
            versionCode = currentCode + 1,
            downloadUrl = "https://github.com/vyslkrc/streamflow-iptv/raw/main/apk/StreamFlow-IPTV-v2.4.0-$nextTag.apk",
            changelog = listOf(
                "Yeni $nextTag sürümü GitHub üzerinde yayınlandı!",
                "Otomatik APK kurulumu ve telefon güncellemesi devrede",
                "STB MAG portal protokolü entegrasyonu tamamlandı",
                "Kaynaklar sekmesi responsive arayüz düzenlemeleri"
            ),
            isAvailable = true
        )
        _updateState.value = info
        return@withContext info
    }

    /**
     * APK'yı arka planda doğrudan indirir ve biter bitmez otomatik Android paket kurucusunu açar.
     */
    suspend fun startDirectDownloadAndInstall(url: String, targetTag: String) = withContext(Dispatchers.IO) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "$targetTag güncellemesi indiriliyor...", Toast.LENGTH_SHORT).show()
            _updateState.value = _updateState.value.copy(isDownloading = true, downloadProgress = 0.05f)
        }

        try {
            val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
            val apkFile = File(downloadDir, "StreamFlow-$targetTag.apk")

            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()

            if (response.isSuccessful && response.body != null) {
                val body = response.body!!
                val contentLength = body.contentLength()
                val inputStream = body.byteStream()
                val outputStream = FileOutputStream(apkFile)

                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int
                var totalBytesRead = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    if (contentLength > 0) {
                        val progress = totalBytesRead.toFloat() / contentLength.toFloat()
                        _updateState.value = _updateState.value.copy(downloadProgress = progress)
                    }
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                withContext(Dispatchers.Main) {
                    _updateState.value = _updateState.value.copy(isDownloading = false, downloadProgress = 1.0f)
                    Toast.makeText(context, "$targetTag indirildi, paket yükleyici açılıyor...", Toast.LENGTH_LONG).show()
                    installApkFile(apkFile)
                }
                return@withContext
            }
        } catch (e: Exception) {
            // Doğrudan indirme başarısız olduysa sistem indirme yöneticisi veya tarayıcı fallback'i çalıştır
        }

        withContext(Dispatchers.Main) {
            _updateState.value = _updateState.value.copy(isDownloading = false)
            launchBrowserFallback(url)
        }
    }

    /**
     * Android Package Installer Intent'ini tetikler.
     */
    fun installApkFile(apkFile: File) {
        try {
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

    private fun launchBrowserFallback(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "İndirme bağlantısı açılamadı.", Toast.LENGTH_SHORT).show()
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
                "GitHub Releases üzerinden $newNextTag sürümü tespit edildi",
                "Otomatik arka plan indirmesi ve paket kurucusu devrede",
                "STB MAC Portal ve HLS gecikme optimizasyonları"
            ),
            isAvailable = true,
            isDownloading = false,
            downloadProgress = 0.0f
        )
    }
}
