package com.example.comunicationwearmobile.ui.utils.Mannager

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.utils.services.GeofencesServices
import com.example.comunicationwearmobile.ui.view.activities.EnableGpsDialog
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient

/**
 * Esta clase se encargará de todo lo relacionado con la obtención de la ubicación (GPS y red),
 *  así como la gestión de permisos relacionados con la ubicación..
 *
 * @property activity nombre de la activity.
 */class LocationManagerHelper constructor(private var context: Context) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var settingsClient: SettingsClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationSettingsRequest: LocationSettingsRequest

    fun configCheckStatusGps() {
        // Inicializar clientes
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        settingsClient = LocationServices.getSettingsClient(context)

        // Crear una solicitud de ubicación
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).build()

        // Crear configuración de ajustes
        locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

    }

    fun checkLocationSettings(context: Context) {
        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                // El GPS está activado
                Log.d(Definition.TAG_DEBUG, "GPS está activado")
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    // El GPS no está activado, pedir al usuario que lo active
                    val intent = Intent(context, EnableGpsDialog::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK )
                    intent.putExtra(Definition.RESOLVABLE_API_EXCEPTION, exception.resolution);

                    startActivity(context,intent,null)
                } else {
                    Log.e(Definition.TAG_DEBUG, "No se puede resolver: ${exception.message}")
                }
            }
    }

}
