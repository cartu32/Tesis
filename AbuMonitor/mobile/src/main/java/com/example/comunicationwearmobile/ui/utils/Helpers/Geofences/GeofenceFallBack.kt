package com.example.comunicationwearmobile.ui.utils.Helpers.Geofences

import android.content.Context
import android.location.Location
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.common.PermissionsArea
import com.example.comunicationwearmobile.ui.common.ResultAreaGenerateEvent
import com.example.comunicationwearmobile.ui.common.ResultFsm
import com.example.comunicationwearmobile.ui.model.dto.DataAreaGeofAux
import com.example.comunicationwearmobile.ui.model.entities.EntityAreaRuntimeState
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDebugLogger
import com.google.android.gms.location.Geofence

    object GeofenceFallBack {

        suspend fun callGeofenceFallBack(context: Context, location: Location) {
            try {
                RepositoryDebugLogger.log(context, "FALLBACK: inicio ejecución")
                Log.d(Definition.TAG_DEBUG, "FALLBACK: inicio ejecución")

                proccessFallBack(context, location)
            } catch (e: Exception) {
                RepositoryDebugLogger.log(context, "FALLBACK: error ${e.message}")
            }
        }

        private suspend fun proccessFallBack(context: Context, location: Location) {
            val appContext = context.applicationContext

            val activatedAreas = getActiveAreas(appContext)
            if (activatedAreas.isEmpty()) {
                RepositoryDebugLogger.log(appContext, "FALLBACK: no hay áreas activas")
                Log.d(Definition.TAG_DEBUG, "FALLBACK: no hay áreas activas")
                return
            }

            val globalEnterIds = mutableListOf<Long>()
            val globalExitIds  = mutableListOf<Long>()

            val (speed, isFast) = determineSpeedElderly(location, appContext)

            for (dataArea in activatedAreas) {
                val areaResult = processSingleAreaFallback(
                    dataArea = dataArea,
                    appContext = appContext,
                    location = location,
                    isFast = isFast,
                    speed = speed
                )

                if (areaResult.fireEnter) {
                    globalEnterIds.add(areaResult.areaId)
                }
                if (areaResult.fireExit) {
                    globalExitIds.add(areaResult.areaId)
                }
            }

            if (globalEnterIds.isNotEmpty()) {
                triggerActionAreaEntry(globalEnterIds, appContext)
            }

            if (globalExitIds.isNotEmpty()) {
                triggerActionAreaExit(globalExitIds, appContext)
            }
        }

        private suspend fun processSingleAreaFallback(
            dataArea: DataAreaGeofAux,
            appContext: Context,
            location: Location,
            isFast: Boolean,
            speed: Float
        ): ResultAreaGenerateEvent {

            val area = dataArea.entityAreaGeofence

            RepositoryDebugLogger.log(appContext,"Area Id: ${area.id_area} | descripcion: ${area.description}")
            Log.d(Definition.TAG_DEBUG,"Area Id: ${area.id_area} | descripcion: ${area.description}")

            val resultFsm = GeofenceFSM.proccessFSM(
                dataArea = dataArea,
                appContext = appContext,
                area = area,
                location = location,
                isFast = isFast,
                speed = speed
            )

            return ResultAreaGenerateEvent(
                areaId = area.id_area,
                fireEnter = resultFsm.triggerEnter,
                fireExit = resultFsm.triggerExit
            )
        }


        private suspend fun triggerActionAreaExit(
            exitIds: MutableList<Long>,
            appContext: Context,
        ) {
            if (exitIds.isNotEmpty()) {
                RepositoryDebugLogger.log(appContext, "FALLBACK: disparo EXIT para ids=$exitIds")
                Log.d(Definition.TAG_DEBUG, "FALLBACK: disparo EXIT para ids=$exitIds")

                GeofenceEventProcessorHelper.handleEvent(
                    triggeringIds = exitIds,
                    transition = Geofence.GEOFENCE_TRANSITION_EXIT
                )
            }
        }

        private suspend fun triggerActionAreaEntry(
            enterIds: MutableList<Long>,
            appContext: Context,
        ) {
            if (enterIds.isNotEmpty()) {
                RepositoryDebugLogger.log(appContext, "FALLBACK: disparo ENTER para ids=$enterIds")
                Log.d(Definition.TAG_DEBUG, "FALLBACK: disparo ENTER para ids=$enterIds")

                GeofenceEventProcessorHelper.handleEvent(
                    triggeringIds = enterIds,
                    transition = Geofence.GEOFENCE_TRANSITION_ENTER
                )
            }
        }

        private fun determineSpeedElderly(location: Location, context: Context): Pair<Float, Boolean> {
            // Velocidad actual (m/s). Sirve para ajustar el intervalo mínimo.
            val speed = location.speed          // velocidad de la persona
            val isFast =
                speed > Definition.LIMIT_SPEED_WALKING //comparo el limite de la velocidad para determinar si va en auto o caminando

            Log.d(Definition.TAG_DEBUG, "Velocidad limite ${Definition.LIMIT_SPEED_WALKING}")
            if (isFast) {
                RepositoryDebugLogger.log(context, "VELOCIDAD $speed EN AUTO")
                Log.d(Definition.TAG_DEBUG, "VELOCIDAD $speed EN AUTO")
            } else {
                RepositoryDebugLogger.log(context, "VELOCIDAD $speed EN CAMINANDO")
                Log.d(Definition.TAG_DEBUG, "VELOCIDAD $speed EN CAMINANDO")
            }
            return Pair(speed, isFast)
        }

        private suspend fun getActiveAreas(appContext: Context): List<DataAreaGeofAux> {
            val repoAreas = RepositoryAreaDB.getInstance(appContext)
            return repoAreas.getAllActiveAreasWithEvents()
        }


    }
