package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Channel
import com.example.player.VlcIntentHelper
import com.example.ui.components.TvFocusableCard
import com.example.ui.components.VideoPlayerView
import com.example.viewmodel.StreamUiState

@Composable
fun PlayerScreen(
    state: StreamUiState,
    effectiveStreamUrl: String,
    onCycleAspectRatio: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onSelectChannel: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
    onOpenEpg: (Channel) -> Unit,
    isPipMode: Boolean = false,
    onEnterPip: () -> Unit = {},
    onRequestUnlock: (Channel) -> Unit = {},
    onSaveVodProgress: (vodId: String, title: String, streamUrl: String, logoUrl: String?, groupTitle: String, posMs: Long, durMs: Long) -> Unit = { _, _, _, _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val channel = state.activeChannel

    val isChannelLocked = channel != null &&
            state.isParentalControlEnabled &&
            !state.isSessionUnlocked &&
            !state.unlockedChannelIds.contains(channel.id) &&
            (channel.isLocked || state.lockedCategories.contains(channel.groupTitle))

    val vodHistory = remember(channel?.id, state.continueWatchingList) {
        if (channel != null && channel.streamType == com.example.data.model.StreamType.VOD) {
            state.continueWatchingList.find { it.vodId == channel.id }
        } else null
    }
    val initialPosMs = vodHistory?.positionMs ?: 0L

    // PiP Modunda iken doğrudan tam ekran video oynatıcıyı göster
    if (isPipMode) {
        Box(modifier = modifier.fillMaxSize()) {
            if (isChannelLocked) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Kilitli", tint = Color.Red, modifier = Modifier.size(48.dp))
                }
            } else if (channel != null) {
                VideoPlayerView(
                    channel = channel,
                    streamUrl = effectiveStreamUrl,
                    aspectRatioMode = state.aspectRatioMode,
                    isMuted = state.isPlayerMuted,
                    isFullscreen = true,
                    isPipMode = true,
                    onEnterPip = onEnterPip,
                    onToggleMute = onToggleMute,
                    onToggleFullscreen = onToggleFullscreen,
                    onCycleAspectRatio = onCycleAspectRatio,
                    initialPlaybackPositionMs = initialPosMs,
                    onPlaybackProgress = { posMs, durMs ->
                        if (channel.streamType == com.example.data.model.StreamType.VOD) {
                            onSaveVodProgress(channel.id, channel.name, channel.streamUrl, channel.logoUrl, channel.groupTitle, posMs, durMs)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        return
    }

    // Tam ekranda iken Android fiziksel geri tuşuna basıldığında önce tam ekrandan çık
    if (state.isFullscreen) {
        BackHandler(enabled = true) {
            onToggleFullscreen()
        }

        // Tam ekran modu: Ekranın %100'ü yalnızca videoya ayrılır
        Box(modifier = modifier.fillMaxSize()) {
            if (isChannelLocked) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Kilitli", tint = Color.Red, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Bu Kanal PIN ile Kilitlidir", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { channel?.let(onRequestUnlock) }) {
                            Text("PIN Girin ve Açın")
                        }
                    }
                }
            } else if (channel != null) {
                VideoPlayerView(
                    channel = channel,
                    streamUrl = effectiveStreamUrl,
                    aspectRatioMode = state.aspectRatioMode,
                    isMuted = state.isPlayerMuted,
                    isFullscreen = true,
                    isPipMode = false,
                    onEnterPip = onEnterPip,
                    onToggleMute = onToggleMute,
                    onToggleFullscreen = onToggleFullscreen,
                    onCycleAspectRatio = onCycleAspectRatio,
                    initialPlaybackPositionMs = initialPosMs,
                    onPlaybackProgress = { posMs, durMs ->
                        if (channel.streamType == com.example.data.model.StreamType.VOD) {
                            onSaveVodProgress(channel.id, channel.name, channel.streamUrl, channel.logoUrl, channel.groupTitle, posMs, durMs)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        return
    }

    // Normal mod: Üstte video oynatıcı, altta kanal bilgisi ve hızlı kanal değiştirici
    Column(modifier = modifier.fillMaxSize()) {
        // Video Player Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (isChannelLocked) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Kilitli", tint = Color.Red, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Ebeveyn Denetimi Koruması", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${channel?.name} (${channel?.groupTitle}) kilitli.", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { channel?.let(onRequestUnlock) }) {
                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PIN Kodu ile Aç")
                        }
                    }
                }
            } else if (channel != null) {
                VideoPlayerView(
                    channel = channel,
                    streamUrl = effectiveStreamUrl,
                    aspectRatioMode = state.aspectRatioMode,
                    isMuted = state.isPlayerMuted,
                    isFullscreen = false,
                    isPipMode = false,
                    onEnterPip = onEnterPip,
                    onToggleMute = onToggleMute,
                    onToggleFullscreen = onToggleFullscreen,
                    onCycleAspectRatio = onCycleAspectRatio,
                    initialPlaybackPositionMs = initialPosMs,
                    onPlaybackProgress = { posMs, durMs ->
                        if (channel.streamType == com.example.data.model.StreamType.VOD) {
                            onSaveVodProgress(channel.id, channel.name, channel.streamUrl, channel.logoUrl, channel.groupTitle, posMs, durMs)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Lütfen oynatmak için bir kanal seçin.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Channel Info & Quick Actions Bar
        if (channel != null) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = channel.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (channel.currentProgram != null) {
                                Text(
                                    text = channel.currentProgram,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Tam Ekran Butonu
                            IconButton(onClick = onToggleFullscreen) {
                                Icon(
                                    imageVector = Icons.Default.Fullscreen,
                                    contentDescription = "Tam Ekran",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Ses Açma / Kapama Butonu
                            IconButton(onClick = onToggleMute) {
                                Icon(
                                    imageVector = if (state.isPlayerMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                    contentDescription = if (state.isPlayerMuted) "Sesi Aç" else "Sesi Kapat",
                                    tint = if (state.isPlayerMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            }

                            // EPG Butonu
                            IconButton(onClick = { onOpenEpg(channel) }) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "EPG",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Favori Butonu
                            IconButton(onClick = { onToggleFavorite(channel) }) {
                                Icon(
                                    imageVector = if (channel.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favori",
                                    tint = if (channel.isFavorite) Color(0xFFFF4081) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // VLC ile Aç Butonu
                            Button(
                                onClick = {
                                    VlcIntentHelper.launchVlc(context, channel.streamUrl, channel.name)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("VLC", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Hızlı Kanal Değiştirici:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Hızlı Kanal Değiştirici Listesi
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(state.liveChannels, key = { it.id }) { ch ->
                            val isSelected = ch.id == channel.id
                            TvFocusableCard(
                                onClick = { onSelectChannel(ch) },
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.width(150.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = ch.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = ch.groupTitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
