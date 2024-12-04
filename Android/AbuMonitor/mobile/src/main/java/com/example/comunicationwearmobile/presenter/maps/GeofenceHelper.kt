package com.example.comunicationwearmobile.presenter.maps

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.util.Log
import com.example.comunicationwearmobile.models.maps.iGeofence
import com.example.comunicationwearmobile.presenter.maps.MapsActivityPresenter.Companion.TAG
import com.example.comunicationwearmobile.utils.maps.Tools
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices


class GeofenceHelper(base: Context?) : ContextWrapper(base) {
    private var geofencingClient: GeofencingClient?= null
    private val geofenceList = ArrayList<Geofence>()
    private val hashMapId = HashMap<String , Int>()


    private var pendingIntent: PendingIntent? = null
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


    val geofencingRequest: GeofencingRequest
        get() = GeofencingRequest.Builder()
                .addGeofences(geofenceList)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build()


    init {
        geofencingClient = base?.let { LocationServices.getGeofencingClient(it) }
    }


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


    fun generateGeofences(listLastGeofence: java.util.ArrayList<iGeofence>): Boolean {
        //Creo el identificador de cada zona de goefoence que empiece por una letra identifcadora.


        var idAux: Int

        if (listLastGeofence.isEmpty()) return false


        Log.d("Alerta" , "Entrando en For")
        for (i in listLastGeofence.indices) {
            //cantGeofences++;
            idAux = createHashMapId(listLastGeofence[i].idArea.toString())
            addGeofenceList(
                    listLastGeofence[i].idArea + idAux ,
                    listLastGeofence[i].latitud!! ,
                    listLastGeofence[i].longitud!! ,
                    listLastGeofence[i].radius ,
                    Geofence.GEOFENCE_TRANSITION_ENTER
            )
            Log.d("Alerta" , "Ejecutando For")
        }

        return activateGefenceRequest()
    }



    @SuppressLint("MissingPermission")
    private fun activateGefenceRequest(): Boolean {
        val geofencingRequest = geofencingRequest

        geofencingClient!!.addGeofences(geofencingRequest , pendingIntent!!)
            .addOnSuccessListener {
                Log.d(TAG , "onSuccess: Geofence Added...")
            }
            .addOnFailureListener { e ->
                val errorMessage = getErrorString(e)
                Log.d(TAG , "onFailure: $errorMessage")
            }
        return true
    }
    private fun createHashMapId(idArea: String): Int {
        // Usamos el operador getOrDefault para obtener el valor de la clave o un valor por defecto si es null
        var cantId = hashMapId.getOrDefault(idArea, 0)

        // Si no es nulo, incrementamos el valor
        cantId++

        // Actualizamos el valor en el mapa
        hashMapId[idArea] = cantId

        return cantId
    }


    public fun getHashMapId(idArea:String): Int {
        val id = hashMapId.getOrDefault(idArea, -1)
        return id
    }

    fun clearGeofencesIntent() {
        geofencingClient!!.removeGeofences(pendingIntent!!)
            .addOnSuccessListener() { // Geofences removed
                // ...
                Log.d(TAG , "se borraron todos los geofences")
            }
            .addOnFailureListener() { // Failed to remove geofences
                Log.d(TAG , "Error al borrar todos los geofences")
            }

        hashMapId.clear()
        clearGeofenceList()
    }
}
