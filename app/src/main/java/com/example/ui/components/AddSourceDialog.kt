package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AddSourceDialog(
    onDismiss: () -> Unit,
    onAddM3u: (name: String, url: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: M3U URL, 1: Xtream Codes
    var playlistName by remember { mutableStateOf("") }
    var m3uUrl by remember { mutableStateOf("") }

    var xtreamHost by remember { mutableStateOf("") }
    var xtreamUser by remember { mutableStateOf("") }
    var xtreamPass by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AddLink,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Yeni Kaynak Ekle", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("M3U / M3U8") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Xtream Codes") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = { playlistName = it },
                        label = { Text("Çalma Listesi Adı") },
                        placeholder = { Text("Örn: Spor & Sinema Paketi") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = m3uUrl,
                        onValueChange = { m3uUrl = it },
                        label = { Text("M3U / M3U8 Bağlantısı (URL)") },
                        placeholder = { Text("http://example.com/playlist.m3u8") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Hızlı Test İçin Örnekler:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(
                        onClick = {
                            playlistName = "IPTV-org Test Listesi"
                            m3uUrl = "https://iptv-org.github.io/iptv/index.m3u"
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("• IPTV-org Açık Canlı TV Kanalları (Dünya)", style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = { playlistName = it },
                        label = { Text("Sunucu / Profil Adı") },
                        placeholder = { Text("Örn: Premium Xtream") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = xtreamHost,
                        onValueChange = { xtreamHost = it },
                        label = { Text("Sunucu Adresi (Host:Port)") },
                        placeholder = { Text("http://xtream-server.com:8080") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = xtreamUser,
                        onValueChange = { xtreamUser = it },
                        label = { Text("Kullanıcı Adı (Username)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = xtreamPass,
                        onValueChange = { xtreamPass = it },
                        label = { Text("Şifre (Password)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val name = playlistName.ifEmpty { if (selectedTab == 0) "M3U Listesi" else "Xtream Paketi" }
                    val finalUrl = if (selectedTab == 0) {
                        m3uUrl
                    } else {
                        val host = xtreamHost.trimEnd('/')
                        "$host/get.php?username=$xtreamUser&password=$xtreamPass&type=m3u_plus&output=ts"
                    }
                    if (finalUrl.isNotEmpty()) {
                        onAddM3u(name, finalUrl)
                        onDismiss()
                    }
                },
                enabled = if (selectedTab == 0) m3uUrl.isNotEmpty() else (xtreamHost.isNotEmpty() && xtreamUser.isNotEmpty())
            ) {
                Text("İçe Aktar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}
