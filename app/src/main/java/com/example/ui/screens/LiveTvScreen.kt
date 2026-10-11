package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Channel
import com.example.ui.components.ChannelListItem
import com.example.ui.components.EpgGridView
import com.example.viewmodel.StreamUiState

@Composable
fun LiveTvScreen(
    state: StreamUiState,
    onSelectChannel: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
    onOpenEpg: (Channel) -> Unit,
    onSelectPlaylist: (String?) -> Unit,
    onSelectCategory: (String?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onOpenAddSource: () -> Unit,
    onSelectVod: (Channel) -> Unit = {},
    onToggleLock: (Channel) -> Unit = {},
    onRequestPlayChannel: (Channel) -> Unit = onSelectChannel,
    onDeleteContinueWatching: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isEpgGridMode by remember { mutableStateOf(false) }
    var onlyFavorites by remember { mutableStateOf(false) }
    var selectedResultTab by remember { mutableIntStateOf(0) } // 0: Tümü, 1: Canlı TV, 2: VOD & Filmler

    val query = state.searchQuery.trim()

    // 1. Seçili Çalma Listesine göre canlı kanalları filtrele
    val playlistChannels = remember(state.liveChannels, state.selectedPlaylistId) {
        if (state.selectedPlaylistId == null) {
            state.liveChannels
        } else {
            state.liveChannels.filter { it.playlistId == state.selectedPlaylistId }
        }
    }

    // 2. Canlı Kanallar: Gerçek zamanlı istemci tarafı arama, kategori ve favori filtreleme
    val filteredLiveChannels = remember(playlistChannels, query, state.selectedCategory, onlyFavorites) {
        playlistChannels.filter { channel ->
            val matchesSearch = query.isEmpty() ||
                    channel.name.contains(query, ignoreCase = true) ||
                    channel.groupTitle.contains(query, ignoreCase = true)
            val matchesCategory = state.selectedCategory == null || channel.groupTitle == state.selectedCategory
            val matchesFavorite = !onlyFavorites || channel.isFavorite
            matchesSearch && matchesCategory && matchesFavorite
        }
    }

    // 3. VOD Başlıkları: Gerçek zamanlı istemci tarafı arama ve favori filtreleme (Prompt gereksinimi)
    val filteredVodChannels = remember(state.vodChannels, query, onlyFavorites) {
        state.vodChannels.filter { vod ->
            val matchesSearch = query.isEmpty() ||
                    vod.name.contains(query, ignoreCase = true) ||
                    vod.groupTitle.contains(query, ignoreCase = true)
            val matchesFavorite = !onlyFavorites || vod.isFavorite
            matchesSearch && matchesFavorite
        }
    }

    val totalFavoritesCount = remember(state.liveChannels, state.vodChannels) {
        state.liveChannels.count { it.isFavorite } + state.vodChannels.count { it.isFavorite }
    }

    val availableCategories = remember(playlistChannels) {
        playlistChannels.map { it.groupTitle }.distinct().sorted()
    }

    val selectedPlaylist = remember(state.playlists, state.selectedPlaylistId) {
        state.playlists.find { it.id == state.selectedPlaylistId }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // 1. Arama Çubuğu, Kaynak Ekle ve EPG Grid Görünüm Değiştirici Butonları
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Canlı kanal veya VOD/film başlığı ara...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Arama")
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Temizle")
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilledTonalIconButton(
                onClick = onOpenAddSource,
                modifier = Modifier.size(52.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Kaynak Ekle")
            }
            Spacer(modifier = Modifier.width(6.dp))
            FilledTonalIconButton(
                onClick = { isEpgGridMode = !isEpgGridMode },
                modifier = Modifier.size(52.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (isEpgGridMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    imageVector = if (isEpgGridMode) Icons.Default.FormatListBulleted else Icons.Default.CalendarViewWeek,
                    contentDescription = if (isEpgGridMode) "Kanal Listesi" else "EPG Program Rehberi (Grid)",
                    tint = if (isEpgGridMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (isEpgGridMode) {
            // Görsel EPG Program Rehberi (Grid Görünümü)
            EpgGridView(
                channels = filteredLiveChannels,
                onSelectChannel = onSelectChannel,
                onToggleFavorite = onToggleFavorite,
                modifier = Modifier.weight(1f)
            )
        } else {
            // Standart Liste Görünümü

            // 2. Hızlı Filtre Barı: [⭐ Sadece Favoriler Toggle] ve Arama Filtre Sekmeleri
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Room Database Destekli Favoriler Toggle Butonu
                FilterChip(
                    selected = onlyFavorites,
                    onClick = { onlyFavorites = !onlyFavorites },
                    label = { Text("Favoriler ($totalFavoritesCount)", fontWeight = FontWeight.Bold) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (onlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (onlyFavorites) Color(0xFFFF4081) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFF4081).copy(alpha = 0.2f),
                        selectedLabelColor = Color(0xFFFF4081)
                    )
                )

                // Arama Yapılırken Canlı TV & VOD Sekme Filtreleri
                if (query.isNotEmpty() || state.vodChannels.isNotEmpty()) {
                    FilterChip(
                        selected = selectedResultTab == 0,
                        onClick = { selectedResultTab = 0 },
                        label = { Text("Tümü (${filteredLiveChannels.size + filteredVodChannels.size})") }
                    )
                    FilterChip(
                        selected = selectedResultTab == 1,
                        onClick = { selectedResultTab = 1 },
                        label = { Text("Canlı TV (${filteredLiveChannels.size})") },
                        leadingIcon = {
                            Icon(Icons.Default.LiveTv, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    )
                    FilterChip(
                        selected = selectedResultTab == 2,
                        onClick = { selectedResultTab = 2 },
                        label = { Text("VOD & Film (${filteredVodChannels.size})") },
                        leadingIcon = {
                            Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    )
                }
            }

            // 3. Çalma Listesi Seçici
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )

                FilterChip(
                    selected = state.selectedPlaylistId == null,
                    onClick = { onSelectPlaylist(null) },
                    label = { Text("Tüm Listeler (${state.liveChannels.size})", fontWeight = FontWeight.SemiBold) },
                    leadingIcon = {
                        Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )

                state.playlists.forEach { playlist ->
                    val icon = when (playlist.type) {
                        "STB" -> Icons.Default.Dvr
                        "XTREAM" -> Icons.Default.CloudSync
                        else -> Icons.Default.FormatListBulleted
                    }
                    FilterChip(
                        selected = state.selectedPlaylistId == playlist.id,
                        onClick = { onSelectPlaylist(playlist.id) },
                        label = {
                            Text(
                                text = "${playlist.name} (${playlist.channelCount})",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }

            // Seçili Liste Rozeti
            if (selectedPlaylist != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Aktif Liste: ${selectedPlaylist.name} [${selectedPlaylist.type}]",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = { onSelectPlaylist(null) },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text("Tümünü Göster", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Kategori Filtre Çipleri
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.selectedCategory == null,
                    onClick = { onSelectCategory(null) },
                    label = { Text("Tüm Kategoriler (${playlistChannels.size})") }
                )

                availableCategories.forEach { category ->
                    val count = playlistChannels.count { it.groupTitle == category }
                    val isCatLocked = state.isParentalControlEnabled && state.lockedCategories.contains(category)
                    FilterChip(
                        selected = state.selectedCategory == category,
                        onClick = { onSelectCategory(category) },
                        label = { Text("$category ($count)") },
                        leadingIcon = if (isCatLocked) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Kilitli Kategori",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else null
                    )
                }
            }

            // 4. İçerik Listesi: Canlı TV ve Arama Sonucu Eşleşen VOD Başlıkları
            val showVodSection = (selectedResultTab == 0 || selectedResultTab == 2) && filteredVodChannels.isNotEmpty()
            val showLiveSection = (selectedResultTab == 0 || selectedResultTab == 1) && filteredLiveChannels.isNotEmpty()

            if (!showVodSection && !showLiveSection) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (onlyFavorites) Icons.Default.FavoriteBorder else Icons.Default.TvOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (onlyFavorites)
                                "Favori listenizde henüz kanal veya film bulunmuyor.\nKalp simgesine dokunarak favorilerinize ekleyebilirsiniz."
                            else
                                "Aranan kriterlere uygun canlı kanal veya VOD içeriği bulunamadı.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(onClick = {
                            onSelectPlaylist(null)
                            onSelectCategory(null)
                            onSearchQueryChange("")
                            onlyFavorites = false
                            selectedResultTab = 0
                        }) {
                            Text("Filtreleri Temizle")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 🌟 VOD İzlemeye Devam Et (Continue Watching) Bölümü (Room Database Destekli)
                    // Kullanıcı arama yapmıyorken veya genel ekranda iken en son izlenen VOD'lar gösterilir
                    if (state.continueWatchingList.isNotEmpty() && query.isEmpty() && !onlyFavorites) {
                        item(key = "continue_watching_section_header") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "İzlemeye Devam Et",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = "${state.continueWatchingList.size}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // İzlemeye Devam Et: Yatay Kartlar ve İlerleme Çubuğu (Progress Bar)
                        item(key = "continue_watching_horizontal_row") {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(state.continueWatchingList, key = { "cw_${it.vodId}" }) { item ->
                                    val matchedVod = state.vodChannels.find { it.id == item.vodId }
                                    val fallbackVod = Channel(
                                        id = item.vodId,
                                        name = item.title,
                                        streamUrl = item.streamUrl,
                                        logoUrl = item.logoUrl,
                                        groupTitle = item.groupTitle,
                                        streamType = com.example.data.model.StreamType.VOD
                                    )
                                    val targetVod = matchedVod ?: fallbackVod

                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(12.dp),
                                        tonalElevation = 3.dp,
                                        modifier = Modifier
                                            .width(180.dp)
                                            .clickable { onSelectVod(targetVod) }
                                    ) {
                                        Column {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(105.dp)
                                                    .background(MaterialTheme.colorScheme.surface),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (!item.logoUrl.isNullOrEmpty()) {
                                                    AsyncImage(
                                                        model = item.logoUrl,
                                                        contentDescription = item.title,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.Movie,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(40.dp)
                                                    )
                                                }

                                                // Oynat Overlay Rozeti
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.Black.copy(alpha = 0.65f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = "Oynat",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }

                                                // Kaldır Butonu (Çarpı)
                                                IconButton(
                                                    onClick = { onDeleteContinueWatching(item.vodId) },
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(2.dp)
                                                        .size(26.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.Black.copy(alpha = 0.6f))
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Geçmişten Kaldır",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }

                                                // İzleme İlerleme Çubuğu (Bottom Progress Bar)
                                                LinearProgressIndicator(
                                                    progress = { item.progress },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(4.dp)
                                                        .align(Alignment.BottomCenter),
                                                    color = MaterialTheme.colorScheme.primary,
                                                    trackColor = Color.Black.copy(alpha = 0.5f),
                                                )
                                            }

                                            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                                Text(
                                                    text = item.title,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = item.groupTitle,
                                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Text(
                                                        text = "%${(item.progress * 100).toInt()}",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item(key = "divider_after_continue_watching") {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )
                        }
                    }

                    // Eşleşen VOD Başlıkları Bölümü (Arama yapıldığında veya VOD sekmesinde)
                    if (showVodSection) {
                        item(key = "vod_section_header") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Movie,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "VOD & Film Sonuçları (${filteredVodChannels.size})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // VOD Sonuçları: Yatay Kayan Poster Kartları
                        item(key = "vod_horizontal_row") {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(filteredVodChannels, key = { "vod_${it.id}" }) { vodItem ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(10.dp),
                                        tonalElevation = 2.dp,
                                        modifier = Modifier
                                            .width(150.dp)
                                            .clickable { onSelectVod(vodItem) }
                                    ) {
                                        Column {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(110.dp)
                                                    .background(MaterialTheme.colorScheme.surface),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (!vodItem.logoUrl.isNullOrEmpty()) {
                                                    AsyncImage(
                                                        model = vodItem.logoUrl,
                                                        contentDescription = vodItem.name,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.Movie,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(36.dp)
                                                    )
                                                }

                                                // Favori Butonu
                                                IconButton(
                                                    onClick = { onToggleFavorite(vodItem) },
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(2.dp)
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.Black.copy(alpha = 0.6f))
                                                ) {
                                                    Icon(
                                                        imageVector = if (vodItem.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                        contentDescription = "Favori",
                                                        tint = if (vodItem.isFavorite) Color(0xFFFF4081) else Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }

                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(
                                                    text = vodItem.name,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = vodItem.groupTitle,
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "İzle ▶",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (showLiveSection) {
                            item(key = "divider_after_vod") {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }

                    // Canlı Kanallar Başlığı ve Listesi
                    if (showLiveSection) {
                        if (showVodSection) {
                            item(key = "live_section_header") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LiveTv,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Canlı TV Kanalları (${filteredLiveChannels.size})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        items(filteredLiveChannels, key = { it.id }) { channel ->
                            val isLocked = state.isParentalControlEnabled &&
                                    !state.isSessionUnlocked &&
                                    !state.unlockedChannelIds.contains(channel.id) &&
                                    (channel.isLocked || state.lockedCategories.contains(channel.groupTitle))

                            ChannelListItem(
                                channel = channel,
                                isSelected = state.activeChannel?.id == channel.id,
                                isLocked = isLocked,
                                onSelect = { onRequestPlayChannel(channel) },
                                onToggleFavorite = { onToggleFavorite(channel) },
                                onToggleLock = { onToggleLock(channel) },
                                onOpenEpg = { onOpenEpg(channel) }
                            )
                        }
                    }
                }
            }
        }
    }
}
