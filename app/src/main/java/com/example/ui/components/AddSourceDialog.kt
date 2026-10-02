package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSourceDialog(
    onDismiss: () -> Unit,
    onAddM3u: (name: String, url: String) -> Unit,
    onAddLocalM3u: (name: String, content: String) -> Unit,
    onAddXtream: (name: String, host: String, user: String, pass: String) -> Unit,
    onAddStb: (name: String, portalUrl: String, mac: String) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: M3U URL, 1: Yerel Dosya, 2: Xtream Codes, 3: STB / MAC Portal

    // Genel Değişkenler
    var playlistName by remember { mutableStateOf("") }

    // Tab 0: M3U URL
    var m3uUrl by remember { mutableStateOf("") }

    // Tab 1: Yerel Dosya (M3U / M3U8)
    var localFileName by remember { mutableStateOf("") }
    var localFileContent by remember { mutableStateOf("") }
    var detectedChannelCount by remember { mutableIntStateOf(0) }

    // Tab 2: Xtream Codes
    var xtreamHost by remember { mutableStateOf("") }
    var xtreamUser by remember { mutableStateOf("") }
    var xtreamPass by remember { mutableStateOf("") }

    // Tab 3: STB / MAC
    var stbHost by remember { mutableStateOf("") }
    var stbMac by remember { mutableStateOf("00:1A:79:B4:C2:A1") }

    fun generateRandomMac(): String {
        val randHex = { (0..255).random().toString(16).padStart(2, '0').uppercase() }
        return "00:1A:79:${randHex()}:${randHex()}:${randHex()}"
    }

    // Yerel M3U / M3U8 Dosya Seçici
    val localM3uFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val stream = context.contentResolver.openInputStream(uri)
                val content = stream?.bufferedReader()?.use { it.readText() } ?: ""
                if (content.isNotEmpty()) {
                    localFileContent = content
                    val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "yerel_liste.m3u"
                    localFileName = fileName
                    if (playlistName.isEmpty()) {
                        playlistName = fileName.substringBeforeLast('.')
                    }
                    detectedChannelCount = content.lines().count { it.trim().startsWith("#EXTINF:") }
                    Toast.makeText(context, "$fileName seçildi ($detectedChannelCount kanal tespit edildi)", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Dosya okunamadı: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Yerel Xtream / STB Dosya Seçici (.json / .txt / .m3u)
    val localConfigFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val stream = context.contentResolver.openInputStream(uri)
                val content = stream?.bufferedReader()?.use { it.readText() } ?: ""
                if (content.isNotEmpty()) {
                    // JSON formatı kontrolü
                    if (content.trim().startsWith("{")) {
                        val json = JSONObject(content)
                        if (json.has("host") || json.has("server")) {
                            xtreamHost = json.optString("host", json.optString("server", ""))
                            xtreamUser = json.optString("username", json.optString("user", ""))
                            xtreamPass = json.optString("password", json.optString("pass", ""))
                            if (playlistName.isEmpty()) playlistName = json.optString("name", "Xtream Hesabı")
                            Toast.makeText(context, "Xtream bilgileri dosyadan yüklendi", Toast.LENGTH_SHORT).show()
                        } else if (json.has("portal") || json.has("mac")) {
                            stbHost = json.optString("portal", json.optString("url", ""))
                            stbMac = json.optString("mac", "00:1A:79:B4:C2:A1")
                            if (playlistName.isEmpty()) playlistName = json.optString("name", "STB Portalı")
                            Toast.makeText(context, "STB bilgileri dosyadan yüklendi", Toast.LENGTH_SHORT).show()
                        }
                    } else if (content.contains("username=") && content.contains("password=")) {
                        // URL / Satır içi Xtream formatı
                        val hostMatch = Regex("""(https?://[^/]+)""").find(content)
                        val userMatch = Regex("""username=([^& \n\r]+)""").find(content)
                        val passMatch = Regex("""password=([^& \n\r]+)""").find(content)
                        if (hostMatch != null && userMatch != null && passMatch != null) {
                            xtreamHost = hostMatch.groupValues[1]
                            xtreamUser = userMatch.groupValues[1]
                            xtreamPass = passMatch.groupValues[1]
                            if (playlistName.isEmpty()) playlistName = "Xtream Hesabı"
                            Toast.makeText(context, "Xtream bağlantısı dosyadan ayrıştırıldı", Toast.LENGTH_SHORT).show()
                        }
                    } else if (content.contains("#EXTINF")) {
                        // M3U dosyası olarak algıla
                        localFileContent = content
                        val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "liste.m3u"
                        localFileName = fileName
                        playlistName = fileName.substringBeforeLast('.')
                        detectedChannelCount = content.lines().count { it.trim().startsWith("#EXTINF:") }
                        selectedTab = 1
                        Toast.makeText(context, "M3U listesi yüklendi ($detectedChannelCount kanal)", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Dosya okunamadı: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
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
                        text = { Text("M3U URL") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Yerel Dosya") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Xtream Codes") }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("STB / MAC") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    // 0: M3U URL
                    0 -> {
                        OutlinedTextField(
                            value = playlistName,
                            onValueChange = { playlistName = it },
                            label = { Text("Çalma Listesi Adı") },
                            placeholder = { Text("Örn: Web Canlı Yayınları") },
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

                    // 1: Yerel Dosyadan İçe Aktar (M3U / M3U8 / TXT)
                    1 -> {
                        OutlinedTextField(
                            value = playlistName,
                            onValueChange = { playlistName = it },
                            label = { Text("Çalma Listesi Adı") },
                            placeholder = { Text("Örn: Yerel M3U Paketi") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (localFileName.isNotEmpty()) localFileName else "Cihazınızdaki M3U / M3U8 dosyasını seçin",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (localFileName.isNotEmpty()) FontWeight.Bold else FontWeight.Normal
                                )

                                if (detectedChannelCount > 0) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "✓ $detectedChannelCount kanal tespit edildi",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { localM3uFilePicker.launch("*/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (localFileName.isNotEmpty()) "Dosyayı Değiştir" else "Dosya Yöneticisinden Seç")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "💡 Telefonunuzun 'İndirilenler' (Downloads) klasöründeki .m3u, .m3u8 veya .txt dosyalarını seçebilirsiniz.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 2: Xtream Codes API
                    2 -> {
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

                        Spacer(modifier = Modifier.height(10.dp))

                        // Yerel Xtream Dosyası Yükleme Butonu
                        OutlinedButton(
                            onClick = { localConfigFilePicker.launch("*/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Yerel Xtream Dosyasından Doldur (.json / .txt)")
                        }
                    }

                    // 3: STB / MAC Portal
                    3 -> {
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

                        // Yerel STB Dosyası Yükleme Butonu
                        OutlinedButton(
                            onClick = { localConfigFilePicker.launch("*/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Yerel STB Dosyasından Doldur (.json / .txt)")
                        }

                        Spacer(modifier = Modifier.height(6.dp))
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
                            val name = playlistName.ifEmpty { localFileName.ifEmpty { "Yerel M3U Listesi" } }
                            if (localFileContent.isNotEmpty()) {
                                onAddLocalM3u(name, localFileContent)
                                onDismiss()
                            }
                        }
                        2 -> {
                            val name = playlistName.ifEmpty { "Xtream Paketi" }
                            if (xtreamHost.isNotEmpty() && xtreamUser.isNotEmpty()) {
                                onAddXtream(name, xtreamHost, xtreamUser, xtreamPass)
                                onDismiss()
                            }
                        }
                        3 -> {
                            val name = playlistName.ifEmpty { "STB Portal ($stbMac)" }
                            val host = stbHost.ifEmpty { "http://stalker.iptvportal.tv:8080/c" }
                            onAddStb(name, host, stbMac)
                            onDismiss()
                        }
                    }
                },
                enabled = when (selectedTab) {
                    0 -> m3uUrl.isNotEmpty()
                    1 -> localFileContent.isNotEmpty()
                    2 -> xtreamHost.isNotEmpty() && xtreamUser.isNotEmpty()
                    3 -> stbMac.isNotEmpty()
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
