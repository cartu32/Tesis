package com.example.comunicationwearmobile.ui.utils

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object Tools {

    fun isPointInsideCircle(point: LatLng , center: LatLng , radiusInMeters: Float): Boolean {
        var distance=FloatArray(1)

        Location.distanceBetween( point.latitude,point.longitude,
            center.latitude, center.longitude, distance);

        if( distance[0] > radiusInMeters  )
            return false
        else
            return true

    }

}