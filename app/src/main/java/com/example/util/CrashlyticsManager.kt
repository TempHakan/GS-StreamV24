package com.example.util

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Firebase Crashlytics yöneticisi:
 * - Çalışma zamanı (runtime) istisnalarını ve çökmelerini kaydeder.
 * - Yapı ve sürüm ayrıntılarını (VK_TAG, VK_CODE, model, Android sürümü) etiketler.
 * - Hata ayıklama ve sistem kararlılığını izlemek için ekmek kırıntıları (breadcrumbs) bırakır.
 */
object CrashlyticsManager {
    private const val TAG = "CrashlyticsManager"
    private var isInitialized = false
    private var isCrashlyticsAvailable = false

    // Arayüzde (Ayarlar ekranında) görüntülenebilir son çalışma zamanı logları
    private val _recentLogs = CopyOnWriteArrayList<String>()
    val recentLogs: List<String> get() = _recentLogs.takeLast(25)

    fun init(context: Context) {
        if (isInitialized) return
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.setCrashlyticsCollectionEnabled(true)

            // Yapı ve Sürüm Görünürlüğü (Build Visibility)
            crashlytics.setCustomKey("vk_tag", BuildConfig.VK_TAG)
            crashlytics.setCustomKey("vk_code", BuildConfig.VK_CODE)
            crashlytics.setCustomKey("version_name", BuildConfig.APP_VERSION_NAME)
            crashlytics.setCustomKey("device_manufacturer", Build.MANUFACTURER)
            crashlytics.setCustomKey("device_model", Build.MODEL)
            crashlytics.setCustomKey("android_release", Build.VERSION.RELEASE)
            crashlytics.setCustomKey("android_sdk", Build.VERSION.SDK_INT)

            crashlytics.log("StreamFlow IPTV started - Version: ${BuildConfig.APP_VERSION_NAME}")
            isCrashlyticsAvailable = true
            log("Crashlytics aktif edildi. [${BuildConfig.VK_TAG}]")
        } catch (e: Throwable) {
            Log.w(TAG, "Crashlytics başlatma notu: ${e.message}")
            isCrashlyticsAvailable = false
        }
        isInitialized = true
        setupGlobalExceptionHandler()
    }

    /**
     * Ekmek kırıntısı (Breadcrumb) günlüğü kaydeder.
     */
    fun log(message: String) {
        val entry = "${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())} $message"
        _recentLogs.add(entry)
        Log.d(TAG, message)
        if (isCrashlyticsAvailable) {
            try {
                FirebaseCrashlytics.getInstance().log(message)
            } catch (t: Throwable) {
                // Güvenli fallback
            }
        }
    }

    /**
     * Ölümcül olmayan (non-fatal) istisnaları Crashlytics'e raporlar.
     */
    fun recordException(throwable: Throwable, tag: String? = null, extra: Map<String, Any>? = null) {
        val msg = "Hata [${tag ?: "Genel"}]: ${throwable.message ?: throwable.javaClass.simpleName}"
        log("⚠️ $msg")
        if (isCrashlyticsAvailable) {
            try {
                val crashlytics = FirebaseCrashlytics.getInstance()
                tag?.let { crashlytics.setCustomKey("error_tag", it) }
                extra?.forEach { (key, value) ->
                    when (value) {
                        is Boolean -> crashlytics.setCustomKey(key, value)
                        is Int -> crashlytics.setCustomKey(key, value)
                        else -> crashlytics.setCustomKey(key, value.toString())
                    }
                }
                crashlytics.recordException(throwable)
            } catch (t: Throwable) {
                Log.e(TAG, "Crashlytics istisna kaydı başarısız: ${t.message}")
            }
        }
    }

    fun setCustomKey(key: String, value: String) {
        if (isCrashlyticsAvailable) {
            try {
                FirebaseCrashlytics.getInstance().setCustomKey(key, value)
            } catch (t: Throwable) {}
        }
    }

    fun setCustomKey(key: String, value: Boolean) {
        if (isCrashlyticsAvailable) {
            try {
                FirebaseCrashlytics.getInstance().setCustomKey(key, value)
            } catch (t: Throwable) {}
        }
    }

    fun setCollectionEnabled(enabled: Boolean) {
        if (isCrashlyticsAvailable) {
            try {
                FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enabled)
                log("Crashlytics veri toplama durumu: $enabled")
            } catch (t: Throwable) {}
        }
    }

    fun isAvailable(): Boolean = isCrashlyticsAvailable

    /**
     * Yakalanmamış küresel istisnaları (Uncaught Exceptions) dinler ve Crashlytics'e iletir.
     */
    private fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            log("🚨 KRİTİK ÇÖKME (${thread.name}): ${throwable.message}")
            if (isCrashlyticsAvailable) {
                try {
                    FirebaseCrashlytics.getInstance().setCustomKey("crashed_thread", thread.name)
                    FirebaseCrashlytics.getInstance().recordException(throwable)
                } catch (t: Throwable) {}
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
