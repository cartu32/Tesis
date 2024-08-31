package com.example.comunicationwearmobile.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.comunicationwearmobile.R

fun showNotification(context: Context) {
    val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    val notificationId = 1
    val channelId = "health_event_channel"

    // Crear canal de notificación para versiones de Android O y superiores
    val channel = NotificationChannel(channelId, "Eventos de Salud", NotificationManager.IMPORTANCE_HIGH).apply {
        description = "Notificación de eventos de salud"
    }
    notificationManager.createNotificationChannel(channel)

    val notificationBuilder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.splash_icon)
        .setContentTitle("Caida Detectada")
        .setContentText("Se ha detectado un evento de caída.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    notificationManager.notify(notificationId, notificationBuilder.build())
}
