package com.example.comunicationwearmobile.ui.utils.Mannager

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.utils.interfaces.LocationCallback

/**
 * Esta clase se encargará de todo lo relacionado con la obtención de la ubicación (GPS y red),
 *  así como la gestión de permisos relacionados con la ubicación..
 *
 * @property activity nombre de la activity.
 */class LocationManagerHelper constructor(private var callback: LocationCallback?) {

    private var location: Location? = null
    private var locationManager: LocationManager? = null
    private var isGPSEnabled = false
    private var isNetworkEnabled: Boolean = false

    fun checkConnectionSignalLocation(): Boolean {
        val context = callback?.getApplicationContext()
        locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationManager?.let { manager ->
            isGPSEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            isNetworkEnabled = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
        return (isGPSEnabled && isNetworkEnabled)
    }

}
