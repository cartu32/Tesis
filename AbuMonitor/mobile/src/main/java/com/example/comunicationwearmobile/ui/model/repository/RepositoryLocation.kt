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
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class RepositoryLocation private constructor(appContext: Context) {

    private val appContext = appContext.applicationContext  // Evitamos fugas de memoria

    private val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(this.appContext)
    private val settingsClient = LocationServices.getSettingsClient(this.appContext)

    private var locationCallback: LocationCallback? = null // Para evitar múltiples instancias

    //se usa para enviar las ubicaciones al viewmodel
    private val _locationLiveData = MutableLiveData<Location>()
    val locationLiveData: LiveData<Location> get() = _locationLiveData

    //se usa para enviar de forma manual las ubicaciones al foregroundservice
    private val _locationFlow = MutableSharedFlow<Location>(
        replay = 1,
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val locationFlow: SharedFlow<Location> = _locationFlow

/*    val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, // GPS fuerte
        10_000L                          // cada 10s
    )
        .setMinUpdateIntervalMillis(5_000L)     // hasta cada 5s si puede
        .setMaxUpdateDelayMillis(30_000L)       // agrupa hasta 30s
        .setMinUpdateDistanceMeters(5f)         // si se movió 5m, mandá
        .build()
*/
    val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, // GPS fuerte
        15_000L                          // cada 15s
    )
        .setMinUpdateIntervalMillis(10_000L)     // hasta cada 5s si puede
        .setMaxUpdateDelayMillis(0L)       // agrupa hasta 30s
        .setMinUpdateDistanceMeters(8f)         // si se movió 5m, mandá
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
                        //se envia la ubicacion al viewmodel para mostrala por pantalla
                        _locationLiveData.postValue(location)
                        //se envia la ubicacion al foregroundservice para detectar de forma manual si la persona
                        //esta dentro o fuera de un area de geofence
                        _locationFlow.tryEmit(location)
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
    //   Helper 1: ubicación puntual BALANCED (puede devolver cache)
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
    //   Helper 2: ubicación puntual HIGH_ACCURACY (despierta sensores)
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
