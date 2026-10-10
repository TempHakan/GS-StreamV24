package com.example

import android.app.Application
import com.example.util.CrashlyticsManager

class StreamFlowApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Firebase Crashlytics kararlılık izleme motorunu başlat
        CrashlyticsManager.init(this)
        CrashlyticsManager.log("StreamFlowApp başlatıldı.")
    }
}
