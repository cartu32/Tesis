package com.example.comunicationwearmobile.ui.utils

import android.location.Location
import android.os.StrictMode
import android.text.Editable
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale


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

    fun getMillisToDate(millis :Long):String{
        val date = Date(millis)
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateFormatter= format.format(date)

        return dateFormatter
    }


    fun formatHour(millis: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val min = cal.get(Calendar.MINUTE)
        return String.format("%02d:%02d", hour, min)
    }
}