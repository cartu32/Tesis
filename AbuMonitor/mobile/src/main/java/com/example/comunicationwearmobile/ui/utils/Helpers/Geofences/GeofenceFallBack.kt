package com.example.comunicationwearmobile.ui.utils.Helpers.Geofences

import android.content.Context
import android.location.Location
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDebugLogger
import com.google.android.gms.location.Geofence

object GeofenceFallBack {

    // --- Configuración de histeresis ---
    private const val ENTER_FACTOR = 0.9f   // 90% del radio para considerar "ENTRA"
    private const val EXIT_FACTOR  = 1.1f   // 110% del radio para considerar "SALE"

    suspend fun callGeofenceFallBack(context: Context, location: Location) {
        try {
            RepositoryDebugLogger.log(context, "FALLBACK: inicio ejecución")
            Log.d(Definition.TAG_DEBUG, "FALLBACK: inicio ejecución")

            proccessFallBack(context, location)
        }catch (e: Exception) {
            RepositoryDebugLogger.log(context, "FALLBACK: error ${e.message}")
        }
    }


    private fun isInsideWithHysteresis(
        distance: Float,
        radiusMeters: Float,
        prevState: Boolean?
    ): Boolean {
        val enterRadius = radiusMeters * ENTER_FACTOR
        val exitRadius  = radiusMeters * EXIT_FACTOR

        return when (prevState) {
            null -> {
                // Primera vez: uso el radio "puro"
                distance <= radiusMeters
            }
            false -> {
                // Estaba FUERA → solo pasa a DENTRO si está bien adentro
                distance <= enterRadius
            }
            true -> {
                // Estaba DENTRO → solo pasa a FUERA si se va bien afuera
                distance <= exitRadius   // sigue "inside" mientras no supere exitRadius
            }
        }
    }

    // --- Fallback completo con histeresis ---
    private suspend fun proccessFallBack(context: Context, location: Location) {
        val appContext = context.applicationContext
        val repoAreas = RepositoryAreaDB.getInstance(appContext)

        // 1) Traigo todas las áreas activas
        val activatedAreas = repoAreas.getAllActiveAreasWithEvents()

        if (activatedAreas.isEmpty()) {
            RepositoryDebugLogger.log(appContext, "FALLBACK: no hay áreas activas")
            Log.d(Definition.TAG_DEBUG, "FALLBACK: no hay áreas activas")
            return
        }

        val enterIds = mutableListOf<Long>()
        val exitIds  = mutableListOf<Long>()

        // 2) Recorro todas las áreas y calculo si hubo cambio de estado
        for (dataArea in activatedAreas) {
            val area = dataArea.entityAreaGeofence
            val runtimeState = dataArea.entityAreaRuntimeState

            // Centro del área
            val areaLocation = Location("fallback_area").apply {
                latitude = area.latitude.toDouble()
                longitude = area.longitude.toDouble()
            }

            val distance = location.distanceTo(areaLocation)
            val radiusMeters = area.meters.toFloat()
            val prevState = runtimeState.last_inside_state

            // Aplico histeresis
            val currentInside = isInsideWithHysteresis(
                distance = distance,
                radiusMeters = radiusMeters,
                prevState = prevState
            )

            RepositoryDebugLogger.log(
                appContext,
                "FALLBACK: área=${area.id_area}, dist=${"%.1f".format(distance)}m, " +
                        "radio=$radiusMeters, prev=$prevState, now=$currentInside"
            )

            when (prevState) {
                null -> {
                    // Primera vez: inicializo estado pero NO disparo eventos
                    RepositoryDebugLogger.log(
                        appContext,
                        "FALLBACK: inicializo estado para área ${area.id_area} (no genero evento)"
                    )
                    Log.d(
                        Definition.TAG_DEBUG,
                        "FALLBACK: inicializo estado para área ${area.id_area} (no genero evento)"
                    )
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

            // 3) Actualizo estado para próxima ejecución
            runtimeState.last_inside_state = currentInside

            val rows = repoAreas.updateAreaState(runtimeState)
            if (rows == 0) {
                Log.e(
                    Definition.TAG_DEBUG,
                    "FALLBACK: error al actualizar estado para área ${area.id_area}"
                )
                RepositoryDebugLogger.log(
                    appContext,
                    "FALLBACK: error al actualizar estado para área ${area.id_area}"
                )
                // sigo con las otras áreas, no corto todo
                continue
            }
        }

        // 4) Disparo eventos ENTER para todas las áreas que cambiaron a DENTRO
        if (enterIds.isNotEmpty()) {
            RepositoryDebugLogger.log(appContext, "FALLBACK: disparo ENTER para ids=$enterIds")
            Log.d(Definition.TAG_DEBUG, "FALLBACK: disparo ENTER para ids=$enterIds")

            GeofenceEventProcessorHelper.handleEvent(
                triggeringIds = enterIds,
                transition = Geofence.GEOFENCE_TRANSITION_ENTER
            )
        }

        // 5) Disparo eventos EXIT para todas las áreas que cambiaron a FUERA
        if (exitIds.isNotEmpty()) {
            RepositoryDebugLogger.log(appContext, "FALLBACK: disparo EXIT para ids=$exitIds")
            Log.d(Definition.TAG_DEBUG, "FALLBACK: disparo EXIT para ids=$exitIds")

            GeofenceEventProcessorHelper.handleEvent(
                triggeringIds = exitIds,
                transition = Geofence.GEOFENCE_TRANSITION_EXIT
            )
        }
    }


}