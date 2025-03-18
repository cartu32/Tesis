package com.example.comunicationwearmobile.ui.utils.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.utils.Mannager.NotificationManagerHelper
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

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
                when (geofencingEvent.geofenceTransition) {
                    Geofence.GEOFENCE_TRANSITION_ENTER -> {
                        notificationHelper?.createAlertNotification("¡Alerta de Geofence!", "Has entrado en la zona.")
                        Log.d(Definition.TAG_DEBUG, "Entraste en un geofence")
                        // Aquí puedes iniciar un servicio, una notificación o guardar un estado en la base de datos
                    }

                    Geofence.GEOFENCE_TRANSITION_EXIT -> {
                        notificationHelper?.createAlertNotification("¡Alerta de Geofence!", "Has salido de la zona.")
                        Log.d(Definition.TAG_DEBUG, "Saliste de un geofence")

                    }
                }
            }
        }
    }
}
