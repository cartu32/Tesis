package com.example.comunicationwearmobile.ui.utils.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.example.app.ACTION_GEOFENCE_EVENT") {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            if (geofencingEvent != null) {
                if (geofencingEvent.hasError()) {
                    Log.e("GeofenceReceiver", "Error en el Geofencing: ${geofencingEvent.errorCode}")
                    return
                }
            }

            if (geofencingEvent != null) {
                when (geofencingEvent.geofenceTransition) {
                    Geofence.GEOFENCE_TRANSITION_ENTER -> {
                        Log.d("GeofenceReceiver", "Entraste en un geofence")
                        // Aquí puedes iniciar un servicio, una notificación o guardar un estado en la base de datos
                    }

                    Geofence.GEOFENCE_TRANSITION_EXIT -> {
                        Log.d("GeofenceReceiver", "Saliste de un geofence")
                    }
                }
            }
        }
    }
}
