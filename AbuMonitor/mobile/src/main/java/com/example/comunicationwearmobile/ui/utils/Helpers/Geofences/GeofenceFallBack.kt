package com.example.comunicationwearmobile.ui.utils.Helpers.Geofences

import android.content.Context
import android.location.Location
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDebugLogger
import com.google.android.gms.location.Geofence

object GeofenceFallBack {

    suspend fun callGeofenceFallBack(context: Context, location: Location) {
        try {
            RepositoryDebugLogger.log(context, "FALLBACK: inicio ejecución")
            Log.d(Definition.TAG_DEBUG, "FALLBACK: inicio ejecución")

            proccessFallBack(context, location)
        }catch (e: Exception) {
            RepositoryDebugLogger.log(context, "FALLBACK: error ${e.message}")
        }
    }

    private suspend fun proccessFallBack(context: Context, location: Location) {
        val repoAreas = RepositoryAreaDB.getInstance(context)
        val activatedAreas=repoAreas.getAllActiveAreasWithEvents()

        if(activatedAreas.isEmpty()) {
            RepositoryDebugLogger.log(context, "FALLBACK: no hay áreas activas")
            return
        }

        val enterIds = mutableListOf<Long>()
        val exitIds  = mutableListOf<Long>()

        for (dataArea in activatedAreas){
            val area=dataArea.entityAreaGeofence

            val areaLocation = Location("fallback_area").apply {
                latitude = area.latitude.toDouble()
                longitude = area.longitude.toDouble()
            }

            val distance = location.distanceTo(areaLocation)
            val currentInside = distance <= area.meters.toFloat()
            val prevState =dataArea.entityAreaRuntimeState.last_inside_state


            when (prevState) {
                null -> {
                    // Primera vez: inicializo estado pero NO disparo eventos
                    RepositoryDebugLogger.log(context, "FALLBACK: inicializo estado para área ${area.id_area} (no genero evento)")
                    Log.d(Definition.TAG_DEBUG, "FALLBACK: inicializo estado para área ${area.id_area} (no genero evento)")
                }
                false -> {
                    // Antes estaba FUERA
                    if (currentInside) {
                        enterIds.add(area.id_area)
                    }
                }
                true -> {
                    // Antes estaba DENTRO
                    if (!currentInside) {
                        exitIds.add(area.id_area)
                    }
                }
            }

            dataArea.entityAreaRuntimeState.last_inside_state=currentInside
            // Actualizo estado para la próxima ejecución
            if(repoAreas.updateAreaState(dataArea.entityAreaRuntimeState)==0) {
                Log.e(Definition.TAG_DEBUG, "FALLBACK: error al actualizar estado para área ${area.id_area}")
                RepositoryDebugLogger.log(context, "FALLBACK: error al actualizar estado para área ${area.id_area}")
                return
            }

            // 5) Disparo eventos ENTER/EXIT según corresponda
            if (enterIds.isNotEmpty()) {
                RepositoryDebugLogger.log(context, "FALLBACK: disparo ENTER para ids=$enterIds")
                Log.d(Definition.TAG_DEBUG, "FALLBACK: disparo ENTER para ids=$enterIds")

                GeofenceEventProcessorHelper.handleEvent(triggeringIds = enterIds, transition = Geofence.GEOFENCE_TRANSITION_ENTER)
            }

            if (exitIds.isNotEmpty()) {
                RepositoryDebugLogger.log(context, "FALLBACK: disparo EXIT para ids=$exitIds")
                Log.d(Definition.TAG_DEBUG, "FALLBACK: disparo EXIT para ids=$exitIds")

                GeofenceEventProcessorHelper.handleEvent(triggeringIds = exitIds, transition = Geofence.GEOFENCE_TRANSITION_EXIT)
            }

        }

    }

}