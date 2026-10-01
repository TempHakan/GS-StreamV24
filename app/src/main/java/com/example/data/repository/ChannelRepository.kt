package com.example.data.repository

import com.example.data.local.ChannelDao
import com.example.data.local.PlaylistDao
import com.example.data.model.Channel
import com.example.data.model.Playlist
import com.example.data.model.StreamType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.UUID

class ChannelRepository(
    private val channelDao: ChannelDao,
    private val playlistDao: PlaylistDao
) {
    private val httpClient = OkHttpClient.Builder().build()

    val allChannels: Flow<List<Channel>> = channelDao.getAllChannels()
    val favoriteChannels: Flow<List<Channel>> = channelDao.getFavoriteChannels()
    val recentChannels: Flow<List<Channel>> = channelDao.getRecentChannels()
    val allPlaylists: Flow<List<Playlist>> = playlistDao.getAllPlaylists()

    fun getChannelsByType(type: StreamType): Flow<List<Channel>> = channelDao.getChannelsByType(type)
    fun getCategories(type: StreamType): Flow<List<String>> = channelDao.getCategories(type)

    suspend fun toggleFavorite(channelId: String, isFavorite: Boolean) {
        channelDao.toggleFavorite(channelId, isFavorite)
    }

    suspend fun updateLastWatched(channelId: String) {
        channelDao.updateLastWatched(channelId, System.currentTimeMillis())
    }

    suspend fun seedInitialChannelsIfEmpty() = withContext(Dispatchers.IO) {
        val sampleChannels = listOf(
            Channel(
                id = "ch_demo_1",
                name = "TRT 1 HD (Canlı)",
                streamUrl = "https://tv-trt1.medya.trt.com.tr/master.m3u8",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e0/TRT_1_logo.svg/300px-TRT_1_logo.svg.png",
                groupTitle = "Ulusal",
                streamType = StreamType.LIVE,
                currentProgram = "Ana Haber Bülteni",
                programProgress = 0.65f
            ),
            Channel(
                id = "ch_demo_2",
                name = "TRT Haber (Canlı)",
                streamUrl = "https://tv-trthaber.medya.trt.com.tr/master.m3u8",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6b/TRT_Haber_logo.svg/300px-TRT_Haber_logo.svg.png",
                groupTitle = "Haber",
                streamType = StreamType.LIVE,
                currentProgram = "Dünya Gündemi ve Canlı Bağlantılar",
                programProgress = 0.30f
            ),
            Channel(
                id = "ch_demo_3",
                name = "TRT Belgesel (Canlı)",
                streamUrl = "https://tv-trtbelgesel.medya.trt.com.tr/master.m3u8",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/TRT_Belgesel_logo.svg/300px-TRT_Belgesel_logo.svg.png",
                groupTitle = "Belgesel",
                streamType = StreamType.LIVE,
                currentProgram = "Doğanın Harikaları & Vahşi Yaşam",
                programProgress = 0.80f
            ),
            Channel(
                id = "ch_demo_4",
                name = "TRT Spor (Canlı)",
                streamUrl = "https://tv-trtsporyildiz.medya.trt.com.tr/master.m3u8",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f6/TRT_Spor_logo.svg/300px-TRT_Spor_logo.svg.png",
                groupTitle = "Spor",
                streamType = StreamType.LIVE,
                currentProgram = "Spor Manşet & Maç Özetleri",
                programProgress = 0.45f
            ),
            Channel(
                id = "ch_demo_5",
                name = "NASA TV Ultra HD (HLS)",
                streamUrl = "https://ntv1.akamaized.net/hls/live/2014075/NASA-NTV1-HLS/master.m3u8",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e5/NASA_logo.svg/300px-NASA_logo.svg.png",
                groupTitle = "Bilim & Uzay",
                streamType = StreamType.LIVE,
                currentProgram = "ISS Live Space Station View",
                programProgress = 0.50f
            ),
            Channel(
                id = "ch_demo_6",
                name = "Red Bull TV Live",
                streamUrl = "https://rbmn-live.akamaized.net/hls/live/590964/BoRB-AT/master.m3u8",
                logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/f/f5/Red_Bull_TV_logo.svg/300px-Red_Bull_TV_logo.svg.png",
                groupTitle = "Aksiyon & Spor",
                streamType = StreamType.LIVE,
                currentProgram = "Extreme Sports & Downhill World Cup",
                programProgress = 0.20f
            ),
            // VOD Örnekleri
            Channel(
                id = "vod_demo_1",
                name = "Big Buck Bunny (4K Film)",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Big_buck_bunny_poster_big.jpg/300px-Big_buck_bunny_poster_big.jpg",
                groupTitle = "Animasyon",
                streamType = StreamType.VOD,
                currentProgram = "Süre: 10 dk | 1080p Full HD"
            ),
            Channel(
                id = "vod_demo_2",
                name = "Tears of Steel (Sci-Fi Film)",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/2/29/Tears_of_Steel_poster.jpg/300px-Tears_of_Steel_poster.jpg",
                groupTitle = "Bilim Kurgu",
                streamType = StreamType.VOD,
                currentProgram = "Süre: 12 dk | 4K Ultra HD"
            ),
            Channel(
                id = "vod_demo_3",
                name = "Sintel (Açık Kaynak Film)",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8f/Sintel_poster.jpg/300px-Sintel_poster.jpg",
                groupTitle = "Macera",
                streamType = StreamType.VOD,
                currentProgram = "Süre: 15 dk | 1080p HD"
            )
        )

        channelDao.insertChannels(sampleChannels)

        val defaultPlaylist = Playlist(
            id = "default",
            name = "StreamFlow Başlangıç Paketi",
            url = "https://streamflow.iptv/default.m3u",
            type = "M3U",
            channelCount = sampleChannels.size
        )
        playlistDao.insertPlaylist(defaultPlaylist)
    }

    suspend fun importM3uFromUrl(playlistName: String, url: String) = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "IPTVSmarters/1.0 (StreamFlow)")
            .build()
        val response = httpClient.newCall(request).execute()
        val content = response.body?.string() ?: return@withContext 0
        return@withContext parseAndSaveM3uContent(playlistName, url, content)
    }

    suspend fun parseAndSaveM3uContent(playlistName: String, url: String, content: String): Int = withContext(Dispatchers.IO) {
        val playlistId = UUID.randomUUID().toString()
        val lines = content.lines()
        val channels = mutableListOf<Channel>()
        var currentTvgName = ""
        var currentTvgLogo: String? = null
        var currentGroup = "Genel"
        var currentName = ""

        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("#EXTINF:")) {
                // tvg-logo
                val logoMatch = Regex("""tvg-logo="([^"]*)"""", RegexOption.IGNORE_CASE).find(line)
                currentTvgLogo = logoMatch?.groupValues?.get(1)

                // group-title
                val groupMatch = Regex("""group-title="([^"]*)"""", RegexOption.IGNORE_CASE).find(line)
                currentGroup = groupMatch?.groupValues?.get(1) ?: "Genel"

                // name
                val commaIdx = line.lastIndexOf(',')
                currentName = if (commaIdx != -1) line.substring(commaIdx + 1).trim() else "Kanal ${channels.size + 1}"
            } else if (line.isNotEmpty() && !line.startsWith("#")) {
                val streamUrl = line
                val isVod = streamUrl.contains(".mp4") || streamUrl.contains("/movie/")
                val channel = Channel(
                    id = UUID.randomUUID().toString(),
                    name = if (currentName.isNotEmpty()) currentName else "Kanal ${channels.size + 1}",
                    streamUrl = streamUrl,
                    logoUrl = currentTvgLogo,
                    groupTitle = currentGroup,
                    streamType = if (isVod) StreamType.VOD else StreamType.LIVE,
                    playlistId = playlistId
                )
                channels.add(channel)
                currentName = ""
                currentTvgLogo = null
                currentGroup = "Genel"
            }
            i++
        }

        if (channels.isNotEmpty()) {
            channelDao.insertChannels(channels)
            val playlist = Playlist(
                id = playlistId,
                name = playlistName,
                url = url,
                type = "M3U",
                channelCount = channels.size
            )
            playlistDao.insertPlaylist(playlist)
        }
        channels.size
    }

    suspend fun deletePlaylist(playlist: Playlist) = withContext(Dispatchers.IO) {
        channelDao.deleteByPlaylistId(playlist.id)
        playlistDao.deletePlaylist(playlist)
    }
}
