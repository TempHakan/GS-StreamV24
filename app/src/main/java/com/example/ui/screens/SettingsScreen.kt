package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.TvFocusableCard
import com.example.ui.theme.AppThemeMode
import com.example.viewmodel.StreamUiState

@Composable
fun SettingsScreen(
    state: StreamUiState,
    onSetTheme: (AppThemeMode) -> Unit,
    onToggleProxy: (Boolean) -> Unit,
    onSetBackendUrl: (String) -> Unit,
    onToggleDefaultPlayer: (Boolean) -> Unit,
    onCheckForUpdates: () -> Unit,
    onSimulateVersionBump: () -> Unit,
    modifier: Modifier = Modifier
) {
    var backendInput by remember(state.backendUrl) { mutableStateOf(state.backendUrl) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Ayarlar & Sistem",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // 1. VK Sürüm Güncelleme Motoru Kartı (Requested in Prompt)
        TvFocusableCard(
            onClick = onCheckForUpdates,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "VK Sürüm Güncelleme Motoru",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Mevcut Versiyon: v2.4.0-${state.updateInfo?.currentTag ?: "VK01"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = state.updateInfo?.currentTag ?: "VK01",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Yeni versiyon geldiğinde VK01 -> VK02 -> VK03 şeklinde 1 artırılarak arka planda APK güncellemesi denetlenir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onCheckForUpdates,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Güncellemeyi Denetle")
                    }

                    OutlinedButton(
                        onClick = onSimulateVersionBump,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.AutoMode, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("VK +1 Artır (Test)")
                    }
                }
            }
        }

        // 2. Dinamik Tema Motoru
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Arayüz Teması",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Dark OLED, Temiz Açık ve Cyberpunk Neon seçenekleri",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.themeMode == AppThemeMode.DARK_OLED,
                        onClick = { onSetTheme(AppThemeMode.DARK_OLED) },
                        label = { Text("Dark OLED") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = state.themeMode == AppThemeMode.VIBRANT_NEON,
                        onClick = { onSetTheme(AppThemeMode.VIBRANT_NEON) },
                        label = { Text("Neon Glow") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = state.themeMode == AppThemeMode.CLEAN_LIGHT,
                        onClick = { onSetTheme(AppThemeMode.CLEAN_LIGHT) },
                        label = { Text("Açık Tema") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Backend Relay Proxy & Ağ
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Backend Stream Proxy",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "CORS ve User-Agent engellerini aşmak için backend tüneli kullanır.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.useProxy,
                        onCheckedChange = onToggleProxy
                    )
                }

                if (state.useProxy) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = backendInput,
                        onValueChange = {
                            backendInput = it
                            onSetBackendUrl(it)
                        },
                        label = { Text("Backend URL") },
                        placeholder = { Text("http://localhost:3000") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        // 4. Varsayılan Oynatıcı Seçimi (Media3 vs VLC)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Harici Oynatıcı Önceliği (VLC)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Kanallara tıklandığında dahili ExoPlayer yerine doğrudan VLC Player Intent'ini başlatır.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.defaultPlayerIsVlc,
                        onCheckedChange = onToggleDefaultPlayer
                    )
                }
            }
        }

        // 5. GitHub ve APK Hakkında Bilgi
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "GitHub ve APK Pipeline Bilgisi",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Proje, GitHub'a push edildiğinde `.github/workflows/build-apk.yml` üzerinden otomatik olarak APK çıktısı üretir ve Release oluşturur.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "• `./bump_version.sh` veya `gradle bumpVkVersion` ile sürüm VK01 -> VK02 olarak artırılır.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
