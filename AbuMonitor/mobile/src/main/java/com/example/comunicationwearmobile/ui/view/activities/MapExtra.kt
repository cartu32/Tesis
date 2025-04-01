package com.example.comunicationwearmobile.ui.view.activities

import android.os.Bundle
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng

class MapExtra:BaseMapActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)
        mMap?.setOnMapClickListener (this)

    }
    override fun onMapClick(latLng: LatLng) {
        super.onMapClick(latLng)

        drawGeofenceArea(latLng.latitude,latLng.longitude)
    }
}