package com.example.comunicationwearmobile.ui.utils.Helpers.Geofences

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.dto.DataAreaGeofAux
import com.example.comunicationwearmobile.ui.utils.broadcast.GeofenceBroadcastReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object GeofenceActivatorHelper {

    private var geofencePendingIntent: PendingIntent? = null


    @SuppressLint("MissingPermission")
    suspend fun activateGeofence(context: Context, dataAreaGeofAux: DataAreaGeofAux): Boolean =
        suspendCancellableCoroutine { continuation ->

        val geofencingClient = LocationServices.getGeofencingClient(context)
        val area = dataAreaGeofAux.entityAreaGeofence

        var transitionTypes = 0

        for (event in dataAreaGeofAux.listIdEventSelected) {
            val type = determineTransitionTypes(event)
            transitionTypes = transitionTypes or type
        }

        if (transitionTypes == 0) {
            Log.e(Definition.TAG_DEBUG, "Error:No se seleccionó ningún tipo de transición")
            continuation.resume(false)
            return@suspendCancellableCoroutine
        }

        val builder = Geofence.Builder()
        .setRequestId(area.id_area.toString())
        .setCircularRegion(
            area.latitude.toDouble(),
            area.longitude.toDouble(),
            area.meters.toFloat()
        )
        .setExpirationDuration(Geofence.NEVER_EXPIRE)
        .setTransitionTypes(transitionTypes)
        .setNotificationResponsiveness(Definition.NOTIFICATION_MAX_RESPONSIVENESS_GEOFENCE)


        if (transitionTypes and Geofence.GEOFENCE_TRANSITION_DWELL != 0) {
            val dwellTime=(dataAreaGeofAux.entityAreaGeofence.dwell_time)*60000
            builder.setLoiteringDelay(dwellTime)
            Log.d(Definition.TAG_DEBUG,"se definio el tiempo de espera: $dwellTime")
        }

        val geofence=builder.build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val pendingIntent = getGeofencePendingIntent(context)

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
            .addOnSuccessListener {
                Log.d(Definition.TAG_DEBUG, "Agregada geocerca ${area.id_area}")
                continuation.resume(true)
            }
            .addOnFailureListener {
                Log.e(Definition.TAG_DEBUG, "Error al agregar geocerca: ${it.message}")
                continuation.resume(false)
            }
    }

    suspend fun clearAllGeofence(context: Context):Boolean=
        suspendCancellableCoroutine { continuation ->
        val geofencingClient = LocationServices.getGeofencingClient(context)

        val geofencePendingIntent=getGeofencePendingIntent(context)

        geofencingClient.removeGeofences(geofencePendingIntent)
            .addOnSuccessListener {
                Log.d(Definition.TAG_DEBUG, "Se eliminaron TODAS las geofences registradas")
                continuation.resume(true)
            }
            .addOnFailureListener{
                Log.e(Definition.TAG_DEBUG, "Error al eliminar todas las geofences: ${it.message}")
                continuation.resume(false)
            }

    }

    fun desactivateGeofence(context: Context,idArea:String) {
        val geofencingClient = LocationServices.getGeofencingClient(context)

        // Eliminar el geofence usando su ID
        val geofenceRequestIds = listOf(idArea)  // El ID del geofence que quieres eliminar

        geofencingClient.removeGeofences(geofenceRequestIds)
            .addOnSuccessListener {
                // El Geofence ha sido eliminado correctamente
                Log.d(Definition.TAG_DEBUG, "Geofence removed successfully")
            }
            .addOnFailureListener { exception ->
                // Error al eliminar el geofence
                Log.e(Definition.TAG_DEBUG, "Failed to remove geofence: ${exception.localizedMessage}")
            }
    }


    @Synchronized
    private fun getGeofencePendingIntent(context: Context): PendingIntent {
        if (geofencePendingIntent != null) return geofencePendingIntent!!

        val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            action = Definition.ACTION_GEOFENCE_EVENT_BROADCAST
        }

        geofencePendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        return geofencePendingIntent!!
    }

    private fun determineTransitionTypes(event: Int): Int {
        when (event) {
            Definition.GEOFENCE_EVENT_ID_ENTER -> return Geofence.GEOFENCE_TRANSITION_ENTER
            Definition.GEOFENCE_EVENT_ID_EXIT -> return Geofence.GEOFENCE_TRANSITION_EXIT
            Definition.GEOFENCE_EVENT_ID_DWELL -> return Geofence.GEOFENCE_TRANSITION_DWELL
        }
        return 0
    }

}