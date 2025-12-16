package com.example.comunicationwearmobile.ui.utils.Helpers.Geofences

import android.content.Context
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.model.dto.DataAreaGeofAux
import com.example.comunicationwearmobile.ui.model.dto.ResultFsm
import com.example.comunicationwearmobile.ui.model.entities.EntityAreaRuntimeState
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDebugLogger
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object GeofenceStatePersistence {

    private val lastStateChangeTime = mutableMapOf<Long, Long>()
    private val stateChangeMutex = Mutex()

    fun getPrevState(dataArea: DataAreaGeofAux): String? {
        val runtimeState = dataArea.entityAreaRuntimeState
        return runtimeState.prev_state_machine
    }

    fun logFsmTransition(context: Context, resultFsm: ResultFsm, currentEventArea: String) {
        RepositoryDebugLogger.log(
            context,
            "FSM: newState=${resultFsm.currentState} | event=$currentEventArea | action=${resultFsm.action}|triggerEnter=${resultFsm.triggerEnter} | triggerExit=${resultFsm.triggerExit}"
        )
        Log.d(
            Definition.TAG_DEBUG,
            "FSM: newState=${resultFsm.currentState} | event=$currentEventArea | action=${resultFsm.action} | triggerEnter=${resultFsm.triggerEnter} | triggerExit=${resultFsm.triggerExit}"
        )
    }

    suspend fun updateInBdCurrentStateArea(
        currentStateArea: String?,
        prevState: String?,
        area: EntityAreaGeofence,
        isFast: Boolean,
        resultFsm: ResultFsm,
        context: Context,
        speed: Float,
    ): String? {

        var currentStateArea1 = currentStateArea

        // Solo tiene sentido hacer algo si hay cambio de estado
        if (currentStateArea1 != prevState) {

            val accept = shouldAllowStateChange(
                areaId = area.id_area,
                prevState = prevState,
                currentState = currentStateArea1,
                isFast = isFast,
                speed = speed,
                radiusMeters = area.meters.toFloat(),   // <--- NUEVO
                context = context
            )

            if (accept) {
                currentStateArea1?.let {
                    updateCurrentStateArea(context, area.id_area, it)
                }
            } else {
                resultFsm.triggerEnter = false
                resultFsm.triggerExit  = false
                resultFsm.triggerDwellStart = false
                resultFsm.triggerDwellCancel = false
                // Cambio rechazado → mantengo estado previo
                currentStateArea1 = prevState
            }
        }

        return currentStateArea1
    }

    private suspend fun updateCurrentStateArea(
        context: Context,
        areaId: Long,
        currentStateArea: String
    ) {
        val repoAreas = RepositoryAreaDB.getInstance(context)

        val runtimeState = EntityAreaRuntimeState().apply {
            this.id_area = areaId
            this.prev_state_machine = currentStateArea
        }

        val rows = repoAreas.updateAreaStateFSM(runtimeState)
        if (rows == 0) {
            Log.e(Definition.TAG_DEBUG, "MANUAL_STRATEGY: error al actualizar estado para área")
            RepositoryDebugLogger.log(context, "MANUAL_STRATEGY: error al actualizar estado para área ")
        }
    }

    private suspend fun shouldAllowStateChange(
        areaId: Long,
        prevState: String?,
        currentState: String?,
        isFast: Boolean,
        speed: Float,
        radiusMeters: Float,
        context: Context
    ): Boolean {
        return stateChangeMutex.withLock {

            val now = System.currentTimeMillis()
            val lastChange = lastStateChangeTime[areaId] ?: 0L
            val elapsed = now - lastChange

            val isFirstState =
                prevState == null ||
                        prevState == Definition.ST_INIT ||
                        lastChange == 0L

            //Intervalo mínimo dinámico (clave para NO perder eventos en radios chicos)
            val dynamicMinIntervalMs = when {
                isFirstState -> 0L
                isFast       -> 2_000L
                radiusMeters <= 15f -> 4_000L       // radios chicos: NO clavar 15s
                radiusMeters <= 25f -> 6_000L
                else         -> 8_000L
            }

            val accept = elapsed >= dynamicMinIntervalMs || isFirstState

            if (accept) {
                lastStateChangeTime[areaId] = now
                RepositoryDebugLogger.log(
                    context,
                    "MANUAL_STRATEGY: cambio de estado ACEPTADO área=$areaId prev=$prevState curr=$currentState " +
                            "elapsed=${elapsed}ms speed=$speed radius=$radiusMeters minInterval=$dynamicMinIntervalMs"
                )
            } else {
                RepositoryDebugLogger.log(
                    context,
                    "MANUAL_STRATEGY: cambio de estado RECHAZADO área=$areaId prev=$prevState curr=$currentState " +
                            "elapsed=${elapsed}ms < $dynamicMinIntervalMs speed=$speed radius=$radiusMeters"
                )
            }

            accept
        }
    }
}
