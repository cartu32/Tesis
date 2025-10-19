package com.example.comunicationwearmobile.ui.utils.Helpers

import android.content.Context
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.data.model.EntityScheduledAssistance
import com.example.comunicationwearmobile.ui.model.dto.DataAreaGeofAux
import com.example.comunicationwearmobile.ui.model.pojo.AreaGeofenceWithAppointment
import com.example.comunicationwearmobile.ui.model.repository.RepositoryGeofActivate
import com.example.comunicationwearmobile.ui.model.repository.RepositoryScheduleAlarmSPref
import com.example.comunicationwearmobile.ui.model.repository.RepositoryScheduleAssistance
import com.example.comunicationwearmobile.ui.utils.Tools

class GeofenceScheduleHelper(mContext:Context) {

    private val context=mContext.applicationContext
    private var repositoryScheduleAssistance:RepositoryScheduleAssistance
    private var repositoryGeofActivate: RepositoryGeofActivate
    private var repositoryScheduleAlarmSPref: RepositoryScheduleAlarmSPref

    init {
        repositoryScheduleAssistance=RepositoryScheduleAssistance.getInstance(context)
        repositoryGeofActivate= RepositoryGeofActivate()
        repositoryScheduleAlarmSPref= RepositoryScheduleAlarmSPref.getInstance(context)
    }

    suspend fun activateAndDesactivateGeofenceScheduled(){
        var timeBetweenAlarm:Long=0
        var dateTimeAlarmInitial:Long=0
        var dateTimeAlarmNext:Long=0
        var resultActivate=false

        //obtengo cual es el intervalo de tiempo en que se va a ejecutar la alarma
        timeBetweenAlarm=repositoryScheduleAlarmSPref.getTimeBetweenChecks()
        Log.d(Definition.TAG_DEBUG,"timeBetweenAlarm: $timeBetweenAlarm")

        //se toma como tiempo inicial el momento en que ocurre la alarma de checkeos
        dateTimeAlarmInitial= System.currentTimeMillis()
        //calculo cual es la proxima hora en que se va a ejecutar la alaram
        dateTimeAlarmNext=dateTimeAlarmInitial+timeBetweenAlarm

        //primero desactivo las geofences que fueron programadas por la alarma anterior
        if(desactivateGeofencePreviousAlarm(dateTimeAlarmInitial,dateTimeAlarmNext)) {
            //luego activo las geofences que dentro del interrvalo horario correspondiente
            // a la alarma actual
            if(activateGeofenceOfCurrentAlarm(dateTimeAlarmInitial, dateTimeAlarmNext)){
                resultActivate=true
            }
        }

        checkActivationAllGeofences(resultActivate)
    }


    private suspend  fun activateGeofenceOfCurrentAlarm(dateTimeAlarmInitial: Long, dateTimeAlarmNext: Long): Boolean {
        var listAreasInsideDateInterval:List<AreaGeofenceWithAppointment>?=null
        var result=false

        //busco todas las geofences programadas dentro del intervalo de la alarma
        listAreasInsideDateInterval=repositoryScheduleAssistance.getAreasInsideDateInterval(
            dateTimeAlarmInitial,
            dateTimeAlarmNext
        )

        Log.d(Definition.TAG_DEBUG,"listAreaInsideDateInterval: $listAreasInsideDateInterval")

        //si hay areas dentro del intervalo de la alarma las activo
        if(listAreasInsideDateInterval.isNotEmpty()){
            result=activateGeofencesForThisAlarm(listAreasInsideDateInterval)
        }

        return result
    }

    private suspend fun activateGeofencesForThisAlarm(listAreasInsideDateInterval: List<AreaGeofenceWithAppointment>): Boolean {

        var allGeofencesActivated = true

        //activo cada area de geofence dentro del intervalo de la alarma
        for (areaScheduled in listAreasInsideDateInterval) {

            //pregunto si la geofecne se activo correctamente
            if(activateOneGeofences(areaScheduled)){
                //pregunto si no se pude actualizar el registro de la geofence en la base de datos
                //como que el area ya esta activada
                if(repositoryScheduleAssistance.updateIsActivatedGeofence(areaScheduled.id_area,true)==0){
                    //si no se pudo registro como que ya activada, entonces la desactivo
                    //ya que hubo un error
                    repositoryGeofActivate.desactivateGeofence(context,areaScheduled.id_area.toString())
                    allGeofencesActivated=false

                    Log.d(Definition.TAG_DEBUG,"No se pudo activar la geofence id: ${areaScheduled.id_area}")

                }else{
                    Log.d(Definition.TAG_DEBUG,"Se activo correctamente la geofence id: ${areaScheduled.id_area}")
                }
            }
        }
        return allGeofencesActivated

    }

    //metodo que activa todas las areas de geofence programada dentro del intervalo de la alarma
    suspend fun activateOneGeofences(areaScheduled: AreaGeofenceWithAppointment): Boolean {
        var result=false

        //armo la entidad area de geofence
        val area = EntityAreaGeofence(
            id_area = areaScheduled.id_area,
            latitude = areaScheduled.latitude,
            longitude = areaScheduled.longitude,
            meters = areaScheduled.meters,
            id_priority = areaScheduled.id_priority,
            id_type_area = areaScheduled.id_type_area
        )

        //como list_id_event me devuelve los id_event del area agrupados por comas ("1,2")
        //entonces lo que hago es separalos por comas y los convierto a enteros agregandolos en un listado.
        //lo hice de esta manera para optimizar memoeria cuando hago la consulta de sql
        val events = areaScheduled.list_id_event.split(",").map {it.toInt() }.toMutableList()

        //completo la esstrutura dataclass para activar el area de geofence
        val areaForActivate= DataAreaGeofAux(
            entityAreaGeofence = area,
            listIdEventSelected = events,
            secZoneTimeRange = null // o como lo necesites
        )
        //activo el area de geofence y si hay algun error lo indico en allGeofencesActivated
        result= repositoryGeofActivate.activateGeofence(context,areaForActivate)

        return result
    }


    private suspend fun checkActivationAllGeofences(allGeofencesActivated: Boolean) {
        var notificationHelper:NotificationHelper?=null

        notificationHelper=NotificationHelper.getInstance(context)

        if(allGeofencesActivated){
            notificationHelper?.showNotificationIndependent(context,"Abumonitor","Se activaron las areas programadas para este horario")
        }else{
            Log.d(Definition.TAG_DEBUG,"No se activo ninguna area de greofence de asistencia programada")
        }
    }

    private suspend fun desactivateGeofencePreviousAlarm(dateTimeAlarmInitial: Long, dateTimeAlarmNext: Long): Boolean {
        var listAreasActivated:List<EntityScheduledAssistance>?= null
        var listInassitenceAppoinment= mutableListOf<EntityScheduledAssistance>()
        var resultDesactivate:Boolean=true

        listAreasActivated=repositoryScheduleAssistance.getAreasWithAppointmentActivated(dateTimeAlarmInitial,dateTimeAlarmNext)

        if (listAreasActivated.isEmpty()) {
            Log.d(Definition.TAG_DEBUG, "No hay areas de geofence programadas para la alarma actual")

            return resultDesactivate
        }

        for (area in listAreasActivated) {

            with(area) {
                //desactivo primero el area de geofence de la cita
                repositoryGeofActivate.desactivateGeofence(context, id_area.toString())

                //compruebo si la cita fue o no asistida
                if(!went_appointment) {
                    //si no aistió lo agrego a un listado
                    Log.d(Definition.TAG_DEBUG, "Cita sin asistencia id: $id_area")
                    listInassitenceAppoinment.add(area)
                }

                //al final indico en la base de datos que la geofence no esta activa
                if (repositoryScheduleAssistance.updateIsActivatedGeofence(id_area.toLong(), false) != 0) {
                    Log.d(Definition.TAG_DEBUG, "Se desactivo correctamente la geofence id: $id_area")
                } else {
                    resultDesactivate = false
                    Log.d(Definition.TAG_DEBUG, "No se pudo desactivar la geofence id: $id_area")
                }

            }
        }
        //despues de desactivar las area,
        // otifico a los familiares todas las citas a las que no asistio
        if (listInassitenceAppoinment.isNotEmpty())
            reportInassistanceScheduled(listInassitenceAppoinment)

        return resultDesactivate
    }


    suspend fun reportInassistanceScheduled(listAppointWithoutAssisntace:List<EntityScheduledAssistance>) {
        val smsHelper=SmsHelper()

        //se genera un resumen de las citas a la que no asistio la persona en el dia de la fecha
        val msg=generateMessageInTable(listAppointWithoutAssisntace)
        Log.d(Definition.TAG_DEBUG, msg)

        //Envio SMS notificando el problema
        smsHelper.sendSMSPlainText(context,msg)
    }

    private fun generateMessageInTable(listAppointWithoutAssisntace: List<EntityScheduledAssistance>): String {

        val message = buildString {
            appendLine("Citas sin asistencia:")
            appendLine("")
            appendLine("--------------------------------")
            appendLine("Descripción         Hora de cita")
            appendLine("--------------------------------")
            for (appointment in listAppointWithoutAssisntace) {
                val time = Tools.getMillisToHourMinutes(appointment.date_hour_appointment)
                val desc = appointment.description.padEnd(23) // ajustá este valor según el largo máximo esperado
                appendLine("$desc $time")
            }
        }
        return message


    }

}