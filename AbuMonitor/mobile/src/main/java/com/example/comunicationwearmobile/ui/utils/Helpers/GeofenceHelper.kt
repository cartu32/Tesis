package com.example.comunicationwearmobile.ui.utils.Helpers

import android.content.Context
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.comunicationwearmobile.ui.model.dto.DataAreaGeofAux
import com.example.comunicationwearmobile.ui.model.pojo.AreaGeofenceWithAppointment
import com.example.comunicationwearmobile.ui.model.repository.RepositoryGeofActivate
import com.example.comunicationwearmobile.ui.model.repository.RepositoryScheduleAssistance
import kotlinx.coroutines.CoroutineScope

class GeofenceHelper(mContext:Context, scope: CoroutineScope?) {

    private val context=mContext.applicationContext
    private var repositoryScheduleAssistance:RepositoryScheduleAssistance?=null
    private var repositoryGeofActivate: RepositoryGeofActivate?=null
    init {
        repositoryScheduleAssistance=
            scope?.let {
                RepositoryScheduleAssistance.getInstance(context, it)
            }

        repositoryGeofActivate= RepositoryGeofActivate()
    }

    suspend fun activateGeofenceScheduled() {
        var listAreasTomorrow:List<AreaGeofenceWithAppointment>?=null

        listAreasTomorrow=repositoryScheduleAssistance?.getAreasForTomorrow()

        Log.d(Definition.TAG_DEBUG,"listAreasTomorrow: $listAreasTomorrow")

        if (listAreasTomorrow != null) {
            activateAllGeofenceForTomorrow(listAreasTomorrow)
        }

    }
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

        checkActivationAllGeofences(context,allGeofencesActivated)
    }

    private fun checkActivationAllGeofences(context:Context,allGeofencesActivated: Boolean) {
        var notificationHelper:NotificationHelper?=null

        notificationHelper=NotificationHelper.getInstance(context)

        if(allGeofencesActivated){
            notificationHelper?.showNotificationIndependent(context,"Abumonitor","Se activaron las areas programadas para mañana")
        }else{
            notificationHelper?.showNotificationIndependent(context,"Abumonitor","No se pudo activar alguna area de geofence para mañana")
        }
    }

    fun desactivateAllGeofenceToToday(){

    }
}