package com.example.comunicationwearmobile.utils.mannager

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.view.jetpackCompose.main.TAG
import com.example.comunicationwearmobile.utils.sendMessageMobile
import com.example.shared_library.SharedData
import com.example.shared_library.toByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("LaunchActivityFromNotification")
class NotificationMannager() {
    fun showNotification(context: Context, msgFallDetection: SharedData.MsgFallDetection) {
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = 1
        val channelId = "health_event_channel"

        try {
            val intent = Intent(context, BroadcastAcceptNotification::class.java)
            intent.putExtra(
                SharedData.ParamIntent.MESSAGE_PATH.name,
                SharedData.PATH_FALL_DETECTION
            )
            intent.putExtra(SharedData.ParamIntent.MESSAGE_BODY.name, toByteArray(msgFallDetection))

            val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )


            // Crear canal de notificación para versiones de Android O y superiores
            val channel = NotificationChannel(
                channelId,
                "Eventos de Salud",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "AbuMonitor"

            }
            notificationManager.createNotificationChannel(channel)

            val notificationBuilder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_old_people)
                .setContentTitle(msgFallDetection.title)
                .setContentText(msgFallDetection.message + " " + msgFallDetection.fechaHora)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)

            notificationManager.notify(notificationId, notificationBuilder.build())
        } catch (e: Exception) {
            Log.i(TAG, "Error al generar la notificacion" + e.message)
        }
    }
}


class BroadcastAcceptNotification : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val path    = intent.getStringExtra(SharedData.ParamIntent.MESSAGE_PATH.name)
        val message = intent.getByteArrayExtra(SharedData.ParamIntent.MESSAGE_BODY.name)
        // 3. Iniciar una corutina para realizar la acción deseada
        CoroutineScope(Dispatchers.IO).launch {
            sendMessageMobile(context, path.toString() ,message)
        }
    }

}
