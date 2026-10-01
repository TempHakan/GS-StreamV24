package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Channel
import com.example.data.model.EpgProgram

@Composable
fun EpgGuideDialog(
    channel: Channel?,
    onDismiss: () -> Unit,
    onPlay: () -> Unit
) {
    if (channel == null) return

    val samplePrograms = listOf(
        EpgProgram(
            id = "epg_1",
            channelId = channel.id,
            title = channel.currentProgram ?: "Canlı Akış & Yayın Akışı",
            description = "Canlı stüdyo yayınları, son dakika gelişmeleri ve HD canlı aktarım.",
            startTime = "14:00",
            endTime = "16:30",
            progress = channel.programProgress
        ),
        EpgProgram(
            id = "epg_2",
            channelId = channel.id,
            title = "Ana Haber Bülteni ve Günün Özeti",
            description = "Günün tüm sıcak gelişmeleri, canlı bağlantılar ve özel röportajlar.",
            startTime = "16:30",
            endTime = "18:00",
            progress = 0.0f
        ),
        EpgProgram(
            id = "epg_3",
            channelId = channel.id,
            title = "Akşam Kuşağı & Prime Time Özel",
            description = "En çok izlenen dizi, sinema veya canlı spor karşılaşması.",
            startTime = "18:00",
            endTime = "21:00",
            progress = 0.0f
        ),
        EpgProgram(
            id = "epg_4",
            channelId = channel.id,
            title = "Gece Sineması (Catch-up / Geri Sarılabilir)",
            description = "Kaçırdığınız programları 7 güne kadar geriye sararak izleyebilirsiniz.",
            startTime = "21:00",
            endTime = "23:30",
            progress = 0.0f
        )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EventNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "${channel.name} - EPG Rehberi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Kategori: ${channel.groupTitle} | 7 Gün Catch-up Destekli",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                samplePrograms.forEachIndexed { index, prog ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (index == 0) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${prog.startTime} - ${prog.endTime}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (index == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (index == 0) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = MaterialTheme.shapes.extraSmall
                                    ) {
                                        Text(
                                            text = "CANLI YAYINDA",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = prog.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = prog.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (index == 0 && prog.progress > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { prog.progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(CircleShape),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onDismiss()
                onPlay()
            }) {
                Text("Kanalı Başlat")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        }
    )
}
