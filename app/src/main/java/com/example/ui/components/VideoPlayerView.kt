package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.R
import com.example.data.model.Channel
import com.example.data.model.StreamType
import com.example.player.VlcIntentHelper
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerView(
    channel: Channel?,
    streamUrl: String,
    aspectRatioMode: Int,
    isMuted: Boolean = false,
    isFullscreen: Boolean = false,
    isPipMode: Boolean = false,
    onEnterPip: () -> Unit = {},
    onToggleMute: () -> Unit = {},
    onToggleFullscreen: () -> Unit = {},
    onCycleAspectRatio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    val maxAudioVolume = remember { audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15 }

    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showControls by remember { mutableStateOf(false) }

    // İlerleme ve Süre Takibi (VOD & Scrubbing)
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var isSeeking by remember { mutableStateOf(false) }
    var seekPositionMs by remember { mutableLongStateOf(0L) }

    // Ekran Üzerinden Jestle Ses ve Parlaklık Kontrolü
    var showVolumeHud by remember { mutableStateOf(false) }
    var showBrightnessHud by remember { mutableStateOf(false) }
    var volumePercent by remember { mutableFloatStateOf(0.5f) }
    var brightnessPercent by remember {
        val initial = activity?.window?.attributes?.screenBrightness ?: -1f
        mutableFloatStateOf(if (initial in 0.01f..1f) initial else 0.5f)
    }

    val exoPlayer = remember {
        val renderersFactory = DefaultRenderersFactory(context.applicationContext)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
            .setEnableDecoderFallback(true)

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                10_000,
                45_000,
                1_000,
                2_500
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        ExoPlayer.Builder(context.applicationContext, renderersFactory)
            .setLoadControl(loadControl)
            .build().apply {
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_OFF
                volume = if (isMuted) 0f else 1f
            }
    }

    // İlk ses seviyesini cihazdan oku
    LaunchedEffect(Unit) {
        val curVol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 8
        volumePercent = (curVol.toFloat() / maxAudioVolume.toFloat()).coerceIn(0f, 1f)
    }

    LaunchedEffect(isMuted) {
        exoPlayer.volume = if (isMuted) 0f else volumePercent
    }

    // Pozisyon ve Süre Güncelleme Döngüsü
    LaunchedEffect(isPlaying, streamUrl) {
        while (true) {
            if (!isSeeking) {
                currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
                val d = exoPlayer.duration
                durationMs = if (d > 0) d else 0L
            }
            delay(400)
        }
    }

    // HUD Gösterim Süreleri (1.5 sn sonra kaybolur)
    LaunchedEffect(showVolumeHud) {
        if (showVolumeHud) {
            delay(1500)
            showVolumeHud = false
        }
    }

    LaunchedEffect(showBrightnessHud) {
        if (showBrightnessHud) {
            delay(1500)
            showBrightnessHud = false
        }
    }

    DisposableEffect(streamUrl) {
        if (streamUrl.isNotEmpty()) {
            errorMessage = null
            isBuffering = true
            try {
                val mediaItem = MediaItem.fromUri(streamUrl)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()
            } catch (e: Exception) {
                errorMessage = "Akış başlatılamadı: ${e.localizedMessage ?: "Format desteklenmiyor"}"
                isBuffering = false
            }
        }

        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) {
                    val d = exoPlayer.duration
                    if (d > 0) durationMs = d
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlayerError(error: PlaybackException) {
                errorMessage = "Akış yüklenemedi: ${error.localizedMessage ?: "Bağlantı hatası"}"
                isBuffering = false
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.stop()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    fun seekRelative(offsetMs: Long) {
        val current = exoPlayer.currentPosition
        val target = (current + offsetMs).coerceAtLeast(0L)
        val maxPos = if (durationMs > 0) durationMs else target
        exoPlayer.seekTo(target.coerceAtMost(maxPos))
        currentPositionMs = exoPlayer.currentPosition
    }

    fun formatDuration(ms: Long): String {
        val totalSec = ms / 1000
        val hours = totalSec / 3600
        val minutes = (totalSec % 3600) / 60
        val seconds = totalSec % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .background(Color.Black)
            .fillMaxSize()
    ) {
        val boxWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val boxHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)

        // Arka Plan Video Görünümü
        AndroidView(
            factory = { ctx ->
                try {
                    (LayoutInflater.from(ctx).inflate(R.layout.exo_texture_player_view, null) as PlayerView).apply {
                        player = exoPlayer
                        useController = false
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                } catch (e: Exception) {
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                }
            },
            update = { playerView ->
                playerView.player = exoPlayer
                playerView.resizeMode = when (aspectRatioMode) {
                    1 -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    2 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                    3 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
                    else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // PiP (Küçük Ekran) modunda iken tüm kontrolleri ve arayüz elemanlarını gizle
        if (!isPipMode) {
            // Jest Dokunma ve Kaydırma Katmanı (Sol: Parlaklık, Sağ: Ses)
            Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            onToggleFullscreen()
                        },
                        onTap = {
                            showControls = !showControls
                        }
                    )
                }
                .pointerInput(Unit) {
                    var isLeftZone = false
                    detectDragGestures(
                        onDragStart = { offset ->
                            isLeftZone = offset.x < (boxWidthPx / 2f)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val delta = -dragAmount.y / (boxHeightPx * 0.65f)
                            if (isLeftZone) {
                                // Sol Taraf: Parlaklık Ayarı
                                brightnessPercent = (brightnessPercent + delta).coerceIn(0.01f, 1.0f)
                                activity?.window?.let { win ->
                                    val lp = win.attributes
                                    lp.screenBrightness = brightnessPercent
                                    win.attributes = lp
                                }
                                showBrightnessHud = true
                                showVolumeHud = false
                            } else {
                                // Sağ Taraf: Ses Ayarı
                                volumePercent = (volumePercent + delta).coerceIn(0.0f, 1.0f)
                                val newVolIdx = (volumePercent * maxAudioVolume).toInt()
                                audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, newVolIdx, 0)
                                exoPlayer.volume = volumePercent
                                showVolumeHud = true
                                showBrightnessHud = false
                            }
                        }
                    )
                }
        )

        // Buffering Spinner
        if (isBuffering) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Hata Paneli
        if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Hata",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "Bilinmeyen hata",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                errorMessage = null
                                exoPlayer.prepare()
                                exoPlayer.play()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Yeniden Dene")
                        }
                        if (channel != null) {
                            OutlinedButton(
                                onClick = {
                                    VlcIntentHelper.launchVlc(context, channel.streamUrl, channel.name)
                                }
                            ) {
                                Text("VLC ile Aç", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // HUD Geri Bildirim: Parlaklık (Sol Taraf)
        AnimatedVisibility(
            visible = showBrightnessHud,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 24.dp)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.85f),
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 8.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BrightnessMedium,
                        contentDescription = "Parlaklık",
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "%${(brightnessPercent * 100).toInt()}",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { brightnessPercent },
                        modifier = Modifier.width(48.dp).height(4.dp),
                        color = Color(0xFFFFD54F),
                        trackColor = Color.DarkGray
                    )
                }
            }
        }

        // HUD Geri Bildirim: Ses (Sağ Taraf)
        AnimatedVisibility(
            visible = showVolumeHud,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.85f),
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 8.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = if (volumePercent <= 0.01f) Icons.Default.VolumeOff else if (volumePercent < 0.5f) Icons.Default.VolumeDown else Icons.Default.VolumeUp,
                        contentDescription = "Ses",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "%${(volumePercent * 100).toInt()}",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { volumePercent },
                        modifier = Modifier.width(48.dp).height(4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.DarkGray
                    )
                }
            }
        }

        // Üst Kontrol Barı
        AnimatedVisibility(
            visible = showControls || errorMessage != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isFullscreen) {
                        IconButton(onClick = onToggleFullscreen) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Tam Ekrandan Çık",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Column {
                        Text(
                            text = channel?.name ?: "StreamFlow IPTV Player",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        if (channel?.currentProgram != null) {
                            Text(
                                text = channel.currentProgram,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Ses Aç/Kapat Butonu
                    IconButton(onClick = onToggleMute) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = if (isMuted) "Sesi Aç" else "Sesi Kapat",
                            tint = if (isMuted) MaterialTheme.colorScheme.error else Color.White
                        )
                    }

                    // En-Boy Oranı Butonu
                    IconButton(onClick = onCycleAspectRatio) {
                        Icon(
                            imageVector = Icons.Default.AspectRatio,
                            contentDescription = "En-Boy Oranı",
                            tint = Color.White
                        )
                    }

                    // Küçük Ekran (Picture-in-Picture) Butonu
                    IconButton(onClick = onEnterPip) {
                        Icon(
                            imageVector = Icons.Default.PictureInPictureAlt,
                            contentDescription = "Küçük Ekran (PiP)",
                            tint = Color.White
                        )
                    }

                    // Tam Ekran Butonu
                    IconButton(onClick = onToggleFullscreen) {
                        Icon(
                            imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = if (isFullscreen) "Tam Ekrandan Çık" else "Tam Ekran",
                            tint = Color.White
                        )
                    }

                    // VLC Player Intent Butonu
                    if (channel != null) {
                        FilledTonalButton(
                            onClick = {
                                VlcIntentHelper.launchVlc(context, channel.streamUrl, channel.name)
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("VLC", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Orta Kontrol Butonları: [⏪ -10s]  [⏯ Oynat/Duraklat]  [⏩ +10s]
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 10 Saniye Geri Sar
                IconButton(
                    onClick = { seekRelative(-10_000L) },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.6f), shape = MaterialTheme.shapes.extraLarge)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "10 Sn Geri",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Oynat / Duraklat
                IconButton(
                    onClick = {
                        if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.Black.copy(alpha = 0.7f), shape = MaterialTheme.shapes.extraLarge)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Duraklat" else "Oynat",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // 10 Saniye İleri Sar
                IconButton(
                    onClick = { seekRelative(10_000L) },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.6f), shape = MaterialTheme.shapes.extraLarge)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "10 Sn İleri",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Sessiz Durum Rozeti
        if (isMuted) {
            Surface(
                color = Color.Black.copy(alpha = 0.75f),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 60.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Sessiz",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Alt Kontrol Paneli: VOD İlerleme Barı (Scrubbing Seekbar) & Zaman Göstergesi
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // VOD / Video İlerleme Çubuğu
                val displayPosition = if (isSeeking) seekPositionMs else currentPositionMs
                val isVodContent = channel?.streamType == StreamType.VOD || durationMs > 0

                if (isVodContent) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatDuration(displayPosition),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        Slider(
                            value = displayPosition.toFloat().coerceIn(0f, durationMs.toFloat().coerceAtLeast(1f)),
                            onValueChange = { newValue ->
                                isSeeking = true
                                seekPositionMs = newValue.toLong()
                            },
                            onValueChangeFinished = {
                                exoPlayer.seekTo(seekPositionMs)
                                currentPositionMs = seekPositionMs
                                isSeeking = false
                            },
                            valueRange = 0f..durationMs.toFloat().coerceAtLeast(1f),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )

                        Text(
                            text = if (durationMs > 0) formatDuration(durationMs) else "--:--",
                            color = Color.LightGray,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                // Alt Bar Hızlı Aksiyonlar ve Mod Göstergesi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (channel?.streamType == StreamType.VOD) "🎬 VOD Video" else "🔴 Canlı Yayın",
                            color = if (channel?.streamType == StreamType.VOD) MaterialTheme.colorScheme.primary else Color.Red,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• Sol: Parlaklık | Sağ: Ses",
                            color = Color.Gray,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    val aspectModeText = when (aspectRatioMode) {
                        1 -> "Doldur (Zoom)"
                        2 -> "16:9 Geniş"
                        3 -> "4:3 Klasik"
                        else -> "Sığdır (Fit)"
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = aspectModeText,
                                color = Color.LightGray,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            IconButton(
                                onClick = onToggleFullscreen,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = if (isFullscreen) "Tam Ekrandan Çık" else "Tam Ekran",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
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
