package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Channel
import com.example.data.model.Playlist
import com.example.data.model.StreamType
import com.example.data.model.VersionUpdateInfo
import com.example.data.repository.ChannelRepository
import com.example.data.repository.UpdateRepository
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class StreamUiState(
    val liveChannels: List<Channel> = emptyList(),
    val vodChannels: List<Channel> = emptyList(),
    val favoriteChannels: List<Channel> = emptyList(),
    val recentChannels: List<Channel> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val activeChannel: Channel? = null,
    val isPlaying: Boolean = false,
    val themeMode: AppThemeMode = AppThemeMode.DARK_OLED,
    val updateInfo: VersionUpdateInfo? = null,
    val showUpdateDialog: Boolean = false,
    val showAddSourceDialog: Boolean = false,
    val showEpgDialog: Boolean = false,
    val epgChannel: Channel? = null,
    val playlists: List<Playlist> = emptyList(),
    val backendUrl: String = "http://localhost:3000",
    val useProxy: Boolean = false,
    val defaultPlayerIsVlc: Boolean = false,
    val autoInstallUpdates: Boolean = true,
    val selectedPlaylistId: String? = null,
    val isPlayerMuted: Boolean = false,
    val aspectRatioMode: Int = 0 // 0: FIT, 1: ZOOM/FILL, 2: 16:9, 3: 4:3
)

class StreamViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val channelRepository = ChannelRepository(db.channelDao(), db.playlistDao())
    private val updateRepository = UpdateRepository(application)

    private val _uiState = MutableStateFlow(StreamUiState())
    val uiState: StateFlow<StreamUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            channelRepository.seedInitialChannelsIfEmpty()
        }

        // Açılışta GitHub Releases üzerinden arka planda güncelleme kontrolü
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            val info = updateRepository.checkForUpdates(
                backendUrl = _uiState.value.backendUrl,
                autoInstallIfNewer = false
            )
            if (info.isAvailable) {
                _uiState.update { it.copy(showUpdateDialog = true) }
            }
        }

        // Live channels flow
        viewModelScope.launch {
            channelRepository.getChannelsByType(StreamType.LIVE).collect { list ->
                _uiState.update { state ->
                    val categories = list.map { it.groupTitle }.distinct().sorted()
                    state.copy(
                        liveChannels = list,
                        categories = categories,
                        activeChannel = state.activeChannel ?: list.firstOrNull()
                    )
                }
            }
        }

        // VOD flow
        viewModelScope.launch {
            channelRepository.getChannelsByType(StreamType.VOD).collect { list ->
                _uiState.update { it.copy(vodChannels = list) }
            }
        }

        // Favorites flow
        viewModelScope.launch {
            channelRepository.favoriteChannels.collect { list ->
                _uiState.update { it.copy(favoriteChannels = list) }
            }
        }

        // Recents flow
        viewModelScope.launch {
            channelRepository.recentChannels.collect { list ->
                _uiState.update { it.copy(recentChannels = list) }
            }
        }

        // Playlists flow
        viewModelScope.launch {
            channelRepository.allPlaylists.collect { list ->
                _uiState.update { it.copy(playlists = list) }
            }
        }

        // Update state flow
        viewModelScope.launch {
            updateRepository.updateState.collect { info ->
                _uiState.update { it.copy(updateInfo = info) }
            }
        }
    }

    fun playChannel(channel: Channel) {
        viewModelScope.launch {
            channelRepository.updateLastWatched(channel.id)
            _uiState.update { it.copy(activeChannel = channel, isPlaying = true) }
        }
    }

    fun toggleFavorite(channel: Channel) {
        viewModelScope.launch {
            channelRepository.toggleFavorite(channel.id, !channel.isFavorite)
        }
    }

    fun selectCategory(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun selectPlaylist(playlistId: String?) {
        _uiState.update { it.copy(selectedPlaylistId = playlistId, selectedCategory = null) }
    }

    fun togglePlayerMute() {
        _uiState.update { it.copy(isPlayerMuted = !it.isPlayerMuted) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun cycleAspectRatio() {
        _uiState.update { it.copy(aspectRatioMode = (it.aspectRatioMode + 1) % 4) }
    }

    fun setThemeMode(mode: AppThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setProxyEnabled(enabled: Boolean) {
        _uiState.update { it.copy(useProxy = enabled) }
    }

    fun setDefaultPlayerIsVlc(isVlc: Boolean) {
        _uiState.update { it.copy(defaultPlayerIsVlc = isVlc) }
    }

    fun setBackendUrl(url: String) {
        _uiState.update { it.copy(backendUrl = url) }
    }

    fun setAutoInstallUpdates(enabled: Boolean) {
        _uiState.update { it.copy(autoInstallUpdates = enabled) }
    }

    fun showUpdateDialog(show: Boolean) {
        _uiState.update { it.copy(showUpdateDialog = show) }
    }

    fun showAddSourceDialog(show: Boolean) {
        _uiState.update { it.copy(showAddSourceDialog = show) }
    }

    fun showEpgDialog(channel: Channel?) {
        _uiState.update { it.copy(showEpgDialog = channel != null, epgChannel = channel) }
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            val info = updateRepository.checkForUpdates(_uiState.value.backendUrl, autoInstallIfNewer = false)
            if (info.isAvailable) {
                _uiState.update { it.copy(showUpdateDialog = true) }
            }
        }
    }

    fun simulateVersionBump() {
        updateRepository.simulateNextVersionBump()
        _uiState.update { it.copy(showUpdateDialog = true) }
    }

    fun downloadAndInstallUpdate(url: String, tag: String) {
        viewModelScope.launch {
            updateRepository.startDirectDownloadAndInstall(url, tag)
            _uiState.update { it.copy(showUpdateDialog = false) }
        }
    }

    fun importM3uPlaylist(name: String, url: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val count = channelRepository.importM3uFromUrl(name, url)
            onComplete(count)
        }
    }

    fun importStbPortal(name: String, portalUrl: String, mac: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val count = channelRepository.importStbPortal(name, portalUrl, mac)
            onComplete(count)
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            channelRepository.deletePlaylist(playlist)
        }
    }

    /**
     * Akış URL'sini seçili moda göre döner:
     * - Doğrudan URL
     * - Backend Relay Proxy üzerinden tünellenmiş URL
     */
    fun resolveEffectiveStreamUrl(rawUrl: String): String {
        return if (_uiState.value.useProxy) {
            val backend = _uiState.value.backendUrl
            "$backend/api/v1/proxy/stream?url=${java.net.URLEncoder.encode(rawUrl, "UTF-8")}"
        } else {
            rawUrl
        }
    }
}
