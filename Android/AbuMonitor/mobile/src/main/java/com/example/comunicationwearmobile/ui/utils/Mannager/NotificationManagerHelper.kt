package com.example.comunicationwearmobile.ui.utils.Mannager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.example.comunicationwearmobile.R

class NotificationManagerHelper(base: Context?) : ContextWrapper(base) {


    init {
        //creo un canal de notificaciones exclusivo para el foregroundservices
        createChannelForegroundServices()
        createChannelAlerts()
    }

    private fun createChannelAlerts() {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                CHANNEL_ALERTS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal de Alertas"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(notificationChannel)
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
            .setSmallIcon(R.drawable.ic_old_person)
            .setOngoing(true) // Esto hace que no pueda eliminarse
            .build()
    }

    public fun createAlertNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID_ALERTS)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_old_person)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Se elimina al tocar la notificación
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(ALERT_NOTIFICATION_ID, notification)
    }


    companion object {
        const val CHANNEL_FOREGROUND_SERVICE    = "Foregroundservice"
        const val CHANNEL_ID_FOREGROUND_SERVICE = "Channel_ID_ForegroundService"

        const val CHANNEL_ALERTS = "Alertas"
        const val CHANNEL_ID_ALERTS = "Channel_ID_Alerts"
        const val ALERT_NOTIFICATION_ID = 1001 // ID para diferenciar esta notificación

        private var instance: NotificationManagerHelper? = null

        fun getInstance(base: Context): NotificationManagerHelper? {
            if (instance == null) {
                instance = NotificationManagerHelper(base)
            }
            return instance
        }
    }

}
