package com.example.comunicationwearmobile.ui.utils.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDispatcherWearable
import com.example.comunicationwearmobile.ui.utils.Mannager.NotificationManagerHelper
import com.example.shared_library.SharedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

// BroadcastReceiver para manejar la cancelación de notificaciones
// Esto se hace aca dentro porque sino no puedo decrementar el contador de notificaciones en
// el pending intent
class NotificationCancelReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(SharedData.PARAM_PENDING_INTENT_NOTIFICATION_ID, 0)
        val notificationManagerHelper =
            NotificationManagerHelper.getInstance(context.applicationContext)

     //   CoroutineScope(Dispatchers.IO).launch {
      //      try {
        //        withTimeout(10_000) {
                    manageNotificationCancel(context, notificationManagerHelper, notificationId)
                    Log.d(Definition.TAG_DEBUG, "Corutina del BroadcastReceiver finalizada")
          /*      }
            } catch (e: TimeoutCancellationException) {
                Log.d(Definition.TAG_DEBUG, "Corutina del BroadcastReceiver cancelada por timeout")
            } catch (e: Exception) {
                Log.e(Definition.TAG_DEBUG, "Error en el BroadcastReceiver", e)
            }
        }*/
    }

    private fun manageNotificationCancel(
        context: Context,
        notificationManagerHelper: NotificationManagerHelper?,
        notificationId: Int
    ) {
        var posNotificationId:Int?=null
        //pregunto si se elimino el grupo de notificaciones o solamente una sola notificacion
        if (notificationId != SharedData.GROUP_ID_NOTIFICATION) {
            //si se elimino una sola notificacion entonces se elimina el shared preference
            // y se obtiene el orden en que fue creada la notificacion
            posNotificationId = notificationManagerHelper?.deleteNewNotificationById(notificationId)
        }else{
            //si se elimino el grupo de notificacion entonces asigno el id del grupo de notificacion
            //para enviarselo al wearable
            posNotificationId=SharedData.GROUP_ID_NOTIFICATION
        }
        //envio la posicion de la notificacion al wearable
        RepositoryDispatcherWearable.sendDataToWearable(context, SharedData.PATH_VIEWED_NOTIFICATION,posNotificationId)
    }
}