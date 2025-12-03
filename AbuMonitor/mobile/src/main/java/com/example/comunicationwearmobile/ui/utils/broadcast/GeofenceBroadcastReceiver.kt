package com.example.comunicationwearmobile.ui.utils.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.extra.GeofenceEventParameter
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDebugLogger
import com.example.comunicationwearmobile.ui.utils.services.GeofencesServices
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        RepositoryDebugLogger.log(context,"**********Entro en broadcast receiver de Geofence")
        if (intent.action != Definition.ACTION_GEOFENCE_EVENT_BROADCAST) {
            return
        }

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null) {
            Log.e(TAG, "GeofencingEvent nulo")
            return
        }

        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Error en GeofencingEvent: ${geofencingEvent.errorCode}")
            return
        }

        var triggeringIds = geofencingEvent.triggeringGeofences
            ?.mapNotNull { it.requestId.toLongOrNull() }
            ?: emptyList()

        triggeringIds=triggeringIds.toMutableList()
        if (triggeringIds.isEmpty()) {
            Log.e(TAG, "No se encontraron IDs de geofence en el evento")
            return
        }

        val transition = geofencingEvent.geofenceTransition

        val transitionText = when (transition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "ENTER"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "EXIT"
            Geofence.GEOFENCE_TRANSITION_DWELL -> "DWELL"
            else -> "UNKNOWN"
        }

        // Coordenadas actuales que usa el geofence del sistema
        val loc = geofencingEvent.triggeringLocation
        val locText =
            if (loc != null)
                "lat=${loc.latitude}, lon=${loc.longitude}, acc=${loc.accuracy}"
            else
                "NO_LOCATION"

        RepositoryDebugLogger.log(
            context,
            "EVENTO GEOFENCE: transition=$transitionText, ids=$triggeringIds, loc=($locText)"
        )

        val parameter=GeofenceEventParameter(triggeringIds,transition)

        val serviceIntent = Intent(context, GeofencesServices::class.java).apply {
            action = intent.action
            putExtra(Definition.PARAMETER_SERVICE,parameter )
            intent.extras?.let { putExtras(it) }
        }

        ContextCompat.startForegroundService(context, serviceIntent)

        /*GeofenceEventProcessorHelper.handleEvent(
            triggeringIds,
            transition,
            pendingResult
        )*/
    }

    companion object {
        private const val TAG = "GeofenceReceiver"
    }
}
