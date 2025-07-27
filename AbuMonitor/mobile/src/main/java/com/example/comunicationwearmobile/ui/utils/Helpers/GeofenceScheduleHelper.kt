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

    //metodo que me desactiva las geofences que se activaron para hoy de forma programada
    //y me activa todas las areas de geofences que fueron programadas para mañana
    suspend fun activateGeofenceScheduled() {
        var listAreasTomorrow:List<AreaGeofenceWithAppointment>?=null

        //desactivo todas las geofences programadas para hoy
        desactivateAllGeofenceToToday()

        //activo todas las geofences programadas para mañana
        listAreasTomorrow=repositoryScheduleAssistance?.getAreasForTomorrow()

        Log.d(Definition.TAG_DEBUG,"listAreasTomorrow: $listAreasTomorrow")

        if (listAreasTomorrow != null) {
            activateAllGeofenceForTomorrow(listAreasTomorrow)
        }

    }

    //metodo que activa todas las areas de geofence programadas para mañana
    suspend fun activateAllGeofenceForTomorrow(listAreasTomorrow: List<AreaGeofenceWithAppointment>) {
        var allGeofencesActivated = true

        //activo cada area de geofence de mañana
        for(areaTomorrow in listAreasTomorrow){

            //armo la entidad area de geofence
            val area = EntityAreaGeofence(
                id_area = areaTomorrow.id_area,
                latitude = areaTomorrow.latitude,
                longitude = areaTomorrow.longitude,
                meters = areaTomorrow.meters,
                id_priority = areaTomorrow.id_priority,
                id_type_area = areaTomorrow.id_type_area
            )

            //como list_id_event me devuelve los id_event del area agrupados por comas ("1,2")
            //entonces lo que hago es separalos por comas y los convierto a enteros agregandolos en un listado.
            //lo hice de esta manera para optimizar memoeria cuando hago la consulta de sql
            val events = areaTomorrow.list_id_event.split(",").map {it.toInt() }.toMutableList()

            //completo la esstrutura dataclass para activar el area de geofence
            val areaForActivate=DataAreaGeofAux(
                                                    entityAreaGeofence = area,
                                                    listIdEventSelected = events,
                                                    secZoneTimeRange = null // o como lo necesites
                                                )
            //activo el area de geofence y si hay algun error lo indico en allGeofencesActivated
            if(repositoryGeofActivate?.activateGeofence(context,areaForActivate)==false){
                allGeofencesActivated=false
            }
        }

        checkActivationAllGeofences(allGeofencesActivated)
    }

    private fun checkActivationAllGeofences(allGeofencesActivated: Boolean) {
        var notificationHelper:NotificationHelper?=null

        notificationHelper=NotificationHelper.getInstance(context)

        if(allGeofencesActivated){
            notificationHelper?.showNotificationIndependent(context,"Abumonitor","Se activaron las areas programadas para mañana")
        }else{
            notificationHelper?.showNotificationIndependent(context,"Abumonitor","No se pudo activar alguna area de geofence para mañana")
        }
    }

    //este metodo me desactiva todas las areas de geofence que fueron programadas
    //para que esten actibvdas en el dia de hoy
    suspend fun desactivateAllGeofenceToToday(){
        var listAreasToday:List<Long>?=null

        listAreasToday=repositoryScheduleAssistance?.getAreasForToday()

        Log.d(Definition.TAG_DEBUG,"listAreasToday: $listAreasToday")

        if (listAreasToday != null) {
            desactivateAllGeofenceForToday(listAreasToday)
        }

    }

    private fun desactivateAllGeofenceForToday(listAreasToday: List<Long>) {
        for(idArea in listAreasToday){
            repositoryGeofActivate?.desactivateGeofence(context,idArea.toString())
        }
    }

    suspend fun checkAssistanceScheduled() {
        val smsHelper=SmsHelper()
        val dateToday = Tools.getDateTodayInMillis()
        val listAppointWithoutAssisntace = repositoryScheduleAssistance
            ?.getAppointmentThatDidntAssistenceToday(dateToday)

        if (listAppointWithoutAssisntace != null && listAppointWithoutAssisntace.isNotEmpty()) {
            //se genera un resumen de las citas a la que no asistio la persona en el dia de la fecha
            val msg=generateMessageInTable(listAppointWithoutAssisntace)
            Log.d(Definition.TAG_DEBUG, msg)

            //Envio SMS notificando el problema
            smsHelper.sendSMSPlainText(context,msg)
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
                val time = Tools.getMillisToHourMinutes(appointment.hour_appointment)
                val desc = appointment.description.padEnd(20) // ajustá este valor según el largo máximo esperado
                appendLine("$desc $time")
            }
        }
        return message


    }


}