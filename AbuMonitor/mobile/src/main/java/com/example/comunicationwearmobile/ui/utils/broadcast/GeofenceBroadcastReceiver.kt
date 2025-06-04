package com.example.comunicationwearmobile.ui.utils.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDispatcherWearable
import com.example.comunicationwearmobile.ui.utils.Mannager.NotificationManagerHelper
import com.example.comunicationwearmobile.ui.utils.Tools
import com.example.shared_library.SharedData
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import java.time.LocalDate
import java.time.LocalTime

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.example.app.ACTION_GEOFENCE_EVENT") {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            if (geofencingEvent != null) {
                if (geofencingEvent.hasError()) {
                    Log.e(Definition.TAG_DEBUG, "Error en el Geofencing: ${geofencingEvent.errorCode}")
                    return
                 }
            }

            if (geofencingEvent != null) {
                val transition = geofencingEvent.geofenceTransition
                val msg=createMsg(transition)

                //envia la notificaciones al usuario
                notifyUser(context.applicationContext,msg)

            }
        }
    }

    private  fun notifyUser( context: Context, msg: SharedData.MsgNotification) {
        val notificationHelper = NotificationManagerHelper.getInstance(context)

        //muestro la notificacion al usuario en la bandeja de notificacion del telefono
        //y obtengo su id para poder enviarselo al samrtwatch
        val idMsgMobile=notificationHelper?.showNotificationGeneral(msg)

        //el id de la notificacion se la agrego al mesnaje que lo envio al smartwatch
        if (idMsgMobile != null) {
            msg.idMsgMobile=idMsgMobile
        }

        RepositoryDispatcherWearable.sendDataToWearable(context,SharedData.PATH_ADD_NOTIFICATION_GENERAL,msg)
    }

    private fun createMsg(transition: Int): SharedData.MsgNotification {
        val msg=SharedData.MsgNotification()

        msg.hour = Tools.getHour(LocalTime.now())
        msg.date = Tools.getDate(LocalDate.now())

        when (transition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                msg.typeNotification = SharedData.TypeNotification.Alert
                msg.title="¡Alerta de Geofence!"
                msg.message="Has entrado en la zona"

                Log.d(Definition.TAG_DEBUG, "Entraste en un geofence")

            }

            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                msg.typeNotification = SharedData.TypeNotification.Alert
                msg.title="¡Alerta de Geofence!"
                msg.message="Has salido de la zona"

                Log.d(Definition.TAG_DEBUG, "Saliste de un geofence")

            }
        }
        return msg
    }
}
