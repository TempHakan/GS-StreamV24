package com.example.player

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object VlcIntentHelper {

    /**
     * Kullanıcı "VLC ile Aç" dediğinde org.videolan.vlc paketine doğrudan Intent yollar.
     * Cihazda VLC yüklü değilse sistem genel oynatıcı seçicisini (Chooser) açar.
     */
    fun launchVlc(context: Context, streamUrl: String, title: String) {
        val uri = Uri.parse(streamUrl)
        val vlcIntent = Intent(Intent.ACTION_VIEW).apply {
            setPackage("org.videolan.vlc")
            setDataAndTypeAndNormalize(uri, "video/*")
            putExtra("title", title)
            putExtra("from_start", true)
            putExtra("position", 0L)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(vlcIntent)
        } catch (e: ActivityNotFoundException) {
            // VLC yüklü değilse genel oynatıcı seçiciyi aç ve kullanıcıyı bilgilendir
            Toast.makeText(context, "VLC bulunamadı, alternatif oynatıcı açılıyor...", Toast.LENGTH_SHORT).show()
            val genericIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndTypeAndNormalize(uri, "video/*")
                putExtra("title", title)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(Intent.createChooser(genericIntent, "Oynatıcı Seçin (VLC, MX Player vb.)"))
            } catch (ex: Exception) {
                Toast.makeText(context, "Akış harici bir oynatıcıda açılamadı.", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Herhangi bir harici oynatıcı için intent seçici açar
     */
    fun launchExternalChooser(context: Context, streamUrl: String, title: String) {
        val uri = Uri.parse(streamUrl)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndTypeAndNormalize(uri, "video/*")
            putExtra("title", title)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Medya Oynatıcı Seçin"))
        } catch (e: Exception) {
            Toast.makeText(context, "Uyumlu harici oynatıcı bulunamadı.", Toast.LENGTH_SHORT).show()
        }
    }
}
