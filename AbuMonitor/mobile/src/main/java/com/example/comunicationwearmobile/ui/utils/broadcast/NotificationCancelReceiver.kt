package com.example.comunicationwearmobile.ui.utils.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.comunicationwearmobile.ui.utils.Mannager.NotificationManagerHelper
import com.example.shared_library.SharedData

// BroadcastReceiver para manejar la cancelación de notificaciones
// Esto se hace aca dentro porque sino no puedo decrementar el contador de notificaciones en
// el pending intent
class NotificationCancelReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val notificationId= intent.getIntExtra(SharedData.PARAM_PENDING_INTENT_NOTIFICATION_ID,0    )
        val notificationManagerHelper = NotificationManagerHelper.getInstance(context)
        val posNotificationId = notificationManagerHelper?.deleteNewNotificationById(notificationId)

        // Decrementar el contador de notificaciones activas
        //Utils.sendMessageToService(context,SharedData.PATH_VIEWED_NOTIFICATION,posNotificationId)
    }
}