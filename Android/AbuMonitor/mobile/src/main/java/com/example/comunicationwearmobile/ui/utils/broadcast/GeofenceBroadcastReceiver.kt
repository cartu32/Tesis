package com.example.comunicationwearmobile.ui.utils.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.abumonitor.constants.Definition
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
            val notificationHelper = NotificationManagerHelper.getInstance(context)

            if (geofencingEvent != null) {
                if (geofencingEvent.hasError()) {
                    Log.e("GeofenceReceiver", "Error en el Geofencing: ${geofencingEvent.errorCode}")
                    return
                }
            }

            if (geofencingEvent != null) {
                val transition = geofencingEvent.geofenceTransition
                val msg=createMsg(transition)

                when (transition) {
                    Geofence.GEOFENCE_TRANSITION_ENTER -> {
                        notificationHelper?.showNotificationGeneral(msg)
                        Log.d(Definition.TAG_DEBUG, "Entraste en un geofence")
                    }
                    Geofence.GEOFENCE_TRANSITION_EXIT -> {
                        notificationHelper?.showNotificationGeneral(msg)
                        Log.d(Definition.TAG_DEBUG, "Saliste de un geofence")
                    }
                    Geofence.GEOFENCE_TRANSITION_DWELL -> {
                        Log.d(Definition.TAG_DEBUG, "Estas dentro de un geofence")
                    }
                }
            }
        }
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
            }

            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                msg.typeNotification = SharedData.TypeNotification.Alert
                msg.title="¡Alerta de Geofence!"
                msg.message="Has salido de la zona"
            }
        }
        return msg
    }
}
