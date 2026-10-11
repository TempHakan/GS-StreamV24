package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onToggleAutoUpdate: (Boolean) -> Unit,
    savedToken: String = "",
    onSaveToken: (String) -> Unit = {},
    savedRepo: String = "vyslkrc/streamflow-iptv",
    onSaveRepo: (String) -> Unit = {},
    onOpenReleases: () -> Unit = {},
    onToggleParentalEnable: (enabled: Boolean, pin: String) -> Unit = { _, _ -> },
    onUpdateParentalPin: (newPin: String) -> Unit = {},
    onToggleCategoryLock: (category: String) -> Unit = {},
    onLockSessionNow: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var backendInput by remember(state.backendUrl) { mutableStateOf(state.backendUrl) }
    var tokenInput by remember(savedToken) { mutableStateOf(savedToken) }
    var repoInput by remember(savedRepo) { mutableStateOf(savedRepo) }

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
                    text = "Yeni versiyon geldiğinde VK01 -> VK02 -> VK03 şeklinde 1 artırılarak GitHub üzerinden APK güncellemesi denetlenir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "GitHub Otomatik Güncelleme",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Yeni sürüm çıktığında telefonunuza otomatik indirip kurar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.autoInstallUpdates,
                        onCheckedChange = onToggleAutoUpdate
                    )
                }

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

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "🔒 Özel (Private) Depo Erişimi & GitHub Token",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "GitHub reponuz Private (Gizli) ise güncellemelerin doğrudan indirilebilmesi için GitHub Personal Access Token (classic 'repo' yetkili) ekleyebilir veya doğrudan tarayıcıdan indirebilirsiniz:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                // GitHub Depo Adı (kullanıcı/repo)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = repoInput,
                        onValueChange = { repoInput = it },
                        label = { Text("GitHub Deposu (kullanıcı/repo)") },
                        placeholder = { Text("vyslkrc/streamflow-iptv") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSaveRepo(repoInput)
                            android.widget.Toast.makeText(context, "Depo adı kaydedildi: $repoInput", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Kaydet")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = tokenInput,
                        onValueChange = { tokenInput = it },
                        label = { Text("GitHub Token (ghp_... veya token)") },
                        placeholder = { Text("Özel repo için token yapıştırın") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSaveToken(tokenInput)
                            android.widget.Toast.makeText(context, "GitHub Token kaydedildi", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Kaydet")
                    }
                }

                // Son kontrol teşhis durumu
                if (!state.updateInfo?.lastCheckStatus.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "ℹ️ Son Denetim: ${state.updateInfo?.lastCheckStatus}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onOpenReleases,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tarayıcıda GitHub Releases Sayfasını Aç (Giriş Yapılmış)")
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

        // 3. Ebeveyn Denetimi & PIN Kilidi
        var showParentalDialog by remember { mutableStateOf(false) }

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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (state.isParentalControlEnabled) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = if (state.isParentalControlEnabled) MaterialTheme.colorScheme.primary else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ebeveyn Denetimi & PIN Kilidi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = if (state.isParentalControlEnabled)
                                "Aktif • ${state.lockedCategories.size} kategori kilitli"
                            else
                                "Kapalı • Kanalları ve kategorileri PIN ile koruyun",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = state.isParentalControlEnabled,
                        onCheckedChange = { showParentalDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showParentalDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PIN & Kilitleri Yönet", style = MaterialTheme.typography.labelMedium)
                    }

                    if (state.isSessionUnlocked) {
                        Button(
                            onClick = onLockSessionNow,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Şimdi Kilitle", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        if (showParentalDialog) {
            com.example.ui.components.ParentalSettingsDialog(
                isEnabled = state.isParentalControlEnabled,
                lockedCategories = state.lockedCategories,
                availableCategories = state.categories,
                onDismiss = { showParentalDialog = false },
                onToggleEnable = { enabled, pin ->
                    onToggleParentalEnable(enabled, pin)
                },
                onUpdatePin = onUpdateParentalPin,
                onToggleCategoryLock = onToggleCategoryLock
            )
        }

        // 4. Backend Relay Proxy & Ağ
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

        // 5. Firebase Crashlytics & Uygulama Kararlılığı
        var showRecentLogs by remember { mutableStateOf(false) }
        var crashlyticsCollectionEnabled by remember { mutableStateOf(true) }
        val recentLogs = remember { com.example.util.CrashlyticsManager.recentLogs }

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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (crashlyticsCollectionEnabled) Color(0xFF4CAF50) else Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Firebase Crashlytics İzleme",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Çalışma zamanı çökmelerini ve istisnaları gerçek zamanlı raporlar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = crashlyticsCollectionEnabled,
                        onCheckedChange = {
                            crashlyticsCollectionEnabled = it
                            com.example.util.CrashlyticsManager.setCollectionEnabled(it)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Yapı ve Sürüm Tanımlayıcıları
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Sürüm Etiketi: ${com.example.BuildConfig.VK_TAG} (Kod: ${com.example.BuildConfig.VK_CODE})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Uygulama: StreamFlow IPTV v${com.example.BuildConfig.APP_VERSION_NAME}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                com.example.util.CrashlyticsManager.recordException(
                                    throwable = IllegalStateException("Test non-fatal exception from SettingsScreen (${com.example.BuildConfig.VK_TAG})"),
                                    tag = "Manual_Test",
                                    extra = mapOf(
                                        "source" to "SettingsScreen",
                                        "timestamp" to System.currentTimeMillis()
                                    )
                                )
                                android.widget.Toast.makeText(context, "Test istisnası Crashlytics'e iletildi!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Hatası Kaydet", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Ekmek Kırıntıları / Olay Günlüğü Toggle
                TextButton(
                    onClick = { showRecentLogs = !showRecentLogs },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = if (showRecentLogs) "▲ Günlük Olay Kayıtlarını Gizle" else "▼ Günlük Olay Kayıtlarını Göster (${com.example.util.CrashlyticsManager.recentLogs.size})",
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                if (showRecentLogs) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = Color.Black.copy(alpha = 0.85f),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            com.example.util.CrashlyticsManager.recentLogs.reversed().forEach { logLine ->
                                Text(
                                    text = logLine,
                                    color = if (logLine.contains("❌") || logLine.contains("🚨") || logLine.contains("⚠️")) Color(0xFFFF5252) else Color(0xFFB0BEC5),
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. GitHub ve APK Hakkında Bilgi
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
                    text = "• Proje, GitHub'a push edildiğinde `.github/workflows/build-release.yml` üzerinden otomatik olarak APK çıktısı üretir, imzalar ve Taslak Release oluşturur.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "• Firebase Crashlytics aktif edilmiştir. Çalışma zamanı hataları otomatik olarak raporlanır.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
