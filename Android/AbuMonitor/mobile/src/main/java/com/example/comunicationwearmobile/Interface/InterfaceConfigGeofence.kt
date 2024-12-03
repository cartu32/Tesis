package com.example.comunicationwearmobile.Interface

import com.google.android.gms.maps.model.LatLng

interface InterfaceConfigGeofence {
    fun clearMaps()
    fun updateCircleRadiusGraphic(geofenceRadius: Float)
    fun updateCircleColorGraphic(id: String?)
    fun addMarkerGeofence(latLng: LatLng? , radius: Float)
    fun showMessage(message: String?)
    fun graphRoute(latLngList: List<LatLng?>? , idArea: String?)
}
