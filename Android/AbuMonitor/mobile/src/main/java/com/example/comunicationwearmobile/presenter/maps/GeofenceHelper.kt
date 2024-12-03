package com.example.comunicationwearmobile.presenter.maps

import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import com.example.comunicationwearmobile.utils.maps.Tools
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingRequest


class GeofenceHelper(base: Context?) : ContextWrapper(base) {
    var pendingIntent: PendingIntent? = null
        get() {
            if (field != null) {
                return field
            }
            val intent = Intent(this , GeofenceTransitionService::class.java)
            intent.putExtra("Operation" , Tools.GEOFENCE_TRANSITION)
            field = PendingIntent.getService(
                this ,
                0 ,
                intent ,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )


            return field
        }
        private set
    private val geofenceList = ArrayList<Geofence>()

    val geofencingRequest: GeofencingRequest
        get() = GeofencingRequest.Builder()
            .addGeofences(geofenceList)
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .build()

    fun addGeofenceList(
        ID: String? ,
        latitude: Double ,
        longitude: Double ,
        radius: Float ,
        transitionTypes: Int
    ) {
        geofenceList.add(
            Geofence.Builder()
                .setCircularRegion(latitude , longitude , radius)
                .setRequestId(ID!!)
                .setTransitionTypes(transitionTypes)
                .setLoiteringDelay(5000)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build()
        )
    }

    fun getGeofenceList(): ArrayList<*> {
        return this.geofenceList
    }

    fun clearGeofenceList() {
        geofenceList.clear()
    }

    fun getErrorString(e: Exception): String? {
        if (e is ApiException) {
            when (e.statusCode) {
                GeofenceStatusCodes
                    .GEOFENCE_NOT_AVAILABLE -> return "GEOFENCE_NOT_AVAILABLE"

                GeofenceStatusCodes
                    .GEOFENCE_TOO_MANY_GEOFENCES -> return "GEOFENCE_TOO_MANY_GEOFENCES"

                GeofenceStatusCodes
                    .GEOFENCE_TOO_MANY_PENDING_INTENTS -> return "GEOFENCE_TOO_MANY_PENDING_INTENTS"
            }
        }
        return e.localizedMessage
    }


}
