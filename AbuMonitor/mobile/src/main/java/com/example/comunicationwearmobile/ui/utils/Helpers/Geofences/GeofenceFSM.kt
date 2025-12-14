package com.example.comunicationwearmobile.ui.utils.Helpers.Geofences

import android.content.Context
import android.location.Location
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.model.dto.PermissionsArea
import com.example.comunicationwearmobile.ui.model.dto.ResultFsm
import com.example.comunicationwearmobile.ui.model.dto.DataAreaGeofAux
import com.example.comunicationwearmobile.ui.model.entities.EntityAreaRuntimeState
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDebugLogger
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object GeofenceFSM {

    private val lastStateChangeTime = mutableMapOf<Long, Long>()
    private val stateChangeMutex = Mutex()



    private data class AreaTrack(
        var lastEventAt: Long = 0L,
        var lastFlipAt: Long = 0L,
        var lastLocAt: Long = 0L,
        var lastLat: Double = 0.0,
        var lastLon: Double = 0.0,
        var stationarySince: Long = 0L,
        var stationaryAccumMove: Float = 0f,
        var insideStreak: Int = 0,
        var outsideStreak: Int = 0,
        var lastDistToCenter: Float = -1f
    )

    private val track = mutableMapOf<Long, AreaTrack>()
    private val trackMutex = Mutex()

    // Debounce (lecturas consecutivas requeridas)
    private const val ENTER_CONFIRM_COUNT = 1
    private const val EXIT_CONFIRM_COUNT  = 1

    // Anti-spam / anti-oscilación
    private const val MIN_EVENT_GAP_MS =  10_000L  // no repetir enter/exit cada pocos segundos

    // Estacionario
    private const val STATIONARY_WINDOW_MS    = 90_000L
    private const val STATIONARY_MAX_MOVE_M   = 8f
    private const val STATIONARY_MAX_SPEED_MS = 0.4f

    private suspend fun <T> withTrack(areaId: Long, block: (AreaTrack) -> T): T {
        return trackMutex.withLock {
            val t = track.getOrPut(areaId) { AreaTrack() }
            block(t)
        }
    }

    private fun accuracyAbsLimit(radiusMeters: Float): Float {
        // Más estricto en radios chicos (20-30m diámetro), más laxo en radios grandes.
        return when {
            radiusMeters <= 10f -> 12f     // diam <= 20m
            radiusMeters <= 15f -> 18f     // diam <= 30m
            radiusMeters <= 25f -> 25f     // diam <= 50m
            else -> 35f                    // radios grandes
        }
    }

    private fun isAccuracyTooBad(accuracy: Float, radiusMeters: Float): Boolean {
        val absLimit = accuracyAbsLimit(radiusMeters)
        val ratioLimit = radiusMeters * 0.60f
        // si el radio es grande, el ratio puede ser muy alto, por eso usamos ambos (ABS + ratio)
        return (accuracy > absLimit) || (accuracy > ratioLimit)
    }

    private fun isStationaryUpdate(t: AreaTrack, location: Location, now: Long): Boolean {
        if (t.lastLocAt == 0L) {
            t.lastLocAt = now
            t.lastLat = location.latitude
            t.lastLon = location.longitude
            t.stationarySince = now
            t.stationaryAccumMove = 0f
            return false
        }

        val prev = Location("prev").apply {
            latitude = t.lastLat
            longitude = t.lastLon
        }
        val d = location.distanceTo(prev)

        t.stationaryAccumMove += d
        t.lastLocAt = now
        t.lastLat = location.latitude
        t.lastLon = location.longitude

        if (t.stationaryAccumMove > STATIONARY_MAX_MOVE_M) {
            t.stationarySince = now
            t.stationaryAccumMove = 0f
            return false
        }

        val speedOk = (!location.hasSpeed()) || (location.speed <= STATIONARY_MAX_SPEED_MS)
        val timeOk  = (now - t.stationarySince) >= STATIONARY_WINDOW_MS
        return speedOk && timeOk
    }

    private fun flipCooldownMs(isFast: Boolean, stationary: Boolean): Long {
        return when {
            stationary -> 8 * 60_000L   // quieto: súper duro (8 minuots)
            isFast     -> 5_000L       // auto: no frenes la salida/entrada real(10 segundos)
            else       -> 10_000L   // caminando: moderado (30 segundos)
        }
    }

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
        val currentEventArea = getEvent(appContext, area, location, prevState, isFast, speed)

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

        // Logueo solo si realmente paso algo
        RepositoryDebugLogger.log(appContext, "FSM: newState=${resultFsm.currentState} | event=$currentEventArea | action=${resultFsm.action}|triggerEnter=${resultFsm.triggerEnter} | triggerExit=${resultFsm.triggerExit}")
        Log.d(Definition.TAG_DEBUG, "FSM: newState=${resultFsm.currentState} | event=$currentEventArea | action=${resultFsm.action} | triggerEnter=${resultFsm.triggerEnter} | triggerExit=${resultFsm.triggerExit}")

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
                radiusMeters = area.meters.toFloat(),
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



    suspend fun getEvent(
        context: Context,
        area: EntityAreaGeofence,
        location: Location,
        prevState: String?,
        isFast: Boolean,
        speed: Float
    ): String {

        val now = System.currentTimeMillis()

        // Centro del área
        val areaLocation = Location("fallback_area").apply {
            latitude = area.latitude.toDouble()
            longitude = area.longitude.toDouble()
        }

        val distance = location.distanceTo(areaLocation)
        val radiusMeters = area.meters.toFloat()
        val accuracy = location.accuracy
        val distanceToBorder = kotlin.math.abs(distance - radiusMeters)

        // Track + stationary (actualiza historial de ubicaciones)
        val stationary = withTrack(area.id_area) { t ->
            isStationaryUpdate(t, location, now)
        }

        // 1) Filtro por accuracy (ABS + ratio vs radio)
        if (isAccuracyTooBad(accuracy, radiusMeters)) {
            RepositoryDebugLogger.log(
                context,
                "GETEVENT_BLIND: BLOCK accuracy area=${area.id_area} acc=${"%.1f".format(accuracy)} " +
                        "absLimit=${"%.1f".format(accuracyAbsLimit(radiusMeters))} ratioLimit=${"%.1f".format(radiusMeters * 0.60f)}"
            )
            return Definition.EVT_CONTINUE
        }

        // 2) Zona gris cerca del borde (tu lógica)
        if (isInGrayZone(context, radiusMeters, accuracy, distanceToBorder)) {
            RepositoryDebugLogger.log(
                context,
                "GETEVENT_BLIND: BLOCK grayzone area=${area.id_area} distToBorder=${"%.1f".format(distanceToBorder)}"
            )
            return Definition.EVT_CONTINUE
        }

        // 3) Histeresis espacial
        val (meterForEnter, meterForExit) = calculateHysteris(accuracy, radiusMeters)

        // 4) Candidato según posición
        val candidate = determineEventAccordingPosition(
            prevState = prevState,
            distance = distance,
            radiusMeters = radiusMeters,
            meterForExit = meterForExit,
            meterForEnter = meterForEnter
        )

        if (candidate == Definition.EVT_CONTINUE) {
            // reset suave de streaks cuando no hay transición candidata
            withTrack(area.id_area) { t ->
                t.insideStreak = 0
                t.outsideStreak = 0
            }
            return Definition.EVT_CONTINUE
        }

        // 5) Anti-spam (no repetir eventos demasiado seguido)
        val spamBlocked = withTrack(area.id_area) { t ->
            (now - t.lastEventAt) < MIN_EVENT_GAP_MS
        }
        if (spamBlocked) {
            RepositoryDebugLogger.log(context, "GETEVENT_BLIND: BLOCK spam area=${area.id_area}")
            return Definition.EVT_CONTINUE
        }

        // 6) Cooldown anti flip dinámico (auto/caminando/quieto)
        val minFlip = flipCooldownMs(isFast = isFast, stationary = stationary)
        val flipBlocked = withTrack(area.id_area) { t ->
            (now - t.lastFlipAt) < minFlip
        }

        // "escape hatch": si estás MUY afuera del umbral de salida, permito exit aunque haya cooldown
        val farOutside = (candidate == Definition.EVT_EXIT) &&
                (distance >= (meterForExit + maxOf(accuracy * 1.5f, radiusMeters * 0.5f, 10f)))

        val lastFlipAt = withTrack(area.id_area) { it.lastFlipAt }
        if (flipBlocked && !farOutside) {
            RepositoryDebugLogger.log(
                context,
                "GETEVENT_BLIND: BLOCK flipCooldown area=${area.id_area} elapsed=${now - lastFlipAt}ms " +
                        "minFlip=$minFlip stationary=$stationary isFast=$isFast"
            )
            return Definition.EVT_CONTINUE
        }

        if (stationary && candidate == Definition.EVT_EXIT) {
            // Exijo que sea una salida "clara"
            // salida clara = más allá del umbral de salida + margen por precisión (o mínimo fijo)
            val clearExitMargin = maxOf(accuracy, 5f)
            val clearExit = distance >= (meterForExit + clearExitMargin)

            if (!clearExit) {
                RepositoryDebugLogger.log(
                    context,
                    "GETEVENT_BLIND: BLOCK stationaryExit(notClear) area=${area.id_area} " +
                            "dist=${"%.1f".format(distance)} meterForExit=${"%.1f".format(meterForExit)} " +
                            "margin=${"%.1f".format(clearExitMargin)}"
                )
                withTrack(area.id_area) { t -> t.outsideStreak = 0 }
                return Definition.EVT_CONTINUE
            }
        }

        // --- Heurística anti-teleport para EXIT (sin subir EXIT_CONFIRM_COUNT global) ---
        val lastDist = withTrack(area.id_area) { it.lastDistToCenter }
        val jump = if (lastDist >= 0f) kotlin.math.abs(distance - lastDist) else 0f
        val overshoot = distance - meterForExit // qué tanto te pasaste del umbral de salida

        // EXIT sospechoso si la precisión es “grande” para el radio o si hay salto fuerte en 1 tick
        val suspiciousExit =
            candidate == Definition.EVT_EXIT && (
                    accuracy > radiusMeters * 0.35f ||                 // en R=28 => >9.8m
                            (lastDist >= 0f && jump > maxOf(accuracy * 2f, 25f)) // teleport típico
                    )

        // Si estás MUY afuera, no pidas 2 (así no perdés EXIT reales)
        val strongExit =
            candidate == Definition.EVT_EXIT &&
                    overshoot >= maxOf(accuracy * 1.2f, 12f)

        // Confirmaciones requeridas solo para EXIT
        val requiredExitConfirm = when {
            strongExit -> 1
            suspiciousExit -> 2
            else -> EXIT_CONFIRM_COUNT   // dejalo en 1 normalmente
        }

        if (candidate == Definition.EVT_EXIT && suspiciousExit && !strongExit) {
            RepositoryDebugLogger.log(
                context,
                "GETEVENT_BLIND: EXIT requires2 area=${area.id_area} " +
                        "acc=${"%.1f".format(accuracy)} jump=${"%.1f".format(jump)} overshoot=${"%.1f".format(overshoot)}"
            )
        }



        // 8) Debounce por lecturas consecutivas
        val confirmed = withTrack(area.id_area) { t ->
            when (candidate) {
                Definition.EVT_ENTER -> {
                    t.insideStreak += 1
                    t.outsideStreak = 0
                    t.insideStreak >= ENTER_CONFIRM_COUNT
                }
                Definition.EVT_EXIT -> {
                    t.outsideStreak += 1
                    t.insideStreak = 0
                    t.outsideStreak >= requiredExitConfirm
                }

                else -> false
            }
        }

        if (!confirmed) {
            RepositoryDebugLogger.log(
                context,
                "GETEVENT_BLIND: WAIT confirm area=${area.id_area} cand=$candidate " +
                        "insideStreak=${withTrack(area.id_area){it.insideStreak}} outsideStreak=${withTrack(area.id_area){it.outsideStreak}}"
            )
            return Definition.EVT_CONTINUE
        }

        // 9) Acepto evento: actualizo track
        withTrack(area.id_area) { t ->
            t.lastEventAt = now
            t.lastFlipAt = now
            t.lastDistToCenter = distance
            t.insideStreak = 0
            t.outsideStreak = 0
        }

        RepositoryDebugLogger.log(
            context,
            "GETEVENT_BLIND: área=${area.id_area}, prevState=$prevState, event=$candidate, " +
                    "dist=${"%.1f".format(distance)}m, radius=${"%.1f".format(radiusMeters)}m, " +
                    "acc=${"%.1f".format(accuracy)}m, enter=${"%.1f".format(meterForEnter)}m, " +
                    "exit=${"%.1f".format(meterForExit)}m, distToBorder=${"%.1f".format(distanceToBorder)}m, " +
                    "stationary=$stationary isFast=$isFast speed=${"%.2f".format(speed)}"
        )
        Log.d(
            Definition.TAG_DEBUG,
            "GETEVENT_BLIND: área=${area.id_area}, prevState=$prevState, event=$candidate, " +
                    "dist=${"%.1f".format(distance)}m, radius=${"%.1f".format(radiusMeters)}m, " +
                    "acc=${"%.1f".format(accuracy)}m, enter=${"%.1f".format(meterForEnter)}m, " +
                    "exit=${"%.1f".format(meterForExit)}m, distToBorder=${"%.1f".format(distanceToBorder)}m, " +
                    "stationary=$stationary isFast=$isFast speed=${"%.2f".format(speed)}"
        )

        return candidate
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

        // En radios chicos, la gray-zone no puede comerse medio geofence
        val maxFraction = if (radiusMeters <= 15f) 0.15f else Definition.MAX_BORDER_FRACTION

        // margen basado en accuracy, pero acotado fuerte
        val borderMargin = kotlin.math.min(
            kotlin.math.max(accuracy * 0.35f, 3f),              // 35% de acc, mínimo 3m
            kotlin.math.min(radiusMeters * maxFraction, 6f)     // cap: fracción y 6m absoluto
        )

        if (distanceToBorder <= borderMargin) {
            RepositoryDebugLogger.log(
                context,
                "GETEVENT: zona gris distToBorder=${"%.1f".format(distanceToBorder)}, " +
                        "borderMargin=${"%.1f".format(borderMargin)}, acc=${"%.1f".format(accuracy)}, CONTINUE"
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
        radiusMeters: Float,   // <--- NUEVO
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

            // ✅ Intervalo mínimo dinámico (clave para NO perder eventos en radios chicos)
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
                    "FALLBACK: cambio de estado ACEPTADO área=$areaId prev=$prevState curr=$currentState " +
                            "elapsed=${elapsed}ms speed=$speed radius=$radiusMeters minInterval=$dynamicMinIntervalMs"
                )
            } else {
                RepositoryDebugLogger.log(
                    context,
                    "FALLBACK: cambio de estado RECHAZADO área=$areaId prev=$prevState curr=$currentState " +
                            "elapsed=${elapsed}ms < $dynamicMinIntervalMs speed=$speed radius=$radiusMeters"
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