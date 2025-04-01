package com.example.comunicationwearmobile.ui.utils

import android.location.Location
import android.os.StrictMode
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.google.android.gms.maps.model.LatLng
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


object Tools {

    fun isPointInsideCircle(point: LatLng, center: LatLng?, radiusInMeters: Double?): Boolean {
        if (center == null || radiusInMeters == null) {
            Log.e(Definition.TAG_DEBUG,"Error center o readiusInmeter son null")
            return false // Si no hay centro o radio, no puede estar dentro.
        }

        val distance = FloatArray(1)
        Location.distanceBetween(
            point.latitude, point.longitude,
            center.latitude, center.longitude,
            distance
        )

        return distance[0] <= radiusInMeters
    }

    fun desactiveStrictMode(){
        //Deshabilita StrictMode temporalmente para evitar warnings
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .permitDiskReads()  // Evita los StrictModeDiskReadViolation
                .permitDiskWrites() // Evita los StrictModeDiskWriteViolation
                .build()
        )

    }


    fun getDate(dateTime: LocalDate):String{
        val formatter=DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val formatterDate=dateTime.format(formatter)

        return formatterDate
    }

    fun getHour(dateTime: LocalTime):String{
        val formatter= DateTimeFormatter.ofPattern("HH:mm")
        val formatterDate=dateTime.format(formatter)

        return formatterDate
    }

}