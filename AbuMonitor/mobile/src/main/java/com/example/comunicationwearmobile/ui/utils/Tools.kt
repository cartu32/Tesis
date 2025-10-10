package com.example.comunicationwearmobile.ui.utils

import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.text.Editable
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.dto.DataAreaGeofAux
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
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
    fun getMillisToHourMinutes(millis: Long): String {
        val date = Date(millis)
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return format.format(date)
    }

    fun getFormatHour(millis: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val min = cal.get(Calendar.MINUTE)
        return String.format(Locale.getDefault(), "%02d:%02d", hour, min)
    }


    fun extractDataNewAreaOfIntent(data: Bundle): DataAreaGeofAux? {
        //Recibo los datos desde la activty PropertiesGeofence Activty
        val dataNewAreaGeof = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            data.getParcelable<DataAreaGeofAux>(Definition.INTENT_DATA_NEW_AREA_GEOF,
                DataAreaGeofAux::class.java)
        } else {
            data.getParcelable<DataAreaGeofAux>(Definition.INTENT_DATA_NEW_AREA_GEOF)
        }
        return dataNewAreaGeof
    }

    fun calculateTomorrowMidnight(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        return calendar.timeInMillis
    }

    fun calculateTodayMidnight(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.DAY_OF_YEAR, 0)
        return calendar.timeInMillis
    }

    fun isGreaterThanToday(timestamp: Long): Boolean {
        val inputDate = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val today = LocalDate.now()
        return inputDate.isAfter(today) || inputDate.isEqual(today)
    }

    fun isTimeAndDateGreaterThanCurrentDate(dateHourAppoint: Long): Boolean {
        if(isToday(dateHourAppoint)){
            if(isTimeGreaterThanCurrentTime(dateHourAppoint)){
                Log.d(Definition.TAG_DEBUG,"es hoy y la hora esta bien:")
                return true
            }
            Log.d(Definition.TAG_DEBUG,"es hoy y la hora esta mal:")
            return false
        }
        Log.d(Definition.TAG_DEBUG,"es un dia mayor a hoy")
        return true
    }

    fun isToday(dateMillis: Long): Boolean {
        val formatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

        val today = formatter.format(Date())
        val givenDate = formatter.format(Date(dateMillis))

        return today == givenDate
    }

    fun isTimeGreaterThanCurrentTime(givenTimeMillis: Long): Boolean {
        val currentTimeMillis = System.currentTimeMillis()
        //se compara la hora seleccionada con la hora actual adelantada un minuto
        val oneMinuteLater = currentTimeMillis + 60 * 1000

        return givenTimeMillis >= oneMinuteLater
    }

    fun isTimeEnterAssistanceCorrect(timeAppointment: Long): Boolean {
        //a la hora de la cita le resto 30 minutos por si llega antes de la hora
        val earlyMinutes = 30*60*1000
        val timeAppointmentEarly = timeAppointment - earlyMinutes

        //obtengo la hora actual
        val currentTimeMillis = System.currentTimeMillis()

        //pregunto si la hora a la que llego la persona
        //es mayor o igual a la hora de la cita menos 30 minutos
        return currentTimeMillis >= timeAppointmentEarly
    }

    //funcion que retorna la fecha de hoy en millis pero sin incluir la hora.
    //Por ejemplo retorna: 21-05-2025, pero en milisegundos
    fun getDateTodayInMillis():Long{
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }

    fun getHourInMillis(hour: Int, minute: Int): Long {

        val timeInMillis = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        if (timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }

    fun getTimeInMillis(hour: Int, minute: Int): Long {
        val hourInMs = hour * 60 * 60 * 1000L
        val minutosInMs = minute * 60 * 1000L
        return hourInMs + minutosInMs
    }


    fun extractDayOfDateInMillis(fullMillis: Long):Long{
        val cal = Calendar.getInstance().apply {
            timeInMillis = fullMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val onlyDayMillis = cal.timeInMillis
        return onlyDayMillis
    }

    fun extractHourOfDateInMillis(fullMillis: Long):Long {

        val cal = Calendar.getInstance().apply { timeInMillis = fullMillis }

        val millisHourOfDay =
            (cal.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000L) +
                    (cal.get(Calendar.MINUTE) * 60 * 1000L) +
                    (cal.get(Calendar.SECOND) * 1000L) +
                    cal.get(Calendar.MILLISECOND)

        return millisHourOfDay
    }
}