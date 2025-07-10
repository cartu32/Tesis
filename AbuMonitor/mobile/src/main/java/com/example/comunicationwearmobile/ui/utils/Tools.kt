package com.example.comunicationwearmobile.ui.utils

import android.location.Location
import android.os.StrictMode
import android.text.Editable
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.google.android.gms.maps.model.LatLng
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


object Tools {
    fun toEditable(text: String): Editable = Editable.Factory.getInstance().newEditable(text)

    fun isOutsideTimeRange(currentTimeStr: String, startTimeStr: String, endTimeStr: String): Boolean {
        val formatter = DateTimeFormatter.ofPattern("HH:mm") // formato de hora 24hs

        val currentTime = LocalTime.parse(currentTimeStr, formatter)
        val startTime = LocalTime.parse(startTimeStr, formatter)
        val endTime = LocalTime.parse(endTimeStr, formatter)

        return if (startTime <= endTime) {
            // Rango normal, por ejemplo de 08:00 a 20:00
            currentTime < startTime || currentTime > endTime
        } else {
            // Rango que pasa por medianoche, por ejemplo de 20:00 a 06:00
            currentTime < startTime && currentTime > endTime
        }
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