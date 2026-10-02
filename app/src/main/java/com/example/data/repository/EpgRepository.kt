package com.example.data.repository

import com.example.data.model.Channel
import com.example.data.model.EpgProgram
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.UUID

class EpgRepository {

    /**
     * Verilen kanallar için zaman tabanlı canlı EPG program akışını oluşturur ve getirir.
     * Günün saatine göre 'Şu Anda Yayında' olan ve geçmiş/gelecek programları dinamik hizalar.
     */
    suspend fun getEpgGridForChannels(channels: List<Channel>): Map<String, List<EpgProgram>> = withContext(Dispatchers.Default) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentTotalMinutes = currentHour * 60 + currentMinute

        val resultMap = mutableMapOf<String, List<EpgProgram>>()

        channels.forEach { channel ->
            val programs = generateChannelSchedule(channel, currentTotalMinutes)
            resultMap[channel.id] = programs
        }

        resultMap
    }

    private fun generateChannelSchedule(channel: Channel, currentTotalMinutes: Int): List<EpgProgram> {
        val channelName = channel.name.lowercase()
        val group = channel.groupTitle.lowercase()

        // Kanal türüne göre program listesi şablonu
        val scheduleTemplates = when {
            channelName.contains("spor") || group.contains("spor") -> listOf(
                Pair("Sabah Sporu & Günün Manşetleri", "Spor"),
                Pair("Süper Lig & Avrupa'dan Goller", "Spor"),
                Pair("Transfer Dosyası & Analiz Masası", "Spor"),
                Pair("Günün Karşılaşması Öncesi Canlı Yayın", "Spor"),
                Pair("Canlı Maç & Canlı Skor Takibi", "Spor"),
                Pair("Stüdyoda Maç Sonu Değerlendirmeleri", "Spor"),
                Pair("Gece Maç Özetleri & Özel Röportajlar", "Spor")
            )
            channelName.contains("haber") || group.contains("haber") -> listOf(
                Pair("Güne Bakış & Sabah Haberleri", "Haber"),
                Pair("Ekonomi Dünyası & Piyasa Açılışı", "Haber"),
                Pair("Günün Sıcak Gelişmeleri & Canlı Bağlantılar", "Haber"),
                Pair("Öğle Bülteni & Dünya Gündemi", "Haber"),
                Pair("Türkiye ve Dünyadan Başlıklar", "Haber"),
                Pair("Ana Haber Bülteni (Canlı)", "Haber"),
                Pair("Açık Oturum & Siyaset Masası", "Haber"),
                Pair("Gece Raporu & Son Dakika", "Haber")
            )
            channelName.contains("belgesel") || group.contains("belgesel") -> listOf(
                Pair("Vahşi Yaşamın Gizemleri", "Belgesel"),
                Pair("Okyanusun Derinlikleri", "Belgesel"),
                Pair("Evrenin Doğuşu & Uzay Çağı", "Belgesel"),
                Pair("Tarihi Değiştiren Savaşlar", "Belgesel"),
                Pair("Dünyanın En Zorlu Yolları", "Belgesel"),
                Pair("Mega Yapılar & Mühendislik Harikaları", "Belgesel"),
                Pair("Gezegenimiz: Kutup Bölgeleri", "Belgesel")
            )
            channelName.contains("sinema") || channelName.contains("film") || group.contains("sinema") -> listOf(
                Pair("Sabah Sineması: Klasikler Kuşağı", "Sinema"),
                Pair("Aile Kuşağı: Sevimli Dostlar", "Sinema"),
                Pair("Macera Filmi: Gizli Hazine", "Sinema"),
                Pair("Aksiyon Zamanı: Son Görev", "Sinema"),
                Pair("Akşam Kuşağı: Gişe Rekortmeni Film", "Sinema"),
                Pair("Gerilim Sineması: Kaçış Planı", "Sinema"),
                Pair("Gece Sineması: Bilim Kurgu Başyapıtı", "Sinema")
            )
            else -> listOf(
                Pair("Güne Merhaba & Sabah Magazin", "Genel"),
                Pair("Mutfakta Ne Var? Yemek Programı", "Yemek"),
                Pair("Gündüz Kuşağı Canlı Eğlence", "Eğlence"),
                Pair("Öğleden Sonra Dizisi (Tekrar)", "Dizi"),
                Pair("Hava Durumu & Yol Durumu", "Bilgi"),
                Pair("Ana Haber Bülteni (Canlı)", "Haber"),
                Pair("Akşamın Yıldızı: Yeni Bölüm Dizi", "Dizi"),
                Pair("Komedi & Talk Show Özel", "Eğlence"),
                Pair("Gece Sineması", "Sinema")
            )
        }

        val programs = mutableListOf<EpgProgram>()
        // Günün başlangıcı: 06:00 (360. dakika) ile 24:00+ arası
        var currentSlotMinutes = 6 * 60 // 06:00

        var templateIdx = 0
        while (currentSlotMinutes < 24 * 60 + 60) {
            val template = scheduleTemplates[templateIdx % scheduleTemplates.size]
            val durationMinutes = when (template.second) {
                "Sinema" -> 120
                "Dizi" -> 105
                "Spor" -> 90
                "Haber" -> 60
                else -> if (templateIdx % 2 == 0) 60 else 45
            }

            val endSlotMinutes = currentSlotMinutes + durationMinutes

            val startH = (currentSlotMinutes / 60) % 24
            val startM = currentSlotMinutes % 60
            val endH = (endSlotMinutes / 60) % 24
            val endM = endSlotMinutes % 60

            val startTimeStr = String.format("%02d:%02d", startH, startM)
            val endTimeStr = String.format("%02d:%02d", endH, endM)

            val isLiveNow = currentTotalMinutes in currentSlotMinutes until endSlotMinutes
            val progress = if (isLiveNow) {
                ((currentTotalMinutes - currentSlotMinutes).toFloat() / durationMinutes.toFloat()).coerceIn(0.05f, 0.95f)
            } else if (currentTotalMinutes >= endSlotMinutes) {
                1.0f
            } else {
                0.0f
            }

            programs.add(
                EpgProgram(
                    id = "epg_${channel.id}_$currentSlotMinutes",
                    channelId = channel.id,
                    title = if (isLiveNow && channel.currentProgram != null) channel.currentProgram else template.first,
                    description = "${channel.name} ekranlarında yayınlanan ${template.first}. HD çözünürlük ve kesintisiz akış desteği ile canlı yayın.",
                    startTime = startTimeStr,
                    endTime = endTimeStr,
                    progress = progress,
                    startMinutes = currentSlotMinutes,
                    durationMinutes = durationMinutes,
                    category = template.second,
                    isLiveNow = isLiveNow,
                    canCatchUp = currentTotalMinutes > currentSlotMinutes
                )
            )

            currentSlotMinutes = endSlotMinutes
            templateIdx++
        }

        return programs
    }
}
