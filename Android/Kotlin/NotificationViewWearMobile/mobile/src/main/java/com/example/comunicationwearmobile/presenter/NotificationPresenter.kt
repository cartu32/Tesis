package com.example.comunicationwearmobile.presenter

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.common.Utils
import com.example.comunicationwearmobile.models.SpNotificationCounter
import com.example.comunicationwearmobile.models.WearableDataListenerService
import com.example.shared_library.SharedData
import com.example.shared_library.toByteArray

@Suppress("UNREACHABLE_CODE")
class NotificationPresenter private constructor(context: Context) {

    private val GROUP_KEY_NOTIFICATION = "GROUP_NOTIFICATION"
    private val CHANNEL_ID = "CHANNEL_ID_NOTIFICATION"
    private val CHANNEL_NAME = "CHANNEL_NAME_NOTIFICATION"
    private val CHANNEL_DESCRIPTION = "CHANNEL_DESCRIPTION_NOTIFICATION"
    private val PATTERN_VIBRATION: LongArray = longArrayOf(0, 1000, 500, 1000)
    private val appContext: Context = context.applicationContext



    companion object {
        @Volatile
        private var INSTANCE: NotificationPresenter? = null

        fun getInstance(context: Context): NotificationPresenter {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NotificationPresenter(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    fun showNotification(msg: SharedData.MsgNotification) {
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationBuilder = createChannel(notificationManager)

        // Crear y notificar la notificación del grupo
        val groupNotificationBuilder = createGroupNotification()
        notificationManager.notify(0, groupNotificationBuilder.build())

        val notificationId=SpNotificationCounter.increment(appContext)

        // Crear y notificar una notificación individual
        createNotification(notificationBuilder,msg,notificationId)
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun createGroupNotification(): NotificationCompat.Builder {
        return NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.old_person)
            .setLargeIcon(BitmapFactory.decodeResource(appContext.resources, R.drawable.old_person))
            .setContentTitle("Grupo de notificaciones")
            .setContentText("Usted tiene algunas alertas pendientes por leer")
            .setGroup(GROUP_KEY_NOTIFICATION)
            .setGroupSummary(true)
            .setAutoCancel(true)
    }

    @SuppressLint("LaunchActivityFromNotification")
    private fun createNotification(
        notificationBuilder: NotificationCompat.Builder ,
        msg: SharedData.MsgNotification ,
        notificationId: Int
    ) {

       val cancelNotificationIntent = Intent(appContext, NotificationCancelReceiver::class.java)

        val cancelPendingIntent = PendingIntent.getBroadcast(appContext, notificationId, cancelNotificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        notificationBuilder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.old_person)
            .setLargeIcon(BitmapFactory.decodeResource(appContext.resources, R.drawable.old_person))
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

    }

    private fun createChannel(notificationManager: NotificationManager): NotificationCompat.Builder {
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

        return NotificationCompat.Builder(appContext, CHANNEL_ID)
    }


}

// BroadcastReceiver para manejar la cancelación de notificaciones
// Esto se hace aca dentro porque sino no puedo decrementar el contador de notificaciones en
// el pending intent
class NotificationCancelReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
            val notificationId=SpNotificationCounter.decrement(context)
            // Decrementar el contador de notificaciones activas
            Utils.sendMessageToService(context,SharedData.PATH_VIEWED_NOTIFICATION,notificationId)
     }
}

