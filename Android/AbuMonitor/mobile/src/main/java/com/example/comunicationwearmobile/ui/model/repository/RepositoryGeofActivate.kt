package com.example.comunicationwearmobile.ui.model.repository

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.comunicationwearmobile.ui.utils.broadcast.GeofenceBroadcastReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class RepositoryGeofActivate() {

    @SuppressLint("MissingPermission")
    suspend fun activateGeofence(context: Context, areaGeof: EntityAreaGeofence): Boolean =
        suspendCancellableCoroutine { continuation ->


            val geofencingClient = LocationServices.getGeofencingClient(context)

            val geofence = Geofence.Builder()
                .setRequestId(areaGeof.id_area.toString())
                .setCircularRegion(areaGeof.latitude.toDouble(),areaGeof.longitude.toDouble(), areaGeof.meters.toFloat())
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)  // Transiciones
                .build()

            val geofencingRequest = GeofencingRequest.Builder()
                //.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            val pendingIntent = getGeofencePendingIntent(context)

            //como esta es una acción asincriconica,y esta funcion es llamada dentro
            //de una corutina, se suspende la ejecucion de la funcion(en realidad la corutina)
            // cuando se ejecuta la ejecucion de geofenclient addgeofence, hasta que nosotros
            // ejecutemos resume
            //De esta forma la funcion puede retornar una valor a la funcion llamadora
            //de forma asincronica
            geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener {
                    Log.d(Definition.TAG_DEBUG, "Geofence agregado: $geofencingRequest.")
                    //reactivo la corutina suspendida
                    continuation.resume(true)

                }
                .addOnFailureListener {
                    Log.e(Definition.TAG_DEBUG, "Error al agregar geofence: ${it.message}")
                    //reactivo la corutina suspendida
                    continuation.resume(false)
                }

    }

    private fun getGeofencePendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            action = "com.example.app.ACTION_GEOFENCE_EVENT"
        }

        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    fun desactivateGeofence(context: Context,idArea:String) {
        val geofencingClient = LocationServices.getGeofencingClient(context)

        // Eliminar el geofence usando su ID
        val geofenceRequestIds = listOf(idArea)  // El ID del geofence que quieres eliminar

        geofencingClient.removeGeofences(geofenceRequestIds)
            .addOnSuccessListener {
                // El Geofence ha sido eliminado correctamente
                Log.d("Geofencing", "Geofence removed successfully")
            }
            .addOnFailureListener { exception ->
                // Error al eliminar el geofence
                Log.e("Geofencing", "Failed to remove geofence: ${exception.localizedMessage}")
            }
    }


}