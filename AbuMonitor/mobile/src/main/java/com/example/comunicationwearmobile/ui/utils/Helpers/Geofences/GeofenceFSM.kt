package com.example.comunicationwearmobile.ui.utils.Helpers.Geofences

import android.content.Context
import android.location.Location
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.common.PermissionsArea
import com.example.comunicationwearmobile.ui.common.ResultFsm
import com.example.comunicationwearmobile.ui.model.dto.DataAreaGeofAux
import com.example.comunicationwearmobile.ui.model.entities.EntityAreaRuntimeState
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDebugLogger
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object GeofenceFSM {

    private val lastStateChangeTime = mutableMapOf<Long, Long>()
    private val stateChangeMutex = Mutex()

    suspend fun proccessFSM(
        dataArea: DataAreaGeofAux,
        appContext: Context,
        area: EntityAreaGeofence,
        location: Location,
        isFast: Boolean,
        speed: Float,
    ): ResultFsm {
        // 3.1) Estado previo de la FSM para esta área
        val prevState: String? = getPrevState(dataArea)

        // 3.2) Evento actual según posición + histeresis + filtros
        val currentEventArea = getEvent(appContext, area, location, prevState)

        // 3.3) Permisos configurados para esta área (ENTER / EXIT / DWELL)
        val permissions = getPermissionGrantes(dataArea.listIdEventSelected)

        // si es CONTINUE no llamo a la FSM
        if (currentEventArea == Definition.EVT_CONTINUE) {
            return ResultFsm(
                currentState = prevState,
                triggerEnter = false,
                triggerExit = false,
                triggerDwellStart = false,
                triggerDwellCancel = false,
                action = null
            )
        }

        // 4) Aplico la máquina de estados (FSM) solo cuando hay evento real
        val resultFsm = FSM(
            prevState = prevState,
            event = currentEventArea,
            permissions = permissions,
            appContext = appContext
        )

        // retorno si no hubo transición real en la fsm
        val noOp =
            (resultFsm.currentState == prevState) &&
                    (resultFsm.action == null) &&
                    !resultFsm.triggerEnter &&
                    !resultFsm.triggerExit &&
                    !resultFsm.triggerDwellStart &&
                    !resultFsm.triggerDwellCancel

        if (noOp) {
            return resultFsm
        }

        // Logueo solo si realmente “pasó algo”
        RepositoryDebugLogger.log(
            appContext,
            "FSM: newState=${resultFsm.currentState} | event=$currentEventArea | action=${resultFsm.action}" +
                    "|triggerEnter=${resultFsm.triggerEnter} | triggerExit=${resultFsm.triggerExit}"
        )
        Log.d(
            Definition.TAG_DEBUG,
            "FSM: newState=${resultFsm.currentState} | event=$currentEventArea | action=${resultFsm.action}" +
                    " | triggerEnter=${resultFsm.triggerEnter} | triggerExit=${resultFsm.triggerExit}"
        )

        // 5) Solo si hay cambio potencial, aplico el filtro de tiempo mínimo + update DB
        val newState = updateInBdCurrentStateArea(
            currentStateArea = resultFsm.currentState,
            prevState = prevState,
            area = area,
            isFast = isFast,
            resultFsm = resultFsm,
            context = appContext,
            speed = speed
        )

        resultFsm.currentState = newState

        return resultFsm
    }


    fun FSM(
        prevState: String?,
        event: String,
        permissions: PermissionsArea,
        appContext: Context
    ): ResultFsm {

        var newState       = prevState
        var fireEnter      = false
        var fireExit       = false
        var fireDwellStart = false
        var fireDwellCancel= false
        var action: String?= null


        with(Definition) {
            when (prevState) {
                ST_INIT -> {
                    when (event) {
                        EVT_ENTER -> {
                            newState = ST_INSIDE
                            if (permissions.enter && permissions.dwell) {
                                fireEnter = true
                                fireDwellStart = true
                                action = ACT_ENTER_AND_DWELL
                            } else if (permissions.enter) {
                                fireEnter = true
                                action = ACT_ENTER_ONLY
                            } else if (permissions.dwell) {
                                fireDwellStart = true
                                action = ACT_DWELL_ONLY
                            } else {
                                action = ACT_SILENT_ENTER
                            }                        }

                        EVT_EXIT -> {
                            newState = ST_OUTSIDE
                            if (permissions.exit) {
                                action = ACT_EXIT
                            } else {
                                action = ACT_SILENT_EXIT
                            }
                        }

                        EVT_CONTINUE -> {
                            // No cambio de estado ni acción
                        }
                    }
                }

                ST_INSIDE -> {
                    when (event) {
                        EVT_EXIT -> {
                            newState = ST_OUTSIDE
                            fireDwellCancel=true
                            if (permissions.exit) {
                                fireExit = true
                                action = ACT_EXIT
                            } else {
                                action = ACT_SILENT_EXIT
                            }
                        }

                        EVT_ENTER, EVT_CONTINUE -> {
                            // Sigue adentro, no hay cambio de estado
                        }
                    }
                }

                ST_OUTSIDE -> {
                    when (event) {
                        EVT_ENTER -> {
                            // Entra al área
                            newState = ST_INSIDE
                            if (permissions.enter && permissions.dwell) {
                                fireEnter = true
                                fireDwellStart = true
                                action = ACT_ENTER_AND_DWELL
                            } else if (permissions.enter) {
                                fireEnter = true
                                action = ACT_ENTER_ONLY
                            } else if (permissions.dwell) {
                                fireDwellStart = true
                                action = ACT_DWELL_ONLY
                            } else {
                                action = ACT_SILENT_ENTER
                            }
                        }

                        EVT_EXIT, EVT_CONTINUE -> {
                            // Sigue afuera, no cambio de estado
                        }
                    }
                }
            }
        }
        RepositoryDebugLogger.log(appContext, "FSM: newState=$newState | event=$event | action=$action|triggerEnter=$fireEnter | triggerExit=$fireExit")
        Log.d(Definition.TAG_DEBUG, "FSM: newState=$newState | event=$event | action=$action | triggerEnter=$fireEnter | triggerExit=$fireExit")

        return ResultFsm(
            currentState      = newState,
            triggerEnter      = fireEnter,
            triggerExit       = fireExit,
            triggerDwellStart = fireDwellStart,
            triggerDwellCancel= fireDwellCancel,
            action            = action
        )
    }

    private fun getPrevState(dataArea: DataAreaGeofAux): String? {
        val runtimeState = dataArea.entityAreaRuntimeState
        return runtimeState.prev_state_machine
    }


     private suspend fun updateInBdCurrentStateArea(
        currentStateArea: String?,
        prevState: String?,
        area: EntityAreaGeofence,
        isFast: Boolean,
        resultFsm:ResultFsm,
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
            Log.e(Definition.TAG_DEBUG, "FALLBACK: error al actualizar estado para área")
            RepositoryDebugLogger.log(context, "FALLBACK: error al actualizar estado para área ")
        }
    }


    fun getEvent(
        context: Context,
        area: EntityAreaGeofence,
        location: Location,
        prevState: String?
    ): String {

        var event = Definition.EVT_CONTINUE

        // Centro del área
        val areaLocation = Location("fallback_area").apply {
            latitude = area.latitude.toDouble()
            longitude = area.longitude.toDouble()
        }

        //obtengo la distancia entre la ubicacion actual y el centro de la zona de geofence
        val distance     = location.distanceTo(areaLocation)
        val radiusMeters = area.meters.toFloat()
        val accuracy     = location.accuracy    // precisión reportada por el GPS
        //la precisión del gps me afectar la medicion dentro un radio determinado.
        //Si la precisión es de 10 metros, esto quiere decir que desde la ubicación
        // que me reporta el gps.la ubcación real puede estar al rededor de 10 metros de ese punto
        // Por lo que la posicion real esta +-10 metros a la redonda..
        val distanceToBorder = kotlin.math.abs(distance - radiusMeters)

        // 1) Aplico filtro por accuracy (muy mala)
        if (applyAccuracyFilter(accuracy, context, area))
            return Definition.EVT_CONTINUE

        // 2) Zona gris cerca del borde, proporcional al radio
        if (isInGrayZone(context, radiusMeters, location.accuracy,distanceToBorder)) {
            return Definition.EVT_CONTINUE
        }

        // 3) Histeresis espacial que depende de la precision del gps
        val (meterForEnter, meterForExit) = calculateHysteris(accuracy, radiusMeters)

        // 4) Determino el evento para la FSM de acuerdo a si la persona se movio adentro o afuera del area
        event = determineEventAccordingPosition(prevState, distance, radiusMeters, meterForExit, meterForEnter)

        RepositoryDebugLogger.log(
            context,
            "GETEVENT: área=${area.id_area}, prevState=$prevState, event=$event, " +
                    "dist=${"%.1f".format(distance)}m, radius=${"%.1f".format(radiusMeters)}m, " +
                    "acc=${"%.1f".format(accuracy)}m, meterForEnter=${"%.1f".format(meterForEnter)}m, " +
                    "meterForExit=${"%.1f".format(meterForExit)}m, distToBorder=${"%.1f".format(distanceToBorder)}"
        )
        Log.d(Definition.TAG_DEBUG,"GETEVENT: área=${area.id_area}, prevState=$prevState, event=$event, " +
                "dist=${"%.1f".format(distance)}m, radius=${"%.1f".format(radiusMeters)}m, " +
                "acc=${"%.1f".format(accuracy)}m, meterForEnter=${"%.1f".format(meterForEnter)}m, " +
                "meterForExit=${"%.1f".format(meterForExit)}m, distToBorder=${"%.1f".format(distanceToBorder)}")

        return event
    }

    private fun calculateHysteris(
        accuracy: Float,
        radiusMeters: Float,
    ): Pair<Float, Float> {
        val extraMargin = (accuracy / radiusMeters).coerceAtMost(0.2f)

        val enterFactor = Definition.BASE_ENTER_FACTOR - extraMargin
        val exitFactor = Definition.BASE_EXIT_FACTOR + extraMargin

        val meterForEnter = radiusMeters * enterFactor  // umbral para ENTER (desde afuera)
        val meterForExit = radiusMeters * exitFactor   // umbral para EXIT (desde adentro)
        return Pair(meterForEnter, meterForExit)
    }

    private fun determineEventAccordingPosition(
        prevState: String?,
        distance: Float,
        radiusMeters: Float,
        meterForExit: Float,
        meterForEnter: Float,
    ): String {
        with(Definition) {
            var event1 = EVT_CONTINUE
            when (prevState) {
                ST_INIT -> {
                    // Arranque: definimos un estado inicial simple
                    event1 = if (distance <= radiusMeters) EVT_ENTER else EVT_EXIT
                }

                ST_INSIDE -> {
                    // Estaba adentro: sólo disparo EXIT si se fue más allá de 1.2R
                    if (distance >= meterForExit) {
                        event1 = EVT_EXIT
                    }
                }

                ST_OUTSIDE -> {
                    // Estaba afuera: sólo disparo ENTER si se metió por debajo de 0.8R
                    if (distance <= meterForEnter) {
                        event1 = EVT_ENTER
                    }
                }

                else -> {
                    // Estado raro: no cambio nada
                }
            }
            return event1
        }
    }

    private fun applyAccuracyFilter(
        accuracy: Float,
        context: Context,
        area: EntityAreaGeofence,
    ): Boolean {
        val MAX_BAD_ACCURACY = 40f
        if (accuracy > MAX_BAD_ACCURACY) {
            RepositoryDebugLogger.log(
                context,
                "GETEVENT: area=${area.id_area} accuracy mala=$accuracy (> $MAX_BAD_ACCURACY), CONTINUE"
            )
            return true
        }
        return false
    }

    private fun isInGrayZone(
        context: Context,
        radiusMeters: Float,
        accuracy: Float,
        distanceToBorder: Float
    ): Boolean {

        // Se toma la mitad de la precisión del GPS, pero limitada
        // entre un mínimo fijo y un máximo proporcional al radio
        val borderMargin = kotlin.math.min(
            kotlin.math.max(accuracy * 0.5f, Definition.MIN_BORDER_MARGIN),
            radiusMeters * Definition.MAX_BORDER_FRACTION
        )

        // Si la distancia al borde cae dentro de esta zona gris,
        // se ignora el evento para evitar falsas entradas/salidas
        if (distanceToBorder <= borderMargin) {
            RepositoryDebugLogger.log(
                context,
                "GETEVENT: zona gris distToBorder=$distanceToBorder, " +
                        "borderMargin=$borderMargin, acc=$accuracy, CONTINUE"
            )
            return true
        }

        return false
    }

    private suspend fun shouldAllowStateChange(
        areaId: Long,
        prevState: String?,
        currentState: String?,
        isFast: Boolean,
        speed: Float,
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

            // Ajusto el intervalo mínimo según la velocidad
            val dynamicMinIntervalMs = when {
                isFirstState -> 0L                          // primer cambio siempre permitido
                isFast       -> 3_000L                      // en auto: permito cambios cada 3s
                else         -> Definition.MIN_STATE_CHANGE_INTERVAL_MS // caminando: 15s, por ej.
            }

            val accept = elapsed >= dynamicMinIntervalMs || isFirstState

            if (accept) {
                // Actualizo el timestamp acá, dentro del mutex
                lastStateChangeTime[areaId] = now

                RepositoryDebugLogger.log(
                    context,
                    "FALLBACK: cambio de estado ACEPTADO área=$areaId " +
                            "prev=$prevState, curr=$currentState, " +
                            "elapsed=${elapsed}ms, speed=$speed, " +
                            "minInterval=$dynamicMinIntervalMs"
                )
            } else {
                RepositoryDebugLogger.log(
                    context,
                    "FALLBACK: cambio de estado RECHAZADO área=$areaId " +
                            "prev=$prevState, curr=$currentState, " +
                            "elapsed=${elapsed}ms < $dynamicMinIntervalMs, speed=$speed"
                )
            }

            accept
        }
    }

    private fun getPermissionGrantes(listIdEventSelected: List<Int>): PermissionsArea {
        val permissions = PermissionsArea()

        permissions.enter = listIdEventSelected.contains(Definition.GEOFENCE_EVENT_ID_ENTER)
        permissions.exit  = listIdEventSelected.contains(Definition.GEOFENCE_EVENT_ID_EXIT)
        permissions.dwell = listIdEventSelected.contains(Definition.GEOFENCE_EVENT_ID_DWELL)

        return permissions
    }

}