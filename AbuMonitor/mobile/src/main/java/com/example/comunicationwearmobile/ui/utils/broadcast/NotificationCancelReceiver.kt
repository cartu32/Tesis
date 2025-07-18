package com.example.comunicationwearmobile.ui.utils.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDispatcherWearable
import com.example.comunicationwearmobile.ui.utils.Helpers.NotificationHelper
import com.example.shared_library.SharedData

// BroadcastReceiver para manejar la cancelación de notificaciones
// Esto se hace aca dentro porque sino no puedo decrementar el contador de notificaciones en
// el pending intent
class NotificationCancelReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(SharedData.PARAM_PENDING_INTENT_NOTIFICATION_ID, 0)

        val notificationManagerHelper = NotificationHelper.getInstance(context.applicationContext)
        manageNotificationCancel(context, notificationManagerHelper, notificationId)
        Log.d(Definition.TAG_DEBUG, "Corutina del BroadcastReceiver finalizada")
     }

    private fun manageNotificationCancel(
        context: Context,
        notificationManagerHelper: NotificationHelper?,
        notificationId: Int
    ) {
        //pregunto si se elimino el grupo de notificaciones o solamente una sola notificacion
        if (notificationId != SharedData.GROUP_ID_NOTIFICATION) {
            //si se elimino una sola notificacion entonces se elimina el shared preference
            // y se obtiene el orden en que fue creada la notificacion
            notificationManagerHelper?.deleteNewNotificationById(notificationId)
        }

        val msg = SharedData.MsgNotification(
            "",
            "",
            SharedData.TypeNotification.WithoutNotifications,
            "",
            "",
            notificationId)

        //envio la posicion de la notificacion al wearable
        RepositoryDispatcherWearable.sendDataToWearable(context, SharedData.PATH_VIEWED_NOTIFICATION,msg)
    }
}