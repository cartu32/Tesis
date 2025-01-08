package com.example.comunicationwearmobile.ui.utils.Mannager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import androidx.core.app.NotificationCompat
import com.example.comunicationwearmobile.R

class NotificationManagerHelper(base: Context?) : ContextWrapper(base) {


    init {
        //creo un canal de notificaciones exclusivo para el foregroundservices
        createChannelForegroundServices()
    }

    public fun createChannelForegroundServices() {
        val notificationChannel =
            NotificationChannel(
                CHANNEL_ID_FOREGROUND_SERVICE ,
                CHANNEL_FOREGROUND_SERVICE ,
                NotificationManager.IMPORTANCE_DEFAULT
            )
        notificationChannel.description = "Canal de Notificacion del Foregroundservice"
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(notificationChannel)
    }

    // Crea la notifcación del foregroundservice
    public fun createNotificationForegroundService(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID_FOREGROUND_SERVICE)
            .setContentTitle("AbuMonitor")
            .setContentText("Ejecutando AbuMonitor en primer plano...")
            .setSmallIcon(R.drawable.old_person)
            .setOngoing(true) // Esto hace que no pueda eliminarse
            .build()
    }


    companion object {
        const val CHANNEL_FOREGROUND_SERVICE    = "Foregroundservice"
        const val CHANNEL_ID_FOREGROUND_SERVICE = "Channel_ID_ForegroundService"

        private var instance: NotificationManagerHelper? = null

        fun getInstance(base: Context): NotificationManagerHelper? {
            if (instance == null) {
                instance = NotificationManagerHelper(base)
            }
            return instance
        }
    }
}
