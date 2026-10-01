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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSourceDialog(
    onDismiss: () -> Unit,
    onAddM3u: (name: String, url: String) -> Unit,
    onAddStb: (name: String, portalUrl: String, mac: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: M3U URL, 1: Xtream Codes, 2: STB / MAC Portal
    var playlistName by remember { mutableStateOf("") }
    var m3uUrl by remember { mutableStateOf("") }

    var xtreamHost by remember { mutableStateOf("") }
    var xtreamUser by remember { mutableStateOf("") }
    var xtreamPass by remember { mutableStateOf("") }

    var stbHost by remember { mutableStateOf("") }
    var stbMac by remember { mutableStateOf("00:1A:79:B4:C2:A1") }

    fun generateRandomMac(): String {
        val randHex = { (0..255).random().toString(16).padStart(2, '0').uppercase() }
        return "00:1A:79:${randHex()}:${randHex()}:${randHex()}"
    }

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
                PrimaryScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
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
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("STB / MAC") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> {
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
                            text = "Hızlı Test İçin Örnek:",
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
                    }
                    1 -> {
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
                    2 -> {
                        OutlinedTextField(
                            value = playlistName,
                            onValueChange = { playlistName = it },
                            label = { Text("STB Portal Adı") },
                            placeholder = { Text("Örn: MAG250 Stalker Portalı") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = stbHost,
                            onValueChange = { stbHost = it },
                            label = { Text("Portal URL (Stalker / Ministra)") },
                            placeholder = { Text("http://mag.portal.com:8080/c/") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = stbMac,
                            onValueChange = { stbMac = it.uppercase() },
                            label = { Text("Cihaz MAC Adresi") },
                            placeholder = { Text("00:1A:79:XX:XX:XX") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MAG 250/322 Formatı",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(
                                onClick = { stbMac = generateRandomMac() },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Rastgele MAC", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                playlistName = "MAG Demo Stalker Portalı"
                                stbHost = "http://stalker.iptvportal.tv:8080/c"
                                stbMac = "00:1A:79:B4:C2:A1"
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("• Örnek STB Portalı Yükle", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (selectedTab) {
                        0 -> {
                            val name = playlistName.ifEmpty { "M3U Çalma Listesi" }
                            if (m3uUrl.isNotEmpty()) {
                                onAddM3u(name, m3uUrl)
                                onDismiss()
                            }
                        }
                        1 -> {
                            val name = playlistName.ifEmpty { "Xtream Paketi" }
                            val host = xtreamHost.trimEnd('/')
                            val finalUrl = "$host/get.php?username=$xtreamUser&password=$xtreamPass&type=m3u_plus&output=ts"
                            onAddM3u(name, finalUrl)
                            onDismiss()
                        }
                        2 -> {
                            val name = playlistName.ifEmpty { "STB Portal ($stbMac)" }
                            val host = stbHost.ifEmpty { "http://stalker.iptvportal.tv:8080/c" }
                            onAddStb(name, host, stbMac)
                            onDismiss()
                        }
                    }
                },
                enabled = when (selectedTab) {
                    0 -> m3uUrl.isNotEmpty()
                    1 -> xtreamHost.isNotEmpty() && xtreamUser.isNotEmpty()
                    2 -> stbMac.isNotEmpty()
                    else -> false
                }
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
