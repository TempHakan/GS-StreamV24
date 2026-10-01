package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.Channel
import com.example.ui.components.ChannelListItem
import com.example.viewmodel.StreamUiState

@Composable
fun LiveTvScreen(
    state: StreamUiState,
    onSelectChannel: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
    onOpenEpg: (Channel) -> Unit,
    onSelectCategory: (String?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onOpenAddSource: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredChannels = state.liveChannels.filter { channel ->
        val matchesCategory = state.selectedCategory == null || channel.groupTitle == state.selectedCategory
        val matchesSearch = state.searchQuery.isEmpty() ||
                channel.name.contains(state.searchQuery, ignoreCase = true) ||
                channel.groupTitle.contains(state.searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Search & Add Source Bar
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
        }

        // Category Filter Chips
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
                label = { Text("Tüm Kanallar (${state.liveChannels.size})") }
            )

            state.categories.forEach { category ->
                val count = state.liveChannels.count { it.groupTitle == category }
                FilterChip(
                    selected = state.selectedCategory == category,
                    onClick = { onSelectCategory(category) },
                    label = { Text("$category ($count)") }
                )
            }
        }

        // Channel List
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
                        text = if (state.searchQuery.isNotEmpty()) "Aramaya uygun kanal bulunamadı." else "Henüz kanal eklenmedi.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
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
