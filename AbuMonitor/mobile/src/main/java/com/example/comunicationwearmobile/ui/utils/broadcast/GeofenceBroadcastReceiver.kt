package com.example.comunicationwearmobile.ui.utils.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.utils.workers.GeofenceWorker
import com.google.android.gms.location.GeofencingEvent

// Reemplazo de la corutina por Worker en el BroadcastReceiver
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext
        Log.d(Definition.TAG_DEBUG,"onReceive GeofenceBroadcastReceiver")
        if (intent.action == Definition.ACTION_GEOFENCE_EVENT_BROADCAST) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            if (geofencingEvent == null || geofencingEvent.hasError()) {
                Log.e(Definition.TAG_DEBUG, "Error en el Geofencing: ${geofencingEvent?.errorCode}")
                return
            }

            // Serializamos el evento (guardamos los IDs y el tipo de transición)
            val triggeringIds = geofencingEvent.triggeringGeofences?.map { it.requestId }?.toTypedArray()
            val transition = geofencingEvent.geofenceTransition

            val inputData = workDataOf(
                "triggering_ids" to triggeringIds,
                "transition" to transition
            )

            val workRequest = OneTimeWorkRequestBuilder<GeofenceWorker>()
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(appContext).enqueueUniqueWork(
                "trabajo_geofence",
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            )
        }
    }
}
