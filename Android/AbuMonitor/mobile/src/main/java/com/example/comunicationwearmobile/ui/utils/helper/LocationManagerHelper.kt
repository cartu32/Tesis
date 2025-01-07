package com.example.comunicationwearmobile.ui.utils.helper

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
    private var isNetworkEnabled: Boolean? = false

    fun checkconnection(): Boolean {
        val context = callback?.getApplicationContext()
        locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationManager?.let { manager ->
            isGPSEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            isNetworkEnabled = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
        return (!isGPSEnabled && !isNetworkEnabled!!)
    }

    fun getLocation(): Location? {
        try {
            if (checkconnection()) {
                callback?.alertNoGps()
            }

            if (isGPSEnabled) {
                setPositionGPS()
            } else if (isNetworkEnabled == true) {
                setPositionNetwork()
            }
        } catch (e: Exception) {
            Log.e("getLocation", e.message.toString())
        }
        return location
    }

    @SuppressLint("MissingPermission")
    private fun setPositionGPS() {
        if (location == null) {
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                Definition.MIN_TIME_BW_UPDATES,
                Definition.MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(),
                callback as? LocationListener ?: return
            )

            locationManager?.let { manager ->
                location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                location?.let { currentLocation ->
                    callback?.positionUpdate(currentLocation)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setPositionNetwork() {
        locationManager?.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            Definition.MIN_TIME_BW_UPDATES,
            Definition.MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(),
            callback as? LocationListener ?: return
        )
        locationManager?.let { manager ->
            location = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            location?.let { currentLocation ->
                callback?.positionUpdate(currentLocation)
            }
        }
    }
}
