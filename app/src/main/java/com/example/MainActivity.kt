package com.example

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.player.VlcIntentHelper
import com.example.ui.components.AddSourceDialog
import com.example.ui.components.EpgGuideDialog
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.UpdateDialog
import com.example.ui.screens.*
import com.example.ui.theme.StreamFlowTheme
import com.example.viewmodel.StreamViewModel

class MainActivity : ComponentActivity() {

    private val isPipModeState = mutableStateOf(false)
    private var streamViewModelRef: StreamViewModel? = null

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isPipModeState.value = isInPictureInPictureMode
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Kullanıcı ana ekrana döndüğünde yayın açıksa otomatik PiP moduna geç
        val activeChannel = streamViewModelRef?.uiState?.value?.activeChannel
        if (activeChannel != null) {
            enterPictureInPicture()
        }
    }

    fun enterPictureInPicture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val aspectRatio = Rational(16, 9)
                val builder = PictureInPictureParams.Builder()
                    .setAspectRatio(aspectRatio)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    builder.setAutoEnterEnabled(true)
                }

                enterPictureInPictureMode(builder.build())
            } catch (e: Exception) {
                Toast.makeText(this, "PiP başlatılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Cihazınız Picture-in-Picture modunu desteklemiyor.", Toast.LENGTH_SHORT).show()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: StreamViewModel = viewModel()
            streamViewModelRef = viewModel
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val context = LocalContext.current
            val activity = context as? Activity
            val isInPip by isPipModeState

            var currentTabIndex by remember { mutableIntStateOf(0) }

            // Tam Ekran Sürükleyici Mod (Immersive Mode): Sistem barlarını gizler
            DisposableEffect(state.isFullscreen) {
                val window = activity?.window
                if (window != null) {
                    try {
                        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                        if (state.isFullscreen) {
                            insetsController.hide(WindowInsetsCompat.Type.systemBars())
                            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        } else {
                            insetsController.show(WindowInsetsCompat.Type.systemBars())
                        }
                    } catch (e: Exception) {
                        // Safe fallback on virtual devices
                    }
                }
                onDispose {
                    val window = activity?.window
                    if (window != null) {
                        try {
                            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                            insetsController.show(WindowInsetsCompat.Type.systemBars())
                        } catch (e: Exception) {
                            // Safe fallback
                        }
                    }
                }
            }

            // Tam ekrandan çıkış için geri tuşu dinleyicisi
            BackHandler(enabled = state.isFullscreen) {
                viewModel.setFullscreen(false)
            }

            StreamFlowTheme(themeMode = state.themeMode) {
                if (isInPip) {
                    // Android OS Picture-in-Picture (PiP) Modu: Yalnızca saf video penceresi gösterilir
                    Box(modifier = Modifier.fillMaxSize()) {
                        PlayerScreen(
                            state = state,
                            effectiveStreamUrl = state.activeChannel?.let { viewModel.resolveEffectiveStreamUrl(it.streamUrl) } ?: "",
                            onCycleAspectRatio = { viewModel.cycleAspectRatio() },
                            onToggleMute = { viewModel.togglePlayerMute() },
                            onToggleFullscreen = { viewModel.toggleFullscreen() },
                            onSelectChannel = { viewModel.playChannel(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onOpenEpg = { viewModel.showEpgDialog(it) },
                            isPipMode = true,
                            onEnterPip = { enterPictureInPicture() }
                        )
                    }
                } else {
                    // Normal Uygulama Arayüzü
                    Scaffold(
                        topBar = {
                            if (!state.isFullscreen) {
                                TopAppBar(
                                    title = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(if (state.useProxy) MaterialTheme.colorScheme.primary else Color.Green)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "StreamFlow IPTV",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (state.useProxy) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    shape = MaterialTheme.shapes.small
                                                ) {
                                                    Text(
                                                        text = "PROXY",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    actions = {
                                        // VK Sürüm Rozeti (Prompt gereksinimi: VK01 -> VK02 -> VK03)
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            shape = MaterialTheme.shapes.small,
                                            modifier = Modifier.clickable { viewModel.showUpdateDialog(true) }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CloudSync,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = state.updateInfo?.currentTag ?: "VK06",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        // Kaynak Ekle Hızlı Butonu
                                        IconButton(onClick = { viewModel.showAddSourceDialog(true) }) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = "Kaynak Ekle")
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.background
                                    )
                                )
                            }
                        },
                        bottomBar = {
                            if (!state.isFullscreen) {
                                Column {
                                    // Uygulama İçi Kayan Mini Oynatıcı (In-App Floating PiP Bar)
                                    // Kullanıcı diğer sekmelerde dolaşırken içeriği izlemeye devam eder
                                    if (currentTabIndex != 2 && state.activeChannel != null) {
                                        MiniPlayerBar(
                                            channel = state.activeChannel!!,
                                            onExpand = { currentTabIndex = 2 },
                                            onEnterPip = { enterPictureInPicture() },
                                            onClose = { viewModel.stopPlayback() }
                                        )
                                    }

                                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                                        NavigationBarItem(
                                            selected = currentTabIndex == 0,
                                            onClick = { currentTabIndex = 0 },
                                            icon = { Icon(Icons.Default.LiveTv, contentDescription = "Canlı TV") },
                                            label = { Text("Canlı TV") }
                                        )
                                        NavigationBarItem(
                                            selected = currentTabIndex == 1,
                                            onClick = { currentTabIndex = 1 },
                                            icon = { Icon(Icons.Default.Movie, contentDescription = "VOD") },
                                            label = { Text("VOD") }
                                        )
                                        NavigationBarItem(
                                            selected = currentTabIndex == 2,
                                            onClick = { currentTabIndex = 2 },
                                            icon = { Icon(Icons.Default.PlayCircle, contentDescription = "Oynatıcı") },
                                            label = { Text("Oynatıcı") }
                                        )
                                        NavigationBarItem(
                                            selected = currentTabIndex == 3,
                                            onClick = { currentTabIndex = 3 },
                                            icon = { Icon(Icons.Default.FormatListBulleted, contentDescription = "Kaynaklar") },
                                            label = { Text("Kaynaklar") }
                                        )
                                        NavigationBarItem(
                                            selected = currentTabIndex == 4,
                                            onClick = { currentTabIndex = 4 },
                                            icon = { Icon(Icons.Default.Settings, contentDescription = "Ayarlar") },
                                            label = { Text("Ayarlar") }
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        val contentPadding = if (state.isFullscreen) PaddingValues(0.dp) else innerPadding
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(contentPadding)
                        ) {
                            when (currentTabIndex) {
                                0 -> LiveTvScreen(
                                    state = state,
                                    onSelectChannel = { channel ->
                                        if (state.defaultPlayerIsVlc) {
                                            VlcIntentHelper.launchVlc(context, channel.streamUrl, channel.name)
                                        } else {
                                            viewModel.playChannel(channel)
                                            currentTabIndex = 2 // Oynatıcı sekmesine geç
                                        }
                                    },
                                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                                    onOpenEpg = { viewModel.showEpgDialog(it) },
                                    onSelectPlaylist = { viewModel.selectPlaylist(it) },
                                    onSelectCategory = { viewModel.selectCategory(it) },
                                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                    onOpenAddSource = { viewModel.showAddSourceDialog(true) }
                                )
                                1 -> VodScreen(
                                    state = state,
                                    onSelectVod = { vod ->
                                        if (state.defaultPlayerIsVlc) {
                                            VlcIntentHelper.launchVlc(context, vod.streamUrl, vod.name)
                                        } else {
                                            viewModel.playChannel(vod)
                                            currentTabIndex = 2
                                        }
                                    }
                                )
                                2 -> PlayerScreen(
                                    state = state,
                                    effectiveStreamUrl = state.activeChannel?.let { viewModel.resolveEffectiveStreamUrl(it.streamUrl) } ?: "",
                                    onCycleAspectRatio = { viewModel.cycleAspectRatio() },
                                    onToggleMute = { viewModel.togglePlayerMute() },
                                    onToggleFullscreen = { viewModel.toggleFullscreen() },
                                    onSelectChannel = { viewModel.playChannel(it) },
                                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                                    onOpenEpg = { viewModel.showEpgDialog(it) },
                                    isPipMode = false,
                                    onEnterPip = { enterPictureInPicture() }
                                )
                                3 -> SourcesScreen(
                                    state = state,
                                    onOpenAddSource = { viewModel.showAddSourceDialog(true) },
                                    onDeletePlaylist = { viewModel.deletePlaylist(it) }
                                )
                                4 -> SettingsScreen(
                                    state = state,
                                    onSetTheme = { viewModel.setThemeMode(it) },
                                    onToggleProxy = { viewModel.setProxyEnabled(it) },
                                    onSetBackendUrl = { viewModel.setBackendUrl(it) },
                                    onToggleDefaultPlayer = { viewModel.setDefaultPlayerIsVlc(it) },
                                    onCheckForUpdates = { viewModel.checkForUpdates() },
                                    onSimulateVersionBump = { viewModel.simulateVersionBump() },
                                    onToggleAutoUpdate = { viewModel.setAutoInstallUpdates(it) },
                                    savedToken = viewModel.getSavedGithubToken(),
                                    onSaveToken = { viewModel.saveGithubToken(it) },
                                    onOpenReleases = { viewModel.openGithubReleasesInBrowser() }
                                )
                            }

                            // Diyaloglar
                            if (state.showUpdateDialog) {
                                UpdateDialog(
                                    updateInfo = state.updateInfo,
                                    onDismiss = { viewModel.showUpdateDialog(false) },
                                    onDownloadAndInstall = { url, tag ->
                                        viewModel.downloadAndInstallUpdate(url, tag)
                                    },
                                    onSimulateNextBump = { viewModel.simulateVersionBump() }
                                )
                            }

                            if (state.showAddSourceDialog) {
                                AddSourceDialog(
                                    onDismiss = { viewModel.showAddSourceDialog(false) },
                                    onAddM3u = { name, url ->
                                        viewModel.importM3uPlaylist(name, url) { count ->
                                            Toast.makeText(context, "$count kanal eklendi!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onAddLocalM3u = { name, content ->
                                        viewModel.importM3uContent(name, content) { count ->
                                            Toast.makeText(context, "Yerel dosya: $count kanal başarıyla içe aktarıldı!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onAddXtream = { name, host, user, pass ->
                                        viewModel.importXtreamCodes(name, host, user, pass) { count ->
                                            Toast.makeText(context, "Xtream: $count kanal eklendi!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onAddStb = { name, host, mac ->
                                        viewModel.importStbPortal(name, host, mac) { count ->
                                            Toast.makeText(context, "STB / MAG: $count kanal eklendi!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }

                            if (state.showEpgDialog && state.epgChannel != null) {
                                EpgGuideDialog(
                                    channel = state.epgChannel,
                                    onDismiss = { viewModel.showEpgDialog(null) },
                                    onPlay = {
                                        state.epgChannel?.let { viewModel.playChannel(it) }
                                        currentTabIndex = 2
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
