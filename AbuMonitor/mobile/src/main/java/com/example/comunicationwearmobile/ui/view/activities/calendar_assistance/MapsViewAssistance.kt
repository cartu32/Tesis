package com.example.comunicationwearmobile.ui.view.activities.calendar_assistance

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.view.activities.common.BaseMapActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng


class MapsViewAssistance : BaseMapActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }



    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)

        val latitude = intent.getStringExtra("latitude")?.toDouble()
        val longitude = intent.getStringExtra("longitude")?.toDouble()
        val meters = intent.getIntExtra("meters", 0).toDouble()

        if (latitude != null && longitude != null) {
            drawAreaGeofHelper?.drawGeofenceArea(
                latitude,
                longitude,
                meters,
                Definition.TYPE_AREA_COLOR_ASSISTANCE
            )

            val latLng = LatLng(latitude, longitude)
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, Definition.ZOOM_MAP))
        } else {
            Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        drawAreaGeofHelper?.freeResources()
        drawAreaGeofHelper=null

        Log.d(Definition.TAG_DEBUG, "Ondestroy MapsViewAssistance")
    }
}

