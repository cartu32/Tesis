package com.example.comunicationwearmobile.presenter.maps

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.util.Log
import com.example.comunicationwearmobile.presenter.maps.MapsActivityPresenter.Companion.MIN_DISTANCE_CHANGE_FOR_UPDATES
import com.example.comunicationwearmobile.presenter.maps.MapsActivityPresenter.Companion.MIN_TIME_BW_UPDATES
import com.example.comunicationwearmobile.ui.Activities.MapsActivity

/**
 * Esta clase se encargará de todo lo relacionado con la obtención de la ubicación (GPS y red),
 *  así como la gestión de permisos relacionados con la ubicación..
 *
 * @property activity nombre de la activity.
 */
class LocationManagerHelper constructor(private var activity: MapsActivity?) {


    /*Se declara una variable de tipo Location que accederá a la última posición conocida proporcionada por el proveedor.*/
    private var location: Location? = null

    /*Se declara una variable de tipo LocationManager encargada de proporcionar acceso al servicio de localización del sistema.*/
    private var locationManager: LocationManager? = null


    private var isGPSEnabled = false
    private var isNetworkEnabled: Boolean? = false

    fun checkconnection(): Boolean {
        activity!!.applicationContext
        locationManager = activity!!.applicationContext
            .getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationManager?.let{manager->
            // getting GPS status
            isGPSEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            // getting network status
            isNetworkEnabled = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
        return (!isGPSEnabled &&  !isNetworkEnabled!!)
    }


    fun getLocation(): Location? {
        try {
            if (checkconnection()) {
                // Si no hay proveedor habilitado
                //solicito que active el gps
                activity!!.alertNoGps()
            }

            // if GPS Enabled get lat/long using GPS Services
            if (isGPSEnabled) {
                setPositionGPS()
            } else if (isNetworkEnabled!!) {
                setPositionNetwork()
            }
        } catch (e: Exception) {
            Log.e("getLocation" , e.message.toString())
        }
        return location
    }

    @SuppressLint("MissingPermission")
    fun setPositionGPS() {
        if (location == null) {
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER ,
                MIN_TIME_BW_UPDATES ,
                MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat() , activity!!
            )

            // Si location es mutable globalmente, asignamos el valor de locationManager a location de manera segura
            locationManager?.let { manager ->
                location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                location?.let { currentLocation ->
                    activity?.positionUpdate(currentLocation)
                }
            } ?: run {
                println("locationManager es nulo")
            }

        }
    }

    @SuppressLint("MissingPermission")
    private fun setPositionNetwork() {
        locationManager?.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER ,
            MIN_TIME_BW_UPDATES ,
            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat() , activity!!
        )
        locationManager?.let { manager->
            location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            location?.let { currentLocation ->
                activity!!.positionUpdate(currentLocation)
            }
        }

    }


}