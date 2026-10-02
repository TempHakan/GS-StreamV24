package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    modifier: Modifier = Modifier
) {
    var isEpgGridMode by remember { mutableStateOf(false) }

    // 1. Seçili Çalma Listesine göre filtrele
    val playlistChannels = if (state.selectedPlaylistId == null) {
        state.liveChannels
    } else {
        state.liveChannels.filter { it.playlistId == state.selectedPlaylistId }
    }

    // 2. Seçili listenin kategorilerini hesapla
    val availableCategories = playlistChannels.map { it.groupTitle }.distinct().sorted()

    // 3. Arama ve Kategori filtrelerini uygula
    val filteredChannels = playlistChannels.filter { channel ->
        val matchesCategory = state.selectedCategory == null || channel.groupTitle == state.selectedCategory
        val matchesSearch = state.searchQuery.isEmpty() ||
                channel.name.contains(state.searchQuery, ignoreCase = true) ||
                channel.groupTitle.contains(state.searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    val selectedPlaylist = state.playlists.find { it.id == state.selectedPlaylistId }

    Column(modifier = modifier.fillMaxSize()) {
        // Search & Add Source & EPG Toggle Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Kanal veya kategori ara...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
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
                channels = filteredChannels,
                onSelectChannel = onSelectChannel,
                onToggleFavorite = onToggleFavorite,
                modifier = Modifier.weight(1f)
            )
        } else {
            // Standart Liste Görünümü
            // Çalma Listesi / Kaynak Seçici (Playlist Selector)
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

            // Tüm Kaynaklar Çipi
            FilterChip(
                selected = state.selectedPlaylistId == null,
                onClick = { onSelectPlaylist(null) },
                label = { Text("Tüm Listeler (${state.liveChannels.size})", fontWeight = FontWeight.SemiBold) },
                leadingIcon = {
                    Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            )

            // Ekli Kaynaklar / Portallar Çipleri
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

        // Seçili Liste Bilgi Rozeti (Eğer spesifik bir liste seçildiyse)
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
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.selectedCategory == null,
                onClick = { onSelectCategory(null) },
                label = { Text("Tüm Kategoriler (${playlistChannels.size})") }
            )

            availableCategories.forEach { category ->
                val count = playlistChannels.count { it.groupTitle == category }
                FilterChip(
                    selected = state.selectedCategory == category,
                    onClick = { onSelectCategory(category) },
                    label = { Text("$category ($count)") }
                )
            }
        }

        // Kanal Listesi
        if (filteredChannels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.TvOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Bu listede aranan kriterlere uygun kanal bulunamadı.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = {
                        onSelectPlaylist(null)
                        onSelectCategory(null)
                        onSearchQueryChange("")
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
                items(filteredChannels, key = { it.id }) { channel ->
                    ChannelListItem(
                        channel = channel,
                        isSelected = state.activeChannel?.id == channel.id,
                        onSelect = { onSelectChannel(channel) },
                        onToggleFavorite = { onToggleFavorite(channel) },
                        onOpenEpg = { onOpenEpg(channel) }
                    )
                }
            }
        }
    }
}
}
