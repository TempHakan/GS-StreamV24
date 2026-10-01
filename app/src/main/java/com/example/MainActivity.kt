package com.example

import android.os.Bundle
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.player.VlcIntentHelper
import com.example.ui.components.AddSourceDialog
import com.example.ui.components.EpgGuideDialog
import com.example.ui.components.UpdateDialog
import com.example.ui.screens.*
import com.example.ui.theme.StreamFlowTheme
import com.example.viewmodel.StreamViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: StreamViewModel = viewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            StreamFlowTheme(themeMode = state.themeMode) {
                MainScreenContent(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(viewModel: StreamViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var currentTabIndex by remember { mutableIntStateOf(0) }

    // Geri tuşu basıldığında Canlı TV sekmesine dön
    BackHandler(enabled = currentTabIndex != 0) {
        currentTabIndex = 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "StreamFlow IPTV",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    // VK Versiyon Rozeti (Tıklanınca güncelleme penceresi açılır)
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .clickable { viewModel.checkForUpdates() }
                            .padding(end = 8.dp)
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
                                text = state.updateInfo?.currentTag ?: "VK01",
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
        },
        bottomBar = {
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
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                    onSelectChannel = { viewModel.playChannel(it) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onOpenEpg = { viewModel.showEpgDialog(it) }
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
                    onToggleAutoUpdate = { viewModel.setAutoInstallUpdates(it) }
                )
            }
        }
    }

    // Güncelleme Bildirimi / Penceresi (VK01 -> VK02)
    if (state.showUpdateDialog) {
        UpdateDialog(
            updateInfo = state.updateInfo,
            onDismiss = { viewModel.showUpdateDialog(false) },
            onDownloadAndInstall = { url, tag ->
                viewModel.downloadAndInstallUpdate(url, tag)
            },
            onSimulateNextBump = {
                viewModel.simulateVersionBump()
            }
        )
    }

    // EPG Zaman Çizelgesi Penceresi
    if (state.showEpgDialog) {
        EpgGuideDialog(
            channel = state.epgChannel,
            onDismiss = { viewModel.showEpgDialog(null) },
            onPlay = {
                state.epgChannel?.let {
                    viewModel.playChannel(it)
                    currentTabIndex = 2
                }
            }
        )
    }

    // Yeni Kaynak Ekleme Penceresi (M3U, Xtream & STB MAC Portal)
    if (state.showAddSourceDialog) {
        AddSourceDialog(
            onDismiss = { viewModel.showAddSourceDialog(false) },
            onAddM3u = { name, url ->
                viewModel.importM3uPlaylist(name, url) { count ->
                    Toast.makeText(context, "$count kanal başarıyla içe aktarıldı.", Toast.LENGTH_SHORT).show()
                }
            },
            onAddStb = { name, host, mac ->
                viewModel.importStbPortal(name, host, mac) { count ->
                    Toast.makeText(context, "$count kanal STB portalından yüklendi.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}
