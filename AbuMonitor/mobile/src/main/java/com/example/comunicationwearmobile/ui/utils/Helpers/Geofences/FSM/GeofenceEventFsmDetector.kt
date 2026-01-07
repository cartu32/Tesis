package com.example.comunicationwearmobile.ui.utils.Helpers.Geofences

import android.content.Context
import android.location.Location
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDebugLogger
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.FSM.GeofenceTrackStore

object GeofenceEventFsmDetector {

    // Debounce (lecturas consecutivas requeridas)
    private const val ENTER_CONFIRM_COUNT = 1
    private const val EXIT_CONFIRM_COUNT  = 1

    // Anti-spam / anti-oscilación. Sirve para no repetir enter/exit cada pocos segundos
    //Esta seria la cantidad de tiempo que debe esperarse entre eventos consecutivos.
    private const val MIN_EVENT_GAP_MS =  10_000L

    suspend fun getEvent(context: Context, area: EntityAreaGeofence, location: Location, prevState: String?, isFast: Boolean, speed: Float): String {

        val now = System.currentTimeMillis()

        // Caclulo cual es el centro del área (Location auxiliar para calcular distancia)
        val areaLocation = buildAreaCenterLocation(area)

        // Métricas básicas (distancia, radio, accuracy, distancia al borde)
        val m = computeMetrics(area, location, areaLocation)

        //1) Se actualiza el historial de ubicaciones que me permite mas adelante
        //  saber si la persona estuvo quieta mucho tiempo en el mismo lugar.
        //  El historial esta en mapa llamado track
        val stationary = GeofenceTrackStore.getStationaryInHitorialLocation(area, location, now)

        // 2) Aplico filtro por accuracy (valor absoluto + ratio vs radio)
        if (blockByAccuracy(context, area, m.accuracy, m.radiusMeters)) {
            return Definition.EVT_CONTINUE
        }

        // 3) Aplico filtro por “zona gris” cerca del borde
        if (blockByGrayZone(context, area, m.radiusMeters, m.accuracy, m.distanceToBorder)) {
            return Definition.EVT_CONTINUE
        }

        // 4)Aplico Histeresis espacial (umbrales enter/exit)
        val (meterForEnter, meterForExit) = calculateHysteris(m.accuracy, m.radiusMeters)

        // 5)Determino cual es el evento candidato según posición (todavía no se acepta).
        //   Cuando no hay transiciones reseteo el contador de eventos Exit/enter consecutivos
        val candidate = determineEventAccordingPosition(
            prevState = prevState,
            distance = m.distance,
            radiusMeters = m.radiusMeters,
            meterForExit = meterForExit,
            meterForEnter = meterForEnter
        )

        // 6) Si no hay transición real, resetea streaks y sale
        if (resetStreaksAndReturnIfContinue(area, candidate)) {
            return Definition.EVT_CONTINUE
        }

        // 7) Anti-spam (no repetir eventos demasiado seguido)
        if (blockBySpam(context, area, now)) {
            return Definition.EVT_CONTINUE
        }

        // 8) Anti-flip (cooldown dinámico) + excepción farOutside
        if (blockByFlipCooldown(context, area, now, isFast, stationary, candidate, m, meterForExit)) {
            return Definition.EVT_CONTINUE
        }

        // 9) Me fijo si está quieto y EXIT es un evento candidato
        if (blockStationaryExitIfNotClear(context, area, stationary, candidate, m.distance, meterForExit, m.accuracy)) {
            return Definition.EVT_CONTINUE
        }

        // 10) Heurística anti-teleport para EXIT. Esto ev
        val requiredExitConfirm = computeRequiredExitConfirmAndLog(
            context = context,
            area = area,
            candidate = candidate,
            distance = m.distance,
            meterForExit = meterForExit,
            accuracy = m.accuracy,
            radiusMeters = m.radiusMeters
        )

        // 11) Debounce por lecturas consecutivas (confirmación por streaks)
        val confirmed = confirmByStreaks(area, candidate, requiredExitConfirm)
        if (!confirmed) {
            logWaitConfirm(context, area, candidate)
            return Definition.EVT_CONTINUE
        }

        // 12) Acepto evento: actualizo track
        acceptEventAndUpdateTrack(area, now, m.distance)

        // 13) Log final (debug)
        logAcceptedEvent(
            context = context,
            area = area,
            prevState = prevState,
            candidate = candidate,
            distance = m.distance,
            radiusMeters = m.radiusMeters,
            accuracy = m.accuracy,
            meterForEnter = meterForEnter,
            meterForExit = meterForExit,
            distanceToBorder = m.distanceToBorder,
            stationary = stationary,
            isFast = isFast,
            speed = speed
        )

        return candidate
    }

    /** Crea un Location con el centro del área para poder usar distanceTo(). */
    private fun buildAreaCenterLocation(area: EntityAreaGeofence): Location {
        return Location("fallback_area").apply {
            latitude = area.latitude.toDouble()
            longitude = area.longitude.toDouble()
        }
    }

    /** Calcula métricas base usadas por los filtros y la FSM (distancia, radio, accuracy, borde). */
    private data class Metrics(
        val distance: Float,
        val radiusMeters: Float,
        val accuracy: Float,
        val distanceToBorder: Float
    )

    private fun computeMetrics(
        area: EntityAreaGeofence,
        location: Location,
        areaLocation: Location
    ): Metrics {
        val distance = location.distanceTo(areaLocation)
        val radiusMeters = area.meters.toFloat()
        val accuracy = location.accuracy
        val distanceToBorder = kotlin.math.abs(distance - radiusMeters)
        return Metrics(distance, radiusMeters, accuracy, distanceToBorder)
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

    /** Bloquea el procesamiento si el accuracy es demasiado malo (ABS + ratio vs radio). */
    private fun blockByAccuracy(
        context: Context,
        area: EntityAreaGeofence,
        accuracy: Float,
        radiusMeters: Float
    ): Boolean {
        if (isAccuracyTooBad(accuracy, radiusMeters)) {
            RepositoryDebugLogger.log(
                context,
                "GETEVENT_BLIND: BLOCK accuracy area=${area.id_area} acc=${"%.1f".format(accuracy)} " +
                        "absLimit=${"%.1f".format(accuracyAbsLimit(radiusMeters))} ratioLimit=${"%.1f".format(radiusMeters * 0.60f)}"
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

    /** Bloquea si la lectura cae en la “zona gris” cerca del borde del radio (evita rebotes por ruido). */
    private fun blockByGrayZone(
        context: Context,
        area: EntityAreaGeofence,
        radiusMeters: Float,
        accuracy: Float,
        distanceToBorder: Float
    ): Boolean {
        if (isInGrayZone(context, radiusMeters, accuracy, distanceToBorder)) {
            RepositoryDebugLogger.log(
                context,
                "GETEVENT_BLIND: BLOCK grayzone area=${area.id_area} distToBorder=${"%.1f".format(distanceToBorder)}"
            )
            return true
        }
        return false
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
                    // Estaba adentro: sólo disparo EXIT si se fue más allá de una distancia determinada
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

                else -> { }
            }
            return event1
        }
    }

    /** Si el candidato es CONTINUE, resetea streaks (debounce) y avisa al caller que debe retornar. */
    private suspend fun resetStreaksAndReturnIfContinue(
        area: EntityAreaGeofence,
        candidate: String
    ): Boolean {
        if (candidate == Definition.EVT_CONTINUE) {
            GeofenceTrackStore.resetStreaks(area.id_area)
            return true
        }
        return false
    }

    /** Bloquea si el evento se repite demasiado seguido (anti-spam). */
    private suspend fun blockBySpam(
        context: Context,
        area: EntityAreaGeofence,
        now: Long
    ): Boolean {
        val spamBlocked = GeofenceTrackStore.isSpamBlocked(area.id_area, now, MIN_EVENT_GAP_MS)
        if (spamBlocked) {
            RepositoryDebugLogger.log(context, "GETEVENT_BLIND: BLOCK spam area=${area.id_area}")
            return true
        }
        return false
    }

    private fun flipCooldownMs(isFast: Boolean, stationary: Boolean): Long {
        return when {
            stationary -> 8 * 60_000L   // quieto: súper duro (8 minuots)
            isFast     -> 5_000L       // auto: no frenes la salida/entrada real(10 segundos)
            else       -> 10_000L   // caminando: moderado (30 segundos)
        }
    }

    /** Bloquea por cooldown anti-flip dinámico, excepto si es un EXIT “muy afuera” (farOutside). */
    private suspend fun blockByFlipCooldown(
        context: Context,
        area: EntityAreaGeofence,
        now: Long,
        isFast: Boolean,
        stationary: Boolean,
        candidate: String,
        m: Metrics,
        meterForExit: Float
    ): Boolean {
        val minFlip = flipCooldownMs(isFast = isFast, stationary = stationary)

        val flipBlocked = GeofenceTrackStore.isFlipBlocked(area.id_area, now, minFlip)

        val farOutside = (candidate == Definition.EVT_EXIT) &&
                (m.distance >= (meterForExit + maxOf(m.accuracy * 1.5f, m.radiusMeters * 0.5f, 10f)))

        val lastFlipAt = GeofenceTrackStore.getLastFlipAt(area.id_area)

        if (flipBlocked && !farOutside) {
            RepositoryDebugLogger.log(
                context,
                "GETEVENT_BLIND: BLOCK flipCooldown area=${area.id_area} elapsed=${now - lastFlipAt}ms " +
                        "minFlip=$minFlip stationary=$stationary isFast=$isFast"
            )
            return true
        }
        return false
    }

    /** Si está quieto y el candidato es EXIT, exige una salida “clara” para evitar falsos positivos. */
    private suspend fun blockStationaryExitIfNotClear(
        context: Context,
        area: EntityAreaGeofence,
        stationary: Boolean,
        candidate: String,
        distance: Float,
        meterForExit: Float,
        accuracy: Float
    ): Boolean {
        if (stationary && candidate == Definition.EVT_EXIT) {
            val clearExitMargin = maxOf(accuracy, 5f)
            val clearExit = distance >= (meterForExit + clearExitMargin)

            if (!clearExit) {
                RepositoryDebugLogger.log(
                    context,
                    "GETEVENT_BLIND: BLOCK stationaryExit(notClear) area=${area.id_area} " +
                            "dist=${"%.1f".format(distance)} meterForExit=${"%.1f".format(meterForExit)} " +
                            "margin=${"%.1f".format(clearExitMargin)}"
                )
                GeofenceTrackStore.resetOutsideStreak(area.id_area)
                return true
            }
        }
        return false
    }

    /** Calcula confirmaciones requeridas para EXIT usando heurística anti-teleport . */
    private suspend fun computeRequiredExitConfirmAndLog(
        context: Context,
        area: EntityAreaGeofence,
        candidate: String,
        distance: Float,
        meterForExit: Float,
        accuracy: Float,
        radiusMeters: Float
    ): Int {
        val lastDist = GeofenceTrackStore.getLastDistToCenter(area.id_area)
        val jump = if (lastDist >= 0f) kotlin.math.abs(distance - lastDist) else 0f
        val overshoot = distance - meterForExit

        val suspiciousExit =
            candidate == Definition.EVT_EXIT && (
                    accuracy > radiusMeters * 0.35f ||
                            (lastDist >= 0f && jump > maxOf(accuracy * 2f, 25f))
                    )

        val strongExit =
            candidate == Definition.EVT_EXIT &&
                    overshoot >= maxOf(accuracy * 1.2f, 12f)

        val requiredExitConfirm = when {
            strongExit -> 1
            suspiciousExit -> 2
            else -> EXIT_CONFIRM_COUNT
        }

        if (candidate == Definition.EVT_EXIT && suspiciousExit && !strongExit) {
            RepositoryDebugLogger.log(
                context,
                "GETEVENT_BLIND: EXIT requires2 area=${area.id_area} " +
                        "acc=${"%.1f".format(accuracy)} jump=${"%.1f".format(jump)} overshoot=${"%.1f".format(overshoot)}"
            )
        }

        return requiredExitConfirm
    }

    /** Aplica debounce por lecturas consecutivas: incrementa streaks y decide si el candidato queda confirmado. */
    private suspend fun confirmByStreaks(
        area: EntityAreaGeofence,
        candidate: String,
        requiredExitConfirm: Int
    ): Boolean {
        return GeofenceTrackStore.confirmByStreaks(
            areaId = area.id_area,
            candidate = candidate,
            enterConfirmCount = ENTER_CONFIRM_COUNT,
            requiredExitConfirm = requiredExitConfirm
        )
    }

    /** Loguea el estado de espera cuando todavía no se alcanzó la confirmación por streaks. */
    private suspend fun logWaitConfirm(
        context: Context,
        area: EntityAreaGeofence,
        candidate: String
    ) {
        RepositoryDebugLogger.log(
            context,
            "GETEVENT_BLIND: WAIT confirm area=${area.id_area} cand=$candidate " +
                    "insideStreak=${GeofenceTrackStore.getInsideStreak(area.id_area)} " +
                    "outsideStreak=${GeofenceTrackStore.getOutsideStreak(area.id_area)}"
        )
    }

    /** Acepta el evento y actualiza el track (timestamps, lastDist, reseteo de streaks). */
    private suspend fun acceptEventAndUpdateTrack(
        area: EntityAreaGeofence,
        now: Long,
        distance: Float
    ) {
        GeofenceTrackStore.acceptEvent(area.id_area, now, distance)
    }

    /** Log final del evento aceptado (Repo logger + Logcat), con todas las métricas relevantes. */
    private fun logAcceptedEvent(
        context: Context,
        area: EntityAreaGeofence,
        prevState: String?,
        candidate: String,
        distance: Float,
        radiusMeters: Float,
        accuracy: Float,
        meterForEnter: Float,
        meterForExit: Float,
        distanceToBorder: Float,
        stationary: Boolean,
        isFast: Boolean,
        speed: Float
    ) {
        val msg =
            "GETEVENT_BLIND: área=${area.id_area}, prevState=$prevState, event=$candidate, " +
                    "dist=${"%.1f".format(distance)}m, radius=${"%.1f".format(radiusMeters)}m, " +
                    "acc=${"%.1f".format(accuracy)}m, enter=${"%.1f".format(meterForEnter)}m, " +
                    "exit=${"%.1f".format(meterForExit)}m, distToBorder=${"%.1f".format(distanceToBorder)}m, " +
                    "stationary=$stationary isFast=$isFast speed=${"%.2f".format(speed)}"

        RepositoryDebugLogger.log(context, msg)
        Log.d(Definition.TAG_DEBUG, msg)
    }
}
