package com.example.comunicationwearmobile.utils.maps

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.comunicationwearmobile.R
import java.util.Random

class NotificationHelper(base: Context?) : ContextWrapper(base) {
    private val CHANNEL_NAME = "High priority channel"
    private val CHANNEL_ID = "com.example.notifications$CHANNEL_NAME"

    init {
        createChannels()
    }

    private fun createChannels() {
        val notificationChannel =
            NotificationChannel(CHANNEL_ID , CHANNEL_NAME , NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.enableLights(true)
        notificationChannel.enableVibration(true)
        notificationChannel.description = "this is the description of the channel."
        notificationChannel.lightColor = Color.RED
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(notificationChannel)
    }

    @SuppressLint("MissingPermission")
    @Synchronized
    fun sendHighPriorityNotification(title: String? , body: String? , activityName: Class<*>?) {
        val intent = Intent(this , activityName)
        val pendingIntent = PendingIntent.getActivity(
            this ,
            267 ,
            intent ,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val notification = NotificationCompat.Builder(this , CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(
                NotificationCompat.BigTextStyle().setSummaryText("summary")
                    .setBigContentTitle(title).bigText(body)
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(Random().nextInt() , notification)
    }

    companion object {
        private var instance: NotificationHelper? = null

        fun getInstance(base: Context?): NotificationHelper? {
            if (instance == null) {
                instance = NotificationHelper(base)
            }
            return instance
        }
    }
}
