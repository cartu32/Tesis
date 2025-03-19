package com.example.comunicationwearmobile.ui.model.repository

/**
 * Esta clase se encargará de todo lo relacionado con la obtención de la ubicación (GPS),
 *
 * @property activity nombre de la activity.
 */

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.utils.Mannager.NotificationManagerHelper
import com.example.comunicationwearmobile.ui.utils.Mannager.NotificationManagerHelper.Companion
import com.example.comunicationwearmobile.ui.view.activities.EnableGpsDialog
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

class RepositoryLocation private constructor(appContext: Context) {

    private val appContext = appContext.applicationContext  // Evitamos fugas de memoria

    private val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.appContext)
    private val settingsClient = LocationServices.getSettingsClient(this.appContext)

    private var locationCallback: LocationCallback? = null // Para evitar múltiples instancias

    private val _locationLiveData = MutableLiveData<Location>()
    val locationLiveData: LiveData<Location> get() = _locationLiveData

    private val locationRequest: LocationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        Definition.INTERVAL_MILLIS_ACTUALIZATION_POS_GPS
    ).setMinUpdateIntervalMillis(Definition.SETUP_UPDATE_INTERVAL_MILLIS)
        .build()

    private val locationSettingsRequest: LocationSettingsRequest = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)
        .build()

    fun checkStatusGPS() {
        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                Log.d(Definition.TAG_DEBUG, "GPS está activado")
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    val intent = Intent(appContext, EnableGpsDialog::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra(Definition.RESOLVABLE_API_EXCEPTION, exception.resolution)
                    }
                    startActivity(appContext, intent, null)
                } else {
                    Log.e(Definition.TAG_DEBUG, "No se puede resolver: ${exception.message}")
                }
            }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (locationCallback == null) {
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    for (location in locationResult.locations) {
                        Log.d("LocationService", "Nueva ubicación: ${location.latitude}, ${location.longitude}")
                        _locationLiveData.postValue(location)
                    }
                }
            }

            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback!!, Looper.getMainLooper()
            )
        }
    }

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationProviderClient.removeLocationUpdates(it)
        }
        locationCallback = null
    }

    companion object {
        @Volatile
        private var INSTANCE: RepositoryLocation? = null

        fun getInstance(context: Context): RepositoryLocation {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RepositoryLocation(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
