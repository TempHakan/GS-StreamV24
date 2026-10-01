package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class StreamType {
    LIVE, VOD, SERIES
}

@Entity(tableName = "channels")
data class Channel(
    @PrimaryKey val id: String,
    val name: String,
    val streamUrl: String,
    val logoUrl: String? = null,
    val groupTitle: String = "Genel",
    val tvgId: String? = null,
    val streamType: StreamType = StreamType.LIVE,
    val playlistId: String = "default",
    val isFavorite: Boolean = false,
    val lastWatchedTime: Long = 0L,
    val currentProgram: String? = null,
    val programProgress: Float = 0.45f
)

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey val id: String,
    val name: String,
    val url: String,
    val type: String = "M3U", // M3U, XTREAM, PORTAL, STB
    val channelCount: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis(),
    val xtreamHost: String? = null,
    val xtreamUser: String? = null,
    val xtreamPass: String? = null,
    val macAddress: String? = null
)

data class EpgProgram(
    val id: String,
    val channelId: String,
    val title: String,
    val description: String,
    val startTime: String,
    val endTime: String,
    val progress: Float = 0.0f
)

data class VersionUpdateInfo(
    val currentTag: String,
    val nextTag: String,
    val versionCode: Int,
    val downloadUrl: String,
    val changelog: List<String>,
    val isAvailable: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0.0f
)
