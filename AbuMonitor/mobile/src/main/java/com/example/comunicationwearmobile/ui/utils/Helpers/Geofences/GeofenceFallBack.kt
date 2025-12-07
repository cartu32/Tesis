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
import kotlin.math.max
import kotlin.math.min
object GeofenceFallBack {

    // --- Configuración de histeresis espacial ---
    private const val ENTER_FACTOR = 0.8f   // 80% del radio para considerar "ENTRA" (desde afuera)
    private const val EXIT_FACTOR  = 1.2f   // 120% del radio para considerar "SALE" (desde adentro)

    // --- Tiempo mínimo entre cambios de estado (para evitar rebotes) ---
    private const val MIN_STATE_CHANGE_INTERVAL_MS = 15_000L  // 15 segundos (modo caminando)
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

        // ---- 1) Filtro por accuracy (muy mala) ----
        val MAX_BAD_ACCURACY = 50f
        if (accuracy > MAX_BAD_ACCURACY) {
            RepositoryDebugLogger.log(
                context,
                "GETEVENT: area=${area.id_area} accuracy mala=$accuracy (> $MAX_BAD_ACCURACY), CONTINUE"
            )
            return Definition.EVENT_CONTINUE
        }

        // ---- 2) Zona gris cerca del borde, proporcional al radio ----
        val MIN_BORDER_MARGIN   = 5f      // margen mínimo absoluto
        val MAX_BORDER_FRACTION = 0.30f   // como mucho 30% del radio

        val distanceToBorder = kotlin.math.abs(distance - radiusMeters)

        // Usamos la mitad de la accuracy, pero limitada por el radio
        val borderMargin = kotlin.math.min(
            kotlin.math.max(accuracy * 0.5f, MIN_BORDER_MARGIN),
            radiusMeters * MAX_BORDER_FRACTION
        )

        if (distanceToBorder <= borderMargin) {
            RepositoryDebugLogger.log(
                context,
                "GETEVENT: zona gris distToBorder=$distanceToBorder, " +
                           "borderMargin=$borderMargin, acc=$accuracy, CONTINUE"
            )
            return Definition.EVENT_CONTINUE
        }

        // ---- 3) Histeresis espacial fija ----
        val meterForEnter = radiusMeters * ENTER_FACTOR  // umbral para ENTER (desde afuera)
        val meterForExit  = radiusMeters * EXIT_FACTOR   // umbral para EXIT (desde adentro)

        with(Definition) {
            when (prevState) {
                STATE_INIT -> {
                    // Arranque: definimos un estado inicial simple
                    event = if (distance <= radiusMeters) EVENT_ENTER else EVENT_EXIT
                }

                STATE_INSIDE -> {
                    // Estaba adentro: sólo disparo EXIT si se fue más allá de 1.2R
                    if (distance >= meterForExit) {
                        event = EVENT_EXIT
                    }
                }

                STATE_OUTSIDE -> {
                    // Estaba afuera: sólo disparo ENTER si se metió por debajo de 0.8R
                    if (distance <= meterForEnter) {
                        event = EVENT_ENTER
                    }
                }

                else -> {
                    // Estado raro: no cambio nada
                    event = EVENT_CONTINUE
                }
            }
        }

        RepositoryDebugLogger.log(
            context,
            "GETEVENT: área=${area.id_area}, prevState=$prevState, event=$event, " +
                    "dist=${"%.1f".format(distance)}m, radius=${"%.1f".format(radiusMeters)}m, " +
                    "acc=${"%.1f".format(accuracy)}m, meterForEnter=${"%.1f".format(meterForEnter)}m, " +
                    "meterForExit=${"%.1f".format(meterForExit)}m, distToBorder=${"%.1f".format(distanceToBorder)}"
        )

        return event
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
        val activatedAreas = getActiveAreas(appContext)

        if (activatedAreas.isEmpty()) {
            RepositoryDebugLogger.log(appContext, "FALLBACK: no hay áreas activas")
            Log.d(Definition.TAG_DEBUG, "FALLBACK: no hay áreas activas")
            return
        }

        val enterIds = mutableListOf<Long>()
        val exitIds  = mutableListOf<Long>()

        // Velocidad actual (m/s). Sirve para ajustar el intervalo mínimo.
        val speed = location.speed          // velocidad de la persona
        val isFast = speed > Definition.LIMIT_SPEED_WALKING //comparo el limite de la velocidad para determinar si va en auto o caminando

        Log.d(Definition.TAG_DEBUG,"Velocidad limite ${Definition.LIMIT_SPEED_WALKING}")
        if(isFast){
            RepositoryDebugLogger.log(context,"VELOCIDAD $speed EN AUTO")
            Log.d(Definition.TAG_DEBUG, "VELOCIDAD $speed EN AUTO")
        }
        else{
            RepositoryDebugLogger.log(context,"VELOCIDAD $speed EN CAMINANDO")
            Log.d(Definition.TAG_DEBUG, "VELOCIDAD $speed EN CAMINANDO")
        }


        // 2) Recorro todas las áreas y calculo si hubo cambio de estado
        for (dataArea in activatedAreas) {
            val area = dataArea.entityAreaGeofence

            val prevState = getPrevState(dataArea)
            val currentEventArea = getEvent(context, area, location, prevState)
            var currentStateArea = prevState

            var triggerEnter = false
            var triggerExit  = false

            with(Definition) {
                when (currentStateArea) {

                    STATE_INIT -> {
                        when (currentEventArea) {
                            EVENT_ENTER -> {
                                currentStateArea = STATE_INSIDE
                                triggerEnter = true   // primer ENTER real
                            }
                            EVENT_EXIT -> {
                                currentStateArea = STATE_OUTSIDE
                                // desde INIT no disparo EXIT, sólo fijo que está afuera
                            }
                            else -> { /* CONTINUE */ }
                        }
                    }

                    STATE_INSIDE -> {
                        when (currentEventArea) {
                            EVENT_EXIT -> {
                                currentStateArea = STATE_OUTSIDE
                                triggerExit = true

                                RepositoryDebugLogger.log(
                                    context,
                                    "State: $STATE_INSIDE EVT: $EVENT_EXIT"
                                )
                            }
                            else -> { /* CONTINUE */ }
                        }
                    }

                    STATE_OUTSIDE -> {
                        when (currentEventArea) {
                            EVENT_ENTER -> {
                                currentStateArea = STATE_INSIDE
                                triggerEnter = true

                                RepositoryDebugLogger.log(
                                    context,
                                    "State: $STATE_OUTSIDE EVT: $EVENT_ENTER"
                                )
                            }
                            else -> { /* CONTINUE */ }
                        }
                    }

                    else -> {
                        // Estado nulo o raro: no hago nada
                    }
                }
            }

            // --- Filtro de tiempo mínimo entre cambios de estado (dinámico por velocidad) ---
            if (currentStateArea != prevState) {
                val now        = System.currentTimeMillis()
                val lastChange = lastStateChangeTime[area.id_area] ?: 0L
                val elapsed    = now - lastChange

                val isFirstState =
                            prevState == null ||
                            prevState == Definition.STATE_INIT ||
                            lastChange == 0L

                // Ajusto el intervalo mínimo según la velocidad
                val dynamicMinIntervalMs = when {
                    isFirstState -> 0L                          // primer cambio siempre permitido
                    isFast       -> 3_000L                      // en auto: permito cambios cada 3s
                    else         -> MIN_STATE_CHANGE_INTERVAL_MS // caminando: sigo con 15s
                }

                if (elapsed >= dynamicMinIntervalMs || isFirstState) {
                    // Acepto el cambio de estado
                    if (triggerEnter) enterIds.add(area.id_area)
                    if (triggerExit)  exitIds.add(area.id_area)

                    currentStateArea?.let {
                        updateCurrentStateArea(context, area.id_area, it)
                    }
                    lastStateChangeTime[area.id_area] = now

                    RepositoryDebugLogger.log(
                        context,
                        "FALLBACK: cambio de estado ACEPTADO área=${area.id_area} " +
                                "prev=$prevState, curr=$currentStateArea, elapsed=${elapsed}ms, " +
                                "speed=$speed, minInterval=$dynamicMinIntervalMs"
                    )
                } else {
                    // Cambio demasiado rápido → ignoro y mantengo prevState
                    RepositoryDebugLogger.log(
                        context,
                        "FALLBACK: cambio de estado RECHAZADO área=${area.id_area} " +
                                "prev=$prevState, curr=$currentStateArea, elapsed=${elapsed}ms < $dynamicMinIntervalMs, speed=$speed"
                    )
                    currentStateArea = prevState
                }
            }
        }

        // 4) Disparo eventos ENTER para todas las áreas que cambiaron a DENTRO aceptadas
        if (enterIds.isNotEmpty()) {
            RepositoryDebugLogger.log(appContext, "FALLBACK: disparo ENTER para ids=$enterIds")
            Log.d(Definition.TAG_DEBUG, "FALLBACK: disparo ENTER para ids=$enterIds")

            GeofenceEventProcessorHelper.handleEvent(
                triggeringIds = enterIds,
                transition = Geofence.GEOFENCE_TRANSITION_ENTER
            )
        }

        // 5) Disparo eventos EXIT para todas las áreas que cambiaron a FUERA aceptadas
        if (exitIds.isNotEmpty()) {
            RepositoryDebugLogger.log(appContext, "FALLBACK: disparo EXIT para ids=$exitIds")
            Log.d(Definition.TAG_DEBUG, "FALLBACK: disparo EXIT para ids=$exitIds")

            GeofenceEventProcessorHelper.handleEvent(
                triggeringIds = exitIds,
                transition = Geofence.GEOFENCE_TRANSITION_EXIT
            )
        }
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
