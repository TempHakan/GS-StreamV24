package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Channel
import com.example.data.model.EpgProgram
import com.example.data.repository.EpgRepository
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpgGridView(
    channels: List<Channel>,
    onSelectChannel: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val epgRepository = remember { EpgRepository() }

    // Dikey ve Yatay Liste Durumları
    val verticalLazyListState = rememberLazyListState()
    val horizontalScrollState = rememberScrollState()
    val smoothFlingBehavior = ScrollableDefaults.flingBehavior()

    var epgScheduleMap by remember { mutableStateOf<Map<String, List<EpgProgram>>>(emptyMap()) }
    var selectedProgram by remember { mutableStateOf<Pair<Channel, EpgProgram>?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf("Tümü") }

    // Günün başlangıç ve bitiş saatleri
    val startDayMinutes = 6 * 60 // 06:00
    val totalSlotsCount = 37 // 06:00 -> 24:00+ (30 dk'lık dilimler)
    val slotWidthDp = 120.dp
    val slotMinutes = 30f
    val channelColumnWidth = 115.dp

    // Güncel saat ve dakika
    val calendar = remember { Calendar.getInstance() }
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    val currentMinute = calendar.get(Calendar.MINUTE)
    val currentTotalMinutes = currentHour * 60 + currentMinute

    // "Şu An" zamanının x koordinatı (06:00 referanslı)
    val nowOffsetMinutes = (currentTotalMinutes - startDayMinutes).coerceAtLeast(0)
    val nowOffsetDp = ((nowOffsetMinutes / slotMinutes) * slotWidthDp.value).dp

    // EPG verilerini arka planda getir
    LaunchedEffect(channels) {
        if (channels.isNotEmpty()) {
            epgScheduleMap = epgRepository.getEpgGridForChannels(channels)
        }
    }

    // Pürüzsüz kaydırma fonksiyonu (Spring Physics Destekli)
    fun smoothScrollToMinute(targetMinutes: Int) {
        coroutineScope.launch {
            val minutesFromStart = (targetMinutes - startDayMinutes).coerceAtLeast(0)
            val targetDp = (minutesFromStart / slotMinutes) * slotWidthDp.value
            val targetPx = with(density) { (targetDp.dp - 150.dp).coerceAtLeast(0.dp).toPx() }.toInt()
            horizontalScrollState.animateScrollTo(
                value = targetPx,
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioLowBouncy
                )
            )
        }
    }

    // İlk açılışta canlı saat dilimine odaklan
    LaunchedEffect(epgScheduleMap) {
        if (epgScheduleMap.isNotEmpty()) {
            smoothScrollToMinute(currentTotalMinutes)
        }
    }

    // Kategoriler ve Filtrelenmiş Kanallar
    val categories = remember(channels) {
        listOf("Tümü") + channels.map { it.groupTitle }.distinct().sorted()
    }

    val filteredChannels = remember(channels, selectedCategoryFilter) {
        if (selectedCategoryFilter == "Tümü") channels
        else channels.filter { it.groupTitle == selectedCategoryFilter }
    }

    // "Şu An" kırmızı çizgisinin ekrandaki konumu (derivedStateOf ile gereksiz recomposition önlenir)
    val indicatorXOffset by remember {
        derivedStateOf {
            val scrollDp = with(density) { horizontalScrollState.value.toDp() }
            channelColumnWidth + nowOffsetDp - scrollDp
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Üst Kontrol & Bilgi Barı
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CANLI EPG",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = String.format("%02d:%02d", currentHour, currentMinute),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Hızlı Atlama Aksiyonları: [-2 Saat] [🔴 Şimdi] [+2 Saat]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilledTonalIconButton(
                            onClick = {
                                val currentScrollDp = with(density) { horizontalScrollState.value.toDp() }
                                val newOffset = (currentScrollDp - (slotWidthDp * 4)).coerceAtLeast(0.dp)
                                coroutineScope.launch {
                                    horizontalScrollState.animateScrollTo(
                                        with(density) { newOffset.toPx() }.toInt(),
                                        spring(stiffness = Spring.StiffnessMedium)
                                    )
                                }
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.FastRewind, contentDescription = "-2 Saat", modifier = Modifier.size(16.dp))
                        }

                        Button(
                            onClick = { smoothScrollToMinute(currentTotalMinutes) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(34.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Şimdi", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }

                        FilledTonalIconButton(
                            onClick = {
                                val currentScrollDp = with(density) { horizontalScrollState.value.toDp() }
                                val newOffset = currentScrollDp + (slotWidthDp * 4)
                                coroutineScope.launch {
                                    horizontalScrollState.animateScrollTo(
                                        with(density) { newOffset.toPx() }.toInt(),
                                        spring(stiffness = Spring.StiffnessMedium)
                                    )
                                }
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.FastForward, contentDescription = "+2 Saat", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Hızlı Zaman Dilimi Hapları (08:00 Sabah, 12:00 Öğle, 18:00 Akşam, 20:00 Prime Time, 22:30 Gece)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val timeShortcuts = listOf(
                        Pair("08:00 Sabah", 8 * 60),
                        Pair("12:00 Öğle", 12 * 60),
                        Pair("15:00 İkindi", 15 * 60),
                        Pair("18:00 Akşam", 18 * 60),
                        Pair("20:00 Prime Time", 20 * 60),
                        Pair("22:30 Gece Kuşağı", 22 * 60 + 30)
                    )

                    timeShortcuts.forEach { (label, minutes) ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.clickable { smoothScrollToMinute(minutes) }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 2. Kategori Çipleri
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategoryFilter).coerceAtLeast(0),
            edgePadding = 12.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            divider = {}
        ) {
            categories.forEach { cat ->
                Tab(
                    selected = selectedCategoryFilter == cat,
                    onClick = { selectedCategoryFilter = cat },
                    text = {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedCategoryFilter == cat) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // 3. EPG Izgarası: Senkronize Tek Bir LazyColumn (Yüksek Performanslı Render)
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Zaman Başlığı (Üst Sabit Satır)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    // Sol Köşe Başlığı
                    Box(
                        modifier = Modifier
                            .width(channelColumnWidth)
                            .fillMaxHeight()
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "Kanallar (${filteredChannels.size})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                    // 06:00 -> 24:00 Saat Dilimleri (Smooth Fling Physics)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(
                                state = horizontalScrollState,
                                flingBehavior = smoothFlingBehavior
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (hour in 6..24) {
                            for (minute in listOf(0, 30)) {
                                if (hour == 24 && minute == 30) continue
                                Box(
                                    modifier = Modifier
                                        .width(slotWidthDp)
                                        .fillMaxHeight()
                                        .padding(horizontal = 6.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = String.format("%02d:%02d", hour % 24, minute),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // TEK BİR LAZYCOLUMN İÇERİSİNDE TÜM KANAL VE PROGRAMLAR (Senkronize Dikey Kaydırma)
                LazyColumn(
                    state = verticalLazyListState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = filteredChannels,
                        key = { it.id },
                        contentType = { "epg_row" }
                    ) { channel ->
                        val programs = epgScheduleMap[channel.id] ?: emptyList()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(76.dp)
                        ) {
                            // 1. Sol Sabit Kanal Başlık Hücresi (115.dp)
                            EpgChannelCell(
                                channel = channel,
                                width = channelColumnWidth,
                                onClick = { onSelectChannel(channel) },
                                onToggleFavorite = { onToggleFavorite(channel) }
                            )

                            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                            // 2. Sağ Senkronize Yatay Program Çizelgesi
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .horizontalScroll(
                                        state = horizontalScrollState,
                                        flingBehavior = smoothFlingBehavior
                                    )
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                programs.forEach { prog ->
                                    val progWidthDp = ((prog.durationMinutes / slotMinutes) * slotWidthDp.value).dp

                                    EpgProgramCard(
                                        program = prog,
                                        width = progWidthDp,
                                        onClick = {
                                            selectedProgram = Pair(channel, prog)
                                        }
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                    }
                }
            }

            // Kırmızı "Şu An / Canlı" İndikatör Çizgisi (Tüm Izgara Üzerinde)
            if (indicatorXOffset > channelColumnWidth) {
                Box(
                    modifier = Modifier
                        .offset(x = indicatorXOffset)
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(Color.Red.copy(alpha = 0.85f))
                )
            }
        }
    }

    // Program Detay Modalı
    selectedProgram?.let { (ch, prog) ->
        AlertDialog(
            onDismissRequest = { selectedProgram = null },
            icon = {
                Icon(
                    imageVector = if (prog.isLiveNow) Icons.Default.LiveTv else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (prog.isLiveNow) Color.Red else MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = prog.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = ch.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Text(
                            text = "${prog.startTime} - ${prog.endTime} (${prog.durationMinutes} dk)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (prog.isLiveNow) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Şu Anda Yayında (%${(prog.progress * 100).toInt()})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { prog.progress },
                            modifier = Modifier.fillMaxWidth().height(4.dp),
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Text(
                        text = prog.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSelectChannel(ch)
                        selectedProgram = null
                    }
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (prog.isLiveNow) "Kanalı Canlı İzle" else "Kanalı Aç")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedProgram = null }) {
                    Text("Kapat")
                }
            }
        )
    }
}

@Composable
fun EpgChannelCell(
    channel: Channel,
    width: Dp,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Surface(
        color = if (channel.isFavorite) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else Color.Transparent,
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Kanal Logosu
            if (!channel.logoUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = channel.logoUrl,
                    contentDescription = channel.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .padding(2.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = channel.name.take(2).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = channel.groupTitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun EpgProgramCard(
    program: EpgProgram,
    width: Dp,
    onClick: () -> Unit
) {
    val isLive = program.isLiveNow
    val cardBackground = if (isLive) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    } else if (program.progress >= 1.0f) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val cardBorder = if (isLive) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    }

    Surface(
        color = cardBackground,
        border = cardBorder,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .padding(horizontal = 2.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${program.startTime} - ${program.endTime}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (isLive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isLive) FontWeight.Bold else FontWeight.Normal
                    )

                    if (isLive) {
                        Surface(
                            color = Color.Red,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "CANLI",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = program.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isLive) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isLive) {
                LinearProgressIndicator(
                    progress = { program.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Gray.copy(alpha = 0.3f)
                )
            } else {
                Text(
                    text = program.category,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1
                )
            }
        }
    }
}
