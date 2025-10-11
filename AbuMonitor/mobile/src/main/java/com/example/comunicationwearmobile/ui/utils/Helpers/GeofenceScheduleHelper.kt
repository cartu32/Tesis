package com.example.comunicationwearmobile.ui.utils.Helpers

import android.content.Context
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.data.model.EntityScheduledAssistance
import com.example.comunicationwearmobile.ui.model.dto.DataAreaGeofAux
import com.example.comunicationwearmobile.ui.model.pojo.AreaGeofenceWithAppointment
import com.example.comunicationwearmobile.ui.model.repository.RepositoryGeofActivate
import com.example.comunicationwearmobile.ui.model.repository.RepositoryScheduleAssistance
import com.example.comunicationwearmobile.ui.utils.Tools
import kotlinx.coroutines.CoroutineScope

class GeofenceScheduleHelper(mContext:Context, scope: CoroutineScope?) {

    private val context=mContext.applicationContext
    private var repositoryScheduleAssistance:RepositoryScheduleAssistance?=null
    private var repositoryGeofActivate: RepositoryGeofActivate?=null
    init {
        repositoryScheduleAssistance=
            scope?.let {
                RepositoryScheduleAssistance.getInstance(context)
            }

        repositoryGeofActivate= RepositoryGeofActivate()
    }

    suspend fun checkAssistanceScheduled() {
        val smsHelper=SmsHelper()
        val dateToday = Tools.getDateTodayInMillis()
        val listAppointWithoutAssisntace = repositoryScheduleAssistance
            ?.getAppointmentThatDidntAssistenceToday(dateToday)

        if (!listAppointWithoutAssisntace.isNullOrEmpty()) {
            //se genera un resumen de las citas a la que no asistio la persona en el dia de la fecha
            val msg=generateMessageInTable(listAppointWithoutAssisntace)
            Log.d(Definition.TAG_DEBUG, msg)

            //Envio SMS notificando el problema
            smsHelper.sendSMSPlainText(context,msg)
        }else{
            Log.d(Definition.TAG_DEBUG, "No hay citas sin asistencia para hoy")
        }
    }

    private fun generateMessageInTable(listAppointWithoutAssisntace: List<EntityScheduledAssistance>): String {

        val message = buildString {
            appendLine("Citas sin asistencia para hoy:")
            appendLine("")
            appendLine("-------------------------")
            appendLine("Descripción         Hora")
            appendLine("-------------------------")
            for (appointment in listAppointWithoutAssisntace) {
                val time = Tools.getMillisToHourMinutes(appointment.date_hour_appointment)
                val desc = appointment.description.padEnd(20) // ajustá este valor según el largo máximo esperado
                appendLine("$desc $time")
            }
        }
        return message


    }

    suspend fun activateAndDesactivateGeofenceScheduled(){
        desactivateGeofencePreviousScheduled()
        activateGeofenceNextScheduled()
    }


    suspend private fun activateGeofenceNextScheduled() {
       /* var listAreasInsideDateInterval:List<AreaGeofenceWithAppointment>?=null

        //activo todas las geofences programadas dentro del intervalo de la alarma
        listAreasTomorrow=repositoryScheduleAssistance?.getAreasForTomorrow()

        Log.d(Definition.TAG_DEBUG,"listAreasTomorrow: $listAreasTomorrow")
*/
    }


    private fun desactivateGeofencePreviousScheduled() {
        TODO("Not yet implemented")
    }
}