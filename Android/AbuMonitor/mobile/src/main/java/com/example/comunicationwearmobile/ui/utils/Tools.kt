package com.example.comunicationwearmobile.ui.utils

import android.location.Location
import com.google.android.gms.maps.model.LatLng

object Tools {

    fun isPointInsideCircle(point: LatLng, center: LatLng?, radiusInMeters: Float): Boolean {
        var distance=FloatArray(1)

        center?.longitude?.let {
            center.latitude.let { it1 ->
                Location.distanceBetween( point.latitude,point.longitude,
                    it1, it, distance)
            }
        };

        if( distance[0] > radiusInMeters  )
            return false
        else
            return true

    }

}