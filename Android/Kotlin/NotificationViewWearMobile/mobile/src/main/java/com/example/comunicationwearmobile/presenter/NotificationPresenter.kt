package com.example.comunicationwearmobile.presenter

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.common.Utils
import com.example.comunicationwearmobile.models.WearableDataListenerService
import com.example.comunicationwearmobile.ui.MainActivity
import com.example.shared_library.SharedData
import com.example.shared_library.toByteArray

import java.util.Random
import kotlin.math.log

@Suppress("UNREACHABLE_CODE")
class NotificationPresenter private constructor() {

    private val GROUP_KEY_NOTIFICATION = "GROUP_NOTIFICATION"
    private val CHANNEL_ID = "CHANNEL_ID_NOTIFICATION"
    private val CHANNEL_NAME = "CHANNEL_NAME_NOTIFICATION"
    private val CHANNEL_DESCRIPTION = "CHANNEL_DESCRIPTION_NOTIFICATION"
    private val PATTERN_VIBRATION: LongArray = longArrayOf(0, 1000, 500, 1000)

    private var countActiveNotifications:Int=0
    fun showNotification(context: Context , msg: SharedData.MsgNotification) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationBuilder = createChannel(context, notificationManager)

        // Crear y notificar la notificación del grupo
        val groupNotificationBuilder = createGroupNotification(context)
        notificationManager.notify(0, groupNotificationBuilder.build())

        // Crear y notificar una notificación individual
        createNotification(context, notificationBuilder,msg)
        notificationManager.notify(countActiveNotifications, notificationBuilder.build())
    }

    private fun createGroupNotification(context: Context): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.old_person)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.old_person))
            .setContentTitle("Grupo de notificaciones")
            .setContentText("Usted tiene algunas alertas pendientes por leer")
            .setGroup(GROUP_KEY_NOTIFICATION)
            .setGroupSummary(true)
            .setAutoCancel(true)
    }

    @SuppressLint("LaunchActivityFromNotification")
    private fun createNotification(
        context: Context ,
        notificationBuilder: NotificationCompat.Builder ,
        msg: SharedData.MsgNotification
    ) {

       val cancelNotificationIntent = Intent(context, WearableDataListenerService::class.java).apply {
            putExtra(SharedData.ParamIntent.MESSAGE_PATH.name,SharedData.PATH_VIEWED_NOTIFICATION)
            putExtra(SharedData.ParamIntent.MESSAGE_BODY.name, toByteArray( countActiveNotifications))
       }
        val cancelPendingIntent = PendingIntent.getService(context, countActiveNotifications, cancelNotificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        notificationBuilder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.old_person)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.old_person))
            .setContentIntent(cancelPendingIntent)
            .setDeleteIntent(cancelPendingIntent)
            .setGroup(GROUP_KEY_NOTIFICATION)
            .setLocalOnly(true)
            .setTicker("Mensajes")
            .setContentTitle(msg.title)
            .setStyle(NotificationCompat.InboxStyle()
                .addLine(msg.message)
                .addLine("")
                .addLine("Fecha: "+msg.date+"    Hora: " +msg.hour))

        countActiveNotifications++
    }

    private fun createChannel(context: Context, notificationManager: NotificationManager): NotificationCompat.Builder {
        val notificationChannel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.description = CHANNEL_DESCRIPTION
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.vibrationPattern = PATTERN_VIBRATION
        notificationChannel.enableVibration(true)
        notificationChannel.setShowBadge(true)

        notificationManager.createNotificationChannel(notificationChannel)

        return NotificationCompat.Builder(context, CHANNEL_ID)
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
