package com.example.comunicationwearmobile.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import androidx.core.app.NotificationCompat
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.models.FallEventData

fun showNotification(context: Context , eventData: FallEventData) {
    val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    val notificationId = 1
    val channelId = "health_event_channel"

    // Crear canal de notificación para versiones de Android O y superiores
    val channel = NotificationChannel(channelId, "Eventos de Salud", NotificationManager.IMPORTANCE_HIGH).apply {
        description = "AbuMonitor"
    }
    notificationManager.createNotificationChannel(channel)

    val notificationBuilder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_old_people)
        .setContentTitle("AbuMonitor")
        .setContentText("Se ha detectado un evento de caída "+eventData.eventTime)
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    notificationManager.notify(notificationId, notificationBuilder.build())
}
