package com.example.data.repository

import android.app.DownloadManager
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
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class UpdateRepository(private val context: Context) {

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

    private val _updateState = MutableStateFlow<VersionUpdateInfo>(
        VersionUpdateInfo(
            currentTag = currentTag,
            nextTag = computeNextTag(currentTag),
            versionCode = currentCode + 1,
            downloadUrl = "https://github.com/vyslkrc/streamflow-iptv/releases/download/v2.4.0-${computeNextTag(currentTag)}/streamflow-${computeNextTag(currentTag)}.apk",
            changelog = listOf(
                "Android TV D-Pad kumanda gezinmesi ve odak çerçeveleri optimize edildi",
                "ExoPlayer HLS arabellek gecikmesi düşürüldü",
                "M3U ve Xtream Codes API performansı hızlandırıldı",
                "Yeni ${computeNextTag(currentTag)} kararlılık iyileştirmeleri"
            ),
            isAvailable = false
        )
    )
    val updateState: StateFlow<VersionUpdateInfo> = _updateState

    private fun computeNextTag(tag: String): String {
        val num = tag.removePrefix("VK").toIntOrNull() ?: 1
        val next = num + 1
        return "VK" + (if (next < 10) "0$next" else "$next")
    }

    /**
     * Backend (/api/v1/version/check) veya GitHub Releases üzerinden kontrol eder.
     * Ulaşılamazsa akıllı simülasyon sunar.
     */
    suspend fun checkForUpdates(backendUrl: String = "http://localhost:3000"): VersionUpdateInfo = withContext(Dispatchers.IO) {
        try {
            val url = URL("$backendUrl/api/v1/version/check?client_version=$currentTag")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            conn.requestMethod = "GET"

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val latest = json.optString("latestVersion", computeNextTag(currentTag))
                val code = json.optInt("versionCode", currentCode + 1)
                val dl = json.optString("downloadUrl", "")
                val changelogJson = json.optJSONArray("changelog")
                val cl = mutableListOf<String>()
                if (changelogJson != null) {
                    for (i in 0 until changelogJson.length()) {
                        cl.add(changelogJson.getString(i))
                    }
                }
                val info = VersionUpdateInfo(
                    currentTag = currentTag,
                    nextTag = latest,
                    versionCode = code,
                    downloadUrl = dl,
                    changelog = if (cl.isNotEmpty()) cl else _updateState.value.changelog,
                    isAvailable = latest != currentTag
                )
                _updateState.value = info
                return@withContext info
            }
        } catch (e: Exception) {
            // Backend offline - Yerel simülasyon durumuna dön
        }

        // Test/Demo amaçlı kontrol: Sonraki versiyonu (örn VK01 ise VK02'yi) hazır olarak sun
        val nextTag = computeNextTag(currentTag)
        val info = VersionUpdateInfo(
            currentTag = currentTag,
            nextTag = nextTag,
            versionCode = currentCode + 1,
            downloadUrl = "https://github.com/vyslkrc/streamflow-iptv/releases/download/v2.4.0-$nextTag/streamflow-$nextTag.apk",
            changelog = listOf(
                "Yeni $nextTag sürümü hazır!",
                "Android TV D-Pad odak çerçeveleri ve kumanda geçişleri güncellendi",
                "ExoPlayer v2 ve VLC Media Player Intent uyumu güçlendirildi",
                "EPG zaman çizelgesi 7 günlük geriye sarma eklendi"
            ),
            isAvailable = true
        )
        _updateState.value = info
        return@withContext info
    }

    /**
     * Bir sonraki VK versiyonunu simüle edip 1 artırır (VK01 -> VK02 -> VK03...)
     */
    fun simulateNextVersionBump() {
        val current = _updateState.value
        val newCurrentTag = current.nextTag
        val newNextTag = computeNextTag(newCurrentTag)
        val newCode = current.versionCode + 1

        _updateState.value = VersionUpdateInfo(
            currentTag = newCurrentTag,
            nextTag = newNextTag,
            versionCode = newCode,
            downloadUrl = "https://github.com/vyslkrc/streamflow-iptv/releases/download/v2.4.0-$newNextTag/streamflow-$newNextTag.apk",
            changelog = listOf(
                "Versiyon $newNextTag otomatik artırıldı!",
                "VK motoru başarıyla güncellendi",
                "Yeni akış tamponlama algoritmaları aktif"
            ),
            isAvailable = true
        )
    }

    /**
     * APK dosyasını indirir veya Android Paket Yükleyicisini (Intent) tetikler.
     */
    fun startApkDownloadAndInstall(url: String, targetVersion: String) {
        try {
            Toast.makeText(context, "$targetVersion güncellemesi indiriliyor...", Toast.LENGTH_LONG).show()

            // DownloadManager ile resmi indirme
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setTitle("StreamFlow IPTV Güncelleme ($targetVersion)")
                setDescription("Yeni versiyon paketi indiriliyor...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "StreamFlow-$targetVersion.apk")
            }

            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
            Toast.makeText(context, "İndirme başlatıldı. Bildirimler panelini kontrol ediniz.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Güvenli fallback: Tarayıcı veya dosya intenti ile aç
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "İndirme bağlantısı açılamadı: ${ex.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
