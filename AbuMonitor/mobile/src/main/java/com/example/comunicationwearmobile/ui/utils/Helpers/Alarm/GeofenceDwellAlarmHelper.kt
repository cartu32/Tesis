package com.example.comunicationwearmobile.ui.utils.Helpers.Alarm

import android.content.Context
import android.location.Location
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDebugLogger
import com.example.comunicationwearmobile.ui.model.repository.RepositoryLocation
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.GeofenceEventProcessorHelper
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_DWELL

object GeofenceDwellAlarmHelper {

    suspend fun executeActionsOfAlarmDwell(applicationContext: Context, areaId:Long) {
        try {

            // 1) Obtener área desde DB
            val repoAreas = RepositoryAreaDB.getInstance(applicationContext)
            val areaJoin = repoAreas.getJoinAreaGeofence(areaId) ?: run {
                RepositoryDebugLogger.log(applicationContext, "DWELL: area inexistente id=$areaId -> descarto")
                return
            }
            val area = areaJoin.areaGeofence

            // 2) Pedir UNA ubicación puntual para revalidar (evita dwell falso)
            val repoLoc = RepositoryLocation.getInstance(applicationContext)
            val loc = repoLoc.getSingleBalancedLocation() ?: run {
                RepositoryDebugLogger.log(applicationContext, "DWELL: sin ubicación puntual -> descarto area=$areaId")
                return
            }

            // 3) Distancia al centro
            val center = Location("dwell_center").apply {
                latitude = area.latitude.toDouble()
                longitude = area.longitude.toDouble()
            }

            val dist = loc.distanceTo(center)
            val radius = area.meters.toFloat()

            // 4) Margen por accuracy (simple pero efectivo)
            val margin = kotlin.math.max(5f, loc.accuracy * 0.5f)

            // Adentro si está suficientemente lejos del borde hacia dentro
            val inside = dist <= (radius - margin)

            if (!inside) {
                RepositoryDebugLogger.log(
                    applicationContext,
                    "DWELL: revalidación FAIL area=$areaId dist=${dist} r=$radius acc=${loc.accuracy} margin=$margin -> NO disparo"
                )
                return
            }

            RepositoryDebugLogger.log(
                applicationContext,
                "DWELL: revalidación OK area=$areaId dist=${dist} r=$radius acc=${loc.accuracy} margin=$margin -> DISPARO"
            )

            GeofenceEventProcessorHelper.handleEvent(
                mutableListOf(areaId),
                GEOFENCE_TRANSITION_DWELL
            )

        } finally {
            Log.d(Definition.TAG_DEBUG, "!!!!Alarma de Dwell Time (procesada)")
        }
    }


}