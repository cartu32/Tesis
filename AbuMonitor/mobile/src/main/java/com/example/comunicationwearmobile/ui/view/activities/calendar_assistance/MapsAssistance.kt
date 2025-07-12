package com.example.comunicationwearmobile.ui.view.activities.calendar_assistance

import android.os.Bundle
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.view.activities.common.BaseMapActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng

class MapsAssistance: BaseMapActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)
        mMap?.setOnMapClickListener (this)

    }
    override fun onMapClick(latLng: LatLng) {
        super.onMapClick(latLng)

        drawGeofenceArea(latLng.latitude, latLng.longitude, Definition.GEOFENCE_RADIUS_DEFAULT)
    }
}