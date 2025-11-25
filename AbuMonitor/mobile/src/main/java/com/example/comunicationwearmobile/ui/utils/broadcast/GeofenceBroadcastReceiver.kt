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
import com.example.comunicationwearmobile.ui.utils.Helpers.GeofenceEventPreocessorHelper
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
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

        val triggeringIds = geofencingEvent.triggeringGeofences
            ?.mapNotNull { it.requestId.toLongOrNull() }
            ?: emptyList()

        if (triggeringIds.isEmpty()) {
            Log.e(TAG, "No se encontraron IDs de geofence en el evento")
            return
        }

        val transition = geofencingEvent.geofenceTransition

        val pendingResult = goAsync()
        val appContext = context.applicationContext

        GeofenceEventPreocessorHelper.handleEvent(
            triggeringIds,
            transition,
            pendingResult
        )
    }

    companion object {
        private const val TAG = "GeofenceReceiver"
    }
}