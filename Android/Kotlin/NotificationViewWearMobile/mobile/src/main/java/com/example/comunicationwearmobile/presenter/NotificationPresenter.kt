package com.example.comunicationwearmobile.presenter

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.MainActivity

import java.util.Random


class NotificationPresenter private constructor() {

    private val CHANNEL_ID = "NOTIFICATION_URGENT_ID"
    private val CHANNEL_NAME = "My Notifications"
    private val CHANNEL_DESCRIPTION = "Channel description"

    fun generateNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel = NotificationChannel(
            CHANNEL_ID , CHANNEL_NAME , NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.description = CHANNEL_DESCRIPTION
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.vibrationPattern = longArrayOf(0 , 1000 , 500 , 1000)
        notificationChannel.enableVibration(true)
        notificationChannel.setShowBadge(true)

        notificationManager.createNotificationChannel(notificationChannel)

        val notificationBuilder = NotificationCompat.Builder(context , CHANNEL_ID)

        notificationBuilder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.old_person)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.old_person))
            .setTicker("Mensajes")
            .setContentTitle("Titulo")
            .setContentIntent(onClick(context))
            .setContentText("Esta es una descripción")
            .setContentInfo("New")
            .setLocalOnly(true)

        val random = Random()
        val m = random.nextInt(9999 - 1000) + 1000
        notificationManager.notify(m , notificationBuilder.build())
    }

    private fun onClick(context: Context): PendingIntent {
        val notificationIntent = Intent(context ,MainActivity::class.java)
        notificationIntent.putExtra("age" , "13")
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        return PendingIntent.getActivity(
            context ,
            0 ,
            notificationIntent ,
            PendingIntent.FLAG_IMMUTABLE
        )
    }
    companion object {
        @Volatile
        private var instance: NotificationPresenter? = null

        fun getInstance(): NotificationPresenter =
            instance ?: synchronized(this) {
                instance ?: NotificationPresenter().also { instance = it }
            }
    }
}