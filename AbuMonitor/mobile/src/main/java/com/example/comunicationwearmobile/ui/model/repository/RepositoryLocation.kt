package com.example.comunicationwearmobile.ui.model.repository

import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.view.activities.utils.EnableGpsDialog

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*    // LocationServices, LocationRequest, etc.
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class RepositoryLocation private constructor(appContext: Context) {

    private val appContext = appContext.applicationContext  // Evitamos fugas de memoria

    private val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(this.appContext)
    private val settingsClient = LocationServices.getSettingsClient(this.appContext)

    private var locationCallback: LocationCallback? = null // Para evitar múltiples instancias

    private val _locationLiveData = MutableLiveData<Location>()
    val locationLiveData: LiveData<Location> get() = _locationLiveData

    private val locationRequest: LocationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        Definition.INTERVAL_MILLIS_ACTUALIZATION_POS_GPS
    ).setMinUpdateIntervalMillis(Definition.SETUP_UPDATE_INTERVAL_MILLIS)
        .build()

    private val locationSettingsRequest: LocationSettingsRequest =
        LocationSettingsRequest.Builder()
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
                        _locationLiveData.postValue(location)
                    }
                }
            }

            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback!!, Looper.getMainLooper()
            )
        } else {
            Log.d(Definition.TAG_DEBUG, "Localizacion callback ya inicializada")
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

    // ----------------------------------------------------------------------
    //  🔹 Helper 1: ubicación puntual BALANCED (puede devolver cache)
    // ----------------------------------------------------------------------
    @SuppressLint("MissingPermission")
    suspend fun getSingleBalancedLocation(): Location? =
        suspendCancellableCoroutine { continuation ->
            fusedLocationProviderClient
                .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { location ->
                    continuation.resume(location)
                }
                .addOnFailureListener { e ->
                    Log.e(Definition.TAG_DEBUG, "getSingleBalancedLocation error: ${e.message}")
                    continuation.resume(null)
                }
        }

    // ----------------------------------------------------------------------
    //  🔹 Helper 2: ubicación puntual HIGH_ACCURACY (despierta sensores)
    //      - Pide updates HIGH_ACCURACY
    //      - Toma el primer fix
    //      - Cancela las updates
    //      - Tiene timeout de seguridad
    // ----------------------------------------------------------------------
    @SuppressLint("MissingPermission")
    suspend fun getSingleHighAccuracyLocation(
        timeoutMillis: Long = 15_000L
    ): Location? = suspendCancellableCoroutine { cont ->
        val req = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            0L
        ).setMinUpdateIntervalMillis(0L)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation
                if (loc != null && !cont.isCompleted) {
                    cont.resume(loc)
                    fusedLocationProviderClient.removeLocationUpdates(this)
                }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            req,
            callback,
            Looper.getMainLooper()
        )

        // Timeout de seguridad
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            if (!cont.isCompleted) {
                fusedLocationProviderClient.removeLocationUpdates(callback)
                cont.resume(null)
            }
        }, timeoutMillis)

        cont.invokeOnCancellation {
            fusedLocationProviderClient.removeLocationUpdates(callback)
        }
    }
}
