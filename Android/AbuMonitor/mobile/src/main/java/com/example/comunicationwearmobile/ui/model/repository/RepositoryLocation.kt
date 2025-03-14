package com.example.comunicationwearmobile.ui.model.repository


import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.abumonitor.constants.Definition
import com.google.android.gms.location.*

class RepositoryLocation(context: Context) {

    private val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationLiveData = MutableLiveData<Location>()

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        Definition.INTERVAL_MILLIS_ACTUALIZATION_POS_GPS)
        .setMinUpdateIntervalMillis(Definition.SETUP_UPDATE_INTERVAL_MILLIS)
        .build()

    private var locationCallback: LocationCallback? = null //  Convertimos en nullable

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (locationCallback == null) {  // Evitar múltiples instancias
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.locations.lastOrNull()?.let {
                        locationLiveData.postValue(it)
                    }
                }
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback!!, null)
        }
    }

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationProviderClient.removeLocationUpdates(it)
        }
        locationCallback = null //Liberamos la referencia para evitar memory leaks
    }

    fun getLocationLiveData(): LiveData<Location> = locationLiveData
}
