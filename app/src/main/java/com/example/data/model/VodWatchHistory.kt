package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * VOD içeriklerinin izleme geçmişi ve son kalınan pozisyonunu tutan Room Entity'si.
 */
@Entity(tableName = "vod_watch_history")
data class VodWatchHistory(
    @PrimaryKey
    val vodId: String,
    val title: String,
    val streamUrl: String,
    val logoUrl: String? = null,
    val groupTitle: String = "VOD",
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val lastWatchedTimestamp: Long = System.currentTimeMillis()
) {
    /**
     * İzleme ilerleme yüzdesi (0.0f - 1.0f aralığında)
     */
    val progress: Float
        get() = if (durationMs > 0L) {
            (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    /**
     * Kullanıcı dostu okunabilir kalan veya izlenen süre etiketi
     */
    fun formattedProgress(): String {
        val currentSec = positionMs / 1000
        val totalSec = durationMs / 1000
        val currentMin = currentSec / 60
        val totalMin = totalSec / 60
        return if (totalMin > 0) {
            "${currentMin} dk / ${totalMin} dk"
        } else {
            "${currentMin} dk"
        }
    }
}
