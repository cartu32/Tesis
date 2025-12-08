package com.example.comunicationwearmobile.ui.utils.Helpers.Geofences

import android.content.Context
import android.location.Location
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.model.dto.DataAreaGeofAux
import com.example.comunicationwearmobile.ui.model.entities.EntityAreaRuntimeState
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDebugLogger
import com.google.android.gms.location.Geofence

object GeofenceFallBack {
    private val lastStateChangeTime = mutableMapOf<Long, Long>()

    suspend fun callGeofenceFallBack(context: Context, location: Location) {
        try {
            RepositoryDebugLogger.log(context, "FALLBACK: inicio ejecución")
            Log.d(Definition.TAG_DEBUG, "FALLBACK: inicio ejecución")

            proccessFallBack(context, location)
        } catch (e: Exception) {
            RepositoryDebugLogger.log(context, "FALLBACK: error ${e.message}")
        }
    }

    private fun getEvent(
        context: Context,
        area: EntityAreaGeofence,
        location: Location,
        prevState: String?
    ): String {

        var event = Definition.EVENT_CONTINUE

        // Centro del área
        val areaLocation = Location("fallback_area").apply {
            latitude = area.latitude.toDouble()
            longitude = area.longitude.toDouble()
        }

        val distance     = location.distanceTo(areaLocation)
        val radiusMeters = area.meters.toFloat()
        val accuracy     = location.accuracy    // precisión reportada por el GPS

        val distanceToBorder = kotlin.math.abs(distance - radiusMeters)

        // 1) Aplico filtro por accuracy (muy mala)
        if (applyAccuracyFilter(accuracy, context, area))
            return Definition.EVENT_CONTINUE

        // 2) Zona gris cerca del borde, proporcional al radio
        if (isInGrayZone(context, radiusMeters, location.accuracy,distanceToBorder)) {
            return Definition.EVENT_CONTINUE
        }

        // 3) Histeresis espacial fija ----
        val meterForEnter = radiusMeters * Definition.ENTER_FACTOR  // umbral para ENTER (desde afuera)
        val meterForExit  = radiusMeters * Definition.EXIT_FACTOR   // umbral para EXIT (desde adentro)

        // 4) Determino el evento para la FSM de acuerdo a si la persona se movio adentro o afuera del area
        event = determineEventAccordingPosition(prevState, event, distance, radiusMeters, meterForExit, meterForEnter)

        RepositoryDebugLogger.log(
            context,
            "GETEVENT: área=${area.id_area}, prevState=$prevState, event=$event, " +
                    "dist=${"%.1f".format(distance)}m, radius=${"%.1f".format(radiusMeters)}m, " +
                    "acc=${"%.1f".format(accuracy)}m, meterForEnter=${"%.1f".format(meterForEnter)}m, " +
                    "meterForExit=${"%.1f".format(meterForExit)}m, distToBorder=${"%.1f".format(distanceToBorder)}"
        )

        return event
    }

    private fun determineEventAccordingPosition(
        prevState: String?,
        event: String,
        distance: Float,
        radiusMeters: Float,
        meterForExit: Float,
        meterForEnter: Float,
    ): String {
        var event1 = event
        with(Definition) {
            when (prevState) {
                STATE_INIT -> {
                    // Arranque: definimos un estado inicial simple
                    event1 = if (distance <= radiusMeters) EVENT_ENTER else EVENT_EXIT
                }

                STATE_INSIDE -> {
                    // Estaba adentro: sólo disparo EXIT si se fue más allá de 1.2R
                    if (distance >= meterForExit) {
                        event1 = EVENT_EXIT
                    }
                }

                STATE_OUTSIDE -> {
                    // Estaba afuera: sólo disparo ENTER si se metió por debajo de 0.8R
                    if (distance <= meterForEnter) {
                        event1 = EVENT_ENTER
                    }
                }

                else -> {
                    // Estado raro: no cambio nada
                    event1 = EVENT_CONTINUE
                }
            }
        }
        return event1
    }

    private fun applyAccuracyFilter(
        accuracy: Float,
        context: Context,
        area: EntityAreaGeofence,
    ): Boolean {
        val MAX_BAD_ACCURACY = 50f
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

        val rows = repoAreas.updateAreaState(runtimeState)
        if (rows == 0) {
            Log.e(Definition.TAG_DEBUG, "FALLBACK: error al actualizar estado para área")
            RepositoryDebugLogger.log(context, "FALLBACK: error al actualizar estado para área ")
        }
    }

    // --- Fallback completo con histeresis + tiempo mínimo entre cambios ---
    private suspend fun proccessFallBack(context: Context, location: Location) {
        val appContext = context.applicationContext
        var area:EntityAreaGeofence
        
        var currentEventArea:String 
        var currentStateArea:String ?=null
        var prevState:String?=null
        
        var triggerEnter:Boolean
        var triggerExit:Boolean
        
        //1)Obtengo las areas de geofence que estan activas en este momento
        val activatedAreas = getActiveAreas(appContext)
        if (activatedAreas.isEmpty()) {
            RepositoryDebugLogger.log(appContext, "FALLBACK: no hay áreas activas")
            Log.d(Definition.TAG_DEBUG, "FALLBACK: no hay áreas activas")
            return
        }

        val enterIds = mutableListOf<Long>()
        val exitIds  = mutableListOf<Long>()

        val (speed, isFast) = determineSpeedElderly(location, context)


        // 2) Recorro todas las áreas y calculo si hubo cambio de estado
        for (dataArea in activatedAreas) {
            area = dataArea.entityAreaGeofence

            //3) obtengo los eventos  y el estado actual de la maquina de estados
            //   que corresponde a esa area de geofence
            prevState = getPrevState(dataArea)
            currentEventArea = getEvent(context, area, location, prevState)
            currentStateArea = prevState

            triggerEnter = false
            triggerExit  = false

            //4) Aplico la maquina de estado
            val triple = FSM(currentStateArea, currentEventArea, triggerEnter, triggerExit, context)
            
            //de la maquina de estado obtengo el nuevo estado de la fsm y si entro o salio del area
            currentStateArea = triple.first
            triggerEnter = triple.second
            triggerExit = triple.third

            //5) Aplico filtro de tiempo mínimo entre cambios de estado (dinámico por velocidad)
            currentStateArea = updateInBdCurrentStateArea(
                currentStateArea,
                prevState,
                area,
                isFast,
                triggerEnter,
                enterIds,
                triggerExit,
                exitIds,
                context,
                speed
            )
        }

        // 6) Disparo eventos ENTER para todas las áreas que cambiaron a DENTRO aceptadas
        triggerActionAreaEntry(enterIds, appContext)

        // 7) Disparo eventos EXIT para todas las áreas que cambiaron a FUERA aceptadas
        triggerActionAreaExit(exitIds, appContext)
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

    private suspend fun updateInBdCurrentStateArea(
        currentStateArea: String?,
        prevState: String?,
        area: EntityAreaGeofence,
        isFast: Boolean,
        triggerEnter: Boolean,
        enterIds: MutableList<Long>,
        triggerExit: Boolean,
        exitIds: MutableList<Long>,
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
                // Cambio de estado aceptado → actualizo estructuras y BD
                if (triggerEnter) enterIds.add(area.id_area)
                if (triggerExit)  exitIds.add(area.id_area)

                currentStateArea1?.let {
                    updateCurrentStateArea(context, area.id_area, it)
                }

            } else {
                // Cambio rechazado → mantengo estado previo
                currentStateArea1 = prevState
            }
        }

        return currentStateArea1
    }


    private fun FSM(currentStateArea: String?, currentEventArea: String, triggerEnter: Boolean, triggerExit: Boolean, context: Context): Triple<String?, Boolean, Boolean> {
        var currentStateArea1 = currentStateArea
        var triggerEnter1 = triggerEnter
        var triggerExit1 = triggerExit
        
        with(Definition) {
            when (currentStateArea1) {

                STATE_INIT -> {
                    when (currentEventArea) {
                        EVENT_ENTER -> {
                            currentStateArea1 = STATE_INSIDE
                            triggerEnter1 = true   // primer ENTER real
                        }

                        EVENT_EXIT -> {
                            currentStateArea1 = STATE_OUTSIDE
                            // desde INIT no disparo EXIT, sólo fijo que está afuera
                        }

                        else -> { /* CONTINUE */
                        }
                    }
                }

                STATE_INSIDE -> {
                    when (currentEventArea) {
                        EVENT_EXIT -> {
                            currentStateArea1 = STATE_OUTSIDE
                            triggerExit1 = true

                            RepositoryDebugLogger.log(
                                context,
                                "State: $STATE_INSIDE EVT: $EVENT_EXIT"
                            )
                        }

                        else -> { /* CONTINUE */
                        }
                    }
                }

                STATE_OUTSIDE -> {
                    when (currentEventArea) {
                        EVENT_ENTER -> {
                            currentStateArea1 = STATE_INSIDE
                            triggerEnter1 = true

                            RepositoryDebugLogger.log(
                                context,
                                "State: $STATE_OUTSIDE EVT: $EVENT_ENTER"
                            )
                        }

                        else -> { /* CONTINUE */
                        }
                    }
                }

                else -> {
                    // Estado nulo o raro: no hago nada
                }
            }
        }
        return Triple(currentStateArea1, triggerEnter1, triggerExit1)
    }

    private fun shouldAllowStateChange(
        areaId: Long,
        prevState: String?,
        currentState: String?,
        isFast: Boolean,
        speed: Float,
        context: Context
    ): Boolean {

        val now = System.currentTimeMillis()
        val lastChange = lastStateChangeTime[areaId] ?: 0L
        val elapsed = now - lastChange

        val isFirstState =
            prevState == null ||
                    prevState == Definition.STATE_INIT ||
                    lastChange == 0L

        // Ajusto el intervalo mínimo según la velocidad
        val dynamicMinIntervalMs = when {
            isFirstState -> 0L                          // primer cambio siempre permitido
            isFast       -> 3_000L                      // en auto: permito cambios cada 3s
            else         -> Definition.MIN_STATE_CHANGE_INTERVAL_MS // caminando: 15s, por ej.
        }

        val accept = elapsed >= dynamicMinIntervalMs || isFirstState

        if (accept) {
            // Actualizo el timestamp acá
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

        return accept
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

    private fun getPrevState(dataArea: DataAreaGeofAux): String? {
        val runtimeState = dataArea.entityAreaRuntimeState
        return runtimeState.prev_state_machine
    }
}
