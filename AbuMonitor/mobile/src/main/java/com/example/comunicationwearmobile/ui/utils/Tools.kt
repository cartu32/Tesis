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
/**
 * Utilidades generales para manejo de fechas, horas y validaciones temporales,
 * además de helpers para Android y operaciones con datos parcelables.
 *
 */
object Tools {

    // ==========================================================
    //  FORMATEADORES Y HELPERS PRIVADOS
    // ==========================================================

    /** Formato estándar de fecha: "dd/MM/yyyy". */
    private val DF_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    /** Formato estándar de hora en 24 horas: "HH:mm". */
    private val DF_HOUR = DateTimeFormatter.ofPattern("HH:mm")

    /**
     * Aplica un formato determinado a un valor temporal en milisegundos.
     *
     * @param millis valor temporal en milisegundos desde epoch.
     * @param pattern patrón de formato, por ejemplo `"dd/MM/yyyy"`.
     * @return una cadena formateada según el patrón indicado.
     */
    private fun formatMillis(millis: Long, pattern: String): String =
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))

    /**
     * Obtiene el instante correspondiente al inicio del día (00:00:00.000)
     * para un tiempo dado.
     *
     * @param millis instante de referencia en milisegundos.
     * @return el mismo día pero a las 00:00:00.000.
     */
     fun getDayStart(millis: Long): Long = Calendar.getInstance().run {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        timeInMillis
    }



    // ==========================================================
    //  ANDROID / UI HELPERS
    // ==========================================================

    /**
     * Convierte una cadena en un objeto [Editable].
     *
     * @param text texto que se desea convertir.
     * @return una instancia de [Editable] con el texto recibido.
     */
    fun toEditable(text: String): Editable =
        Editable.Factory.getInstance().newEditable(text)

    /**
     * Desactiva temporalmente el [StrictMode] para permitir
     * operaciones de lectura/escritura en disco sin generar advertencias.
     */
    fun desactiveStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .permitDiskReads()
                .permitDiskWrites()
                .build()
        )
    }


    // ==========================================================
    //  FORMATEO Y CONVERSIÓN DE FECHAS / HORAS
    // ==========================================================

    /**
     * Convierte los minutos a milisegundos.
     *
     * @param parcialMillis cantidad de minutos a convertir
     * @return el tiempo convertido en milisegundos.
     */

    fun convertMinutesToMillis(time: Long): Long {
        return time * 60 * 1000
    }

    /**
     * Convierte un [LocalDate] a formato `"dd/MM/yyyy"`.
     *
     * @param dateTime objeto de tipo [LocalDate].
     * @return cadena con la fecha formateada.
     */
    fun getDate(dateTime: LocalDate): String = dateTime.format(DF_DATE)

    /**
     * Convierte un [LocalTime] a formato `"HH:mm"`.
     *
     * @param dateTime objeto de tipo [LocalTime].
     * @return cadena con la hora formateada.
     */
    fun getHour(dateTime: LocalTime): String = dateTime.format(DF_HOUR)

    /**
     * Convierte un valor en milisegundos a una cadena `"dd/MM/yyyy"`.
     *
     * @param millis tiempo en milisegundos desde epoch.
     * @return cadena con la fecha formateada.
     */
    fun getMillisToDate(millis: Long): String = formatMillis(millis, "dd/MM/yyyy")

    /**
     * Convierte un valor en milisegundos a una cadena `"HH:mm"`.
     *
     * @param millis tiempo en milisegundos desde epoch.
     * @return cadena con la hora formateada.
     */
    fun getMillisToHourMinutes(millis: Long): String = formatMillis(millis, "HH:mm")

    /**
     * Convierte un desplazamiento dentro del día (en milisegundos) a una pareja hora:minuto.
     *
     * @param parcialMillis milisegundos desde las 00:00 (0 a 86.399.999).
     * @return un [Pair] con la hora y los minutos correspondientes.
     */
    fun getHourMinOfParcial(parcialMillis: Long): Pair<Int, Int> {
        val totalMinutes = (parcialMillis / (60 * 1000)).toInt()
        val hour = totalMinutes / 60
        val min = totalMinutes % 60
        return hour to min
    }


    // ==========================================================
    //  EXTRACCIÓN / DESCOMPOSICIÓN DE FECHAS
    // ==========================================================

    /**
     * Extrae sólo la parte de “día” de una fecha (hora truncada a 00:00).
     *
     * @param fullMillis instante completo en milisegundos.
     * @return el mismo día a las 00:00 en milisegundos.
     */
    fun extractDayOfDateInMillis(fullMillis: Long): Long = getDayStart(fullMillis)

    /**
     * Obtiene sólo la parte horaria (offset desde la medianoche).
     *
     * @param fullMillis instante completo en milisegundos.
     * @return cantidad de milisegundos transcurridos desde las 00:00 de ese día.
     */
    fun extractHourOfDateInMillis(fullMillis: Long): Long =
        fullMillis - getDayStart(fullMillis)

    /**
     * Calcula el inicio del día siguiente a partir de un instante.
     *
     * @param funMillis instante base en milisegundos.
     * @return instante del día siguiente a las 00:00.
     */
    fun getStartNextDay(funMillis: Long): Long =
        getDayStart(funMillis) + 24 * 60 * 60 * 1000L


    // ==========================================================
    //  CONSTRUCCIÓN DE INSTANTES
    // ==========================================================

    /**
     * Obtiene el instante correspondiente al inicio del día actual (00:00).
     *
     * @return tiempo en milisegundos del inicio del día actual.
     */
    fun getDateTodayInMillis(): Long = getDayStart(System.currentTimeMillis())

    /**
     * Construye un instante para hoy a la hora y minuto especificados.
     * Si la hora ya pasó hoy, devuelve el mismo horario del día siguiente.
     *
     * @param hour hora deseada (0–23).
     * @param minute minuto deseado (0–59).
     * @return instante en milisegundos representando esa hora.
     */
    fun getHourInMillis(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= now.timeInMillis) cal.add(Calendar.DAY_OF_YEAR, 1)
        return cal.timeInMillis
    }

    /**
     * Convierte una hora y minuto a milisegundos desde las 00:00.
     *
     * @param hour hora (0–23).
     * @param minute minuto (0–59).
     * @return milisegundos desde las 00:00 de un día ficticio.
     */
    fun getTimeInMillis(hour: Int, minute: Int): Long =
        hour * 60 * 60 * 1000L + minute * 60 * 1000L


    // ==========================================================
    //  VALIDACIONES Y COMPARACIONES TEMPORALES
    // ==========================================================

    /**
     * Determina si una hora actual está fuera de un rango dado (formato "HH:mm").
     * Soporta rangos que cruzan la medianoche.
     *
     * @param currentTimeStr hora actual, formato "HH:mm".
     * @param startTimeStr hora de inicio del rango.
     * @param endTimeStr hora de fin del rango.
     * @return `true` si la hora actual está fuera del rango; `false` si está dentro.
     */
    fun isOutsideTimeRange(currentTimeStr: String, startTimeStr: String, endTimeStr: String): Boolean {
        val current = LocalTime.parse(currentTimeStr, DF_HOUR)
        val start = LocalTime.parse(startTimeStr, DF_HOUR)
        val end = LocalTime.parse(endTimeStr, DF_HOUR)
        return if (start <= end) current < start || current > end
        else current < start && current > end
    }

    /**
     * Verifica si una fecha (en milisegundos) es hoy o futura.
     *
     * @param timestamp instante en milisegundos desde epoch.
     * @return `true` si pertenece a hoy o a una fecha posterior.
     */
    fun isGreaterThanToday(timestamp: Long): Boolean {
        val inputDate = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        val today = LocalDate.now()
        return !inputDate.isBefore(today)
    }

    /**
     * Verifica si una fecha corresponde al día actual.
     *
     * @param dateMillis instante en milisegundos.
     * @return `true` si la fecha coincide con el día actual.
     */
    fun isToday(dateMillis: Long): Boolean {
        val fmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return fmt.format(Date()) == fmt.format(Date(dateMillis))
    }

    /**
     * Determina si un instante futuro está al menos [bufferMillis] ms por delante del actual.
     *
     * @param givenTimeMillis instante objetivo en milisegundos.
     * @param bufferMillis margen mínimo permitido (por defecto 60.000 ms = 1 minuto).
     * @return `true` si [givenTimeMillis] ocurre después del margen.
     */
    fun isTimeGreaterThanCurrentTime(givenTimeMillis: Long, bufferMillis: Long = 60_000L): Boolean {
        val now = System.currentTimeMillis()
        return givenTimeMillis >= now + bufferMillis
    }

    /**
     * Comprueba si la hora actual está dentro de una ventana válida de llegada a una cita.
     *
     * @param timeAppointment hora de la cita en milisegundos.
     * @param earlyMinutes minutos permitidos de antelación (por defecto 30).
     * @return `true` si la persona llegó dentro del margen válido.
     */
    fun isTimeEnterAssistanceCorrect(timeAppointment: Long, earlyMinutes: Int = 30): Boolean {
        val earlyWindow = earlyMinutes * 60 * 1000L
        val timeAppointmentEarly = timeAppointment - earlyWindow
        return System.currentTimeMillis() >= timeAppointmentEarly
    }

    /**
     * Verifica si una cita (fecha+hora) es hoy y aún no pasó, o si pertenece a una fecha futura.
     *
     * @param dateHourAppoint instante de la cita en milisegundos.
     * @return `true` si la cita aún no ocurrió; `false` si ya pasó.
     */
    fun isTimeAndDateGreaterThanCurrentDate(dateHourAppoint: Long): Boolean {
        return if (isToday(dateHourAppoint)) {
            val ok = isTimeGreaterThanCurrentTime(dateHourAppoint)
            Log.d(Definition.TAG_DEBUG, if (ok) "es hoy y la hora está bien" else "es hoy y la hora está mal")
            ok
        } else {
            Log.d(Definition.TAG_DEBUG, "es un día mayor a hoy")
            true
        }
    }


    // ==========================================================
    //  INTENTS / PARCELABLES
    // ==========================================================

    /**
     * Extrae un objeto [DataAreaGeofAux] de un [Bundle] enviado por un Intent.
     * Compatible con Android 13 (Tiramisu) y versiones anteriores.
     *
     * @param data bundle recibido en la Activity/Service.
     * @return objeto [DataAreaGeofAux] si está presente; `null` si no existe.
     */
    fun extractDataNewAreaOfIntent(data: Bundle): DataAreaGeofAux? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            data.getParcelable(Definition.INTENT_DATA_NEW_AREA_GEOF, DataAreaGeofAux::class.java)
        } else {
            @Suppress("DEPRECATION")
            data.getParcelable(Definition.INTENT_DATA_NEW_AREA_GEOF)
        }
}
