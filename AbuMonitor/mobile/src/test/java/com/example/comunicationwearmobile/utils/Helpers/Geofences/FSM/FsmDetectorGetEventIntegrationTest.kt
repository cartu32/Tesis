package com.example.comunicationwearmobile.utils.Helpers.Geofences.FSM

import android.content.Context
import android.location.Location
import androidx.test.core.app.ApplicationProvider
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.comunicationwearmobile.ui.model.dto.AreaTrack
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.FSM.GeofenceEventFsmDetector
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.FSM.GeofenceTrackStore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Pruebas integrales de getEvent().
 *
 * Objetivo: probar la interacción completa:
 *
 * accuracy -> historial -> stationary -> gray-zone -> hysteresis ->
 * candidato -> anti-flip -> stationary EXIT -> anti-teleport ->
 * debounce -> acceptEvent.
 *
 * Los filtros individuales tienen sus propios tests.
 * Aquí se prueban combinaciones y secuencias completas pasando
 * directamente por getEvent().
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FsmDetectorGetEventIntegrationTest {

    private lateinit var context: Context
    private lateinit var area: EntityAreaGeofence

    private var nextAreaId = 900_000L

    @Before
    fun setUp() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        GeofenceTrackStore.clearTrackForTest()
        area = newArea(radius = 100)
    }

    private fun newArea(radius: Int = 100): EntityAreaGeofence {
        nextAreaId++

        return EntityAreaGeofence(
            id_area = nextAreaId,
            latitude = "0.0",
            longitude = "0.0",
            meters = radius
        )
    }

    /**
     * Crea una Location aproximadamente a distanceMeters del centro (0,0),
     * desplazándose sobre el ecuador.
     *
     * Location.distanceTo() calcula luego la distancia real utilizada
     * por producción.
     */
    private fun locationAt(
        distanceMeters: Float,
        accuracy: Float = 10f,
        speed: Float? = null
    ): Location {

        val metersPerDegreeAtEquator = 111_319.49

        return Location("test").apply {
            latitude = 0.0
            longitude = distanceMeters / metersPerDegreeAtEquator
            this.accuracy = accuracy

            if (speed != null) {
                this.speed = speed
            }
        }
    }

    private suspend fun event(
        state: String?,
        distance: Float,
        accuracy: Float = 10f,
        isFast: Boolean = false,
        speed: Float = 0f,
        areaToUse: EntityAreaGeofence = area
    ): String {

        return GeofenceEventFsmDetector.getEvent(
            context = context,
            area = areaToUse,
            location = locationAt(distance, accuracy, speed),
            prevState = state,
            isFast = isFast,
            speed = speed
        )
    }

    // ================================================================
    // Helpers para preparar estados temporales.
    //
    // Permiten evitar esperar realmente:
    // - 90 segundos para stationary
    // - 5/10 segundos para cooldown
    // - 8 minutos para stationary cooldown
    // ================================================================

    @Suppress("UNCHECKED_CAST")
    private fun rawTrackMap(): MutableMap<Long, AreaTrack> {

        val field = GeofenceTrackStore::class.java.getDeclaredField("track")

        field.isAccessible = true

        return field.get(GeofenceTrackStore) as MutableMap<Long, AreaTrack>
    }

    private fun mutateTrack(
        areaId: Long,
        block: (AreaTrack) -> Unit
    ) {

        val map = rawTrackMap()

        val track = map[areaId]
            ?: AreaTrack().also {
                map[areaId] = it
            }

        block(track)
    }

    private fun makeStationary(
        areaId: Long,
        distance: Float
    ) {

        val loc = locationAt(
            distance,
            accuracy = 5f,
            speed = 0f
        )

        val now = System.currentTimeMillis()

        mutateTrack(areaId) { t ->

            t.lastLocAt = now - 91_000L
            t.stationarySince = now - 91_000L

            t.lastLat = loc.latitude
            t.lastLon = loc.longitude

            t.stationaryAccumMove = 0f
        }
    }

    private fun setLastFlipAgo(
        areaId: Long,
        millisAgo: Long
    ) {

        mutateTrack(areaId) {
            it.lastFlipAt =
                System.currentTimeMillis() - millisAgo
        }
    }

    private fun setPreviousDistance(
        areaId: Long,
        distance: Float
    ) {

        mutateTrack(areaId) {
            it.previousLocationDistance = distance
        }
    }

    // ================================================================
    // A. ACCURACY
    // ================================================================

    @Test
    fun accuracyAboveAbsoluteLimit_blocksBeforeCandidate() = runBlocking {

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_INSIDE,
                160f,
                accuracy = 35.1f
            )
        )
    }

    @Test
    fun accuracyExactlyAbsoluteLimit_isAcceptedForLargeRadius() = runBlocking {

        // R = 100
        // ratioLimit = 60
        //
        // accuracy = 35
        //
        // No supera:
        // - ABSOLUTE_LIMIT = 35
        // - ratioLimit = 60

        assertEquals(
            Definition.EVT_EXIT,
            event(
                Definition.ST_INSIDE,
                170f,
                accuracy = 35f
            )
        )
    }

    @Test
    fun accuracyAboveRelativeLimit_blocksSmallRadius() = runBlocking {

        val small = newArea(radius = 25)

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_INSIDE,
                50f,
                accuracy = 15.1f,
                areaToUse = small
            )
        )
    }

    @Test
    fun accuracyExactlyRelativeLimit_smallRadius_isAccepted() = runBlocking {

        val small = newArea(radius = 25)

        /*
         * R = 25
         *
         * ratioLimit:
         *
         * 25 * 0.60 = 15
         *
         * accuracy = 15
         *
         * Como el código utiliza:
         *
         * accuracy > ratioLimit
         *
         * 15 > 15 = false
         *
         * Por lo tanto Accuracy acepta la lectura.
         *
         * Sin embargo:
         *
         * accuracy > 0.35 * R
         *
         * 15 > 8.75
         *
         * Anti-Teleport considera el EXIT sospechoso.
         *
         * Como no es strongExit necesita dos confirmaciones.
         */

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_INSIDE,
                50f,
                accuracy = 15f,
                areaToUse = small
            )
        )

        assertEquals(
            Definition.EVT_EXIT,
            event(
                Definition.ST_INSIDE,
                50f,
                accuracy = 15f,
                areaToUse = small
            )
        )
    }

    // ================================================================
    // B. GRAY ZONE + INIT
    // ================================================================

    @Test
    fun init_insideAwayFromBorder_returnsEnter() = runBlocking {

        assertEquals(
            Definition.EVT_ENTER,
            event(
                Definition.ST_INIT,
                50f,
                accuracy = 5f
            )
        )
    }

    @Test
    fun init_outsideAwayFromBorder_returnsExit() = runBlocking {

        assertEquals(
            Definition.EVT_EXIT,
            event(
                Definition.ST_INIT,
                150f,
                accuracy = 5f
            )
        )
    }

    @Test
    fun init_insideButInGrayZone_returnsContinue() = runBlocking {

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_INIT,
                98f,
                accuracy = 10f
            )
        )
    }

    @Test
    fun init_outsideButInGrayZone_returnsContinue() = runBlocking {

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_INIT,
                102f,
                accuracy = 10f
            )
        )
    }

    @Test
    fun exactlyCenter_isEnter() = runBlocking {

        assertEquals(
            Definition.EVT_ENTER,
            event(
                Definition.ST_INIT,
                0f,
                accuracy = 5f
            )
        )
    }

    // ================================================================
    // C. FSM + HYSTERESIS
    //
    // R = 100
    // accuracy = 10
    //
    // extraMargin = 10 / 100 = 0.10
    //
    // ENTER = 100 * (0.8 - 0.1) = 70
    // EXIT  = 100 * (1.2 + 0.1) = 130
    // ================================================================

    @Test
    fun inside_belowExitThreshold_returnsContinue() = runBlocking {

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_INSIDE,
                129f,
                accuracy = 10f
            )
        )
    }

    @Test
    fun inside_aboveExitThreshold_returnsExit() = runBlocking {

        assertEquals(
            Definition.EVT_EXIT,
            event(
                Definition.ST_INSIDE,
                131f,
                accuracy = 10f
            )
        )
    }

    @Test
    fun outside_aboveEnterThreshold_returnsContinue() = runBlocking {

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_OUTSIDE,
                71f,
                accuracy = 10f
            )
        )
    }

    @Test
    fun outside_belowEnterThreshold_returnsEnter() = runBlocking {

        assertEquals(
            Definition.EVT_ENTER,
            event(
                Definition.ST_OUTSIDE,
                69f,
                accuracy = 10f
            )
        )
    }

    @Test
    fun inside_deepInside_neverGeneratesEnter() = runBlocking {

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_INSIDE,
                20f,
                accuracy = 5f
            )
        )
    }

    @Test
    fun outside_farOutside_neverGeneratesExit() = runBlocking {

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_OUTSIDE,
                200f,
                accuracy = 5f
            )
        )
    }

    @Test
    fun nullState_returnsContinue() = runBlocking {

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                null,
                50f,
                accuracy = 5f
            )
        )
    }

    @Test
    fun unknownState_returnsContinue() = runBlocking {

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                "UNKNOWN",
                50f,
                accuracy = 5f
            )
        )
    }

    // ================================================================
    // D. ANTI-FLIP
    //
    // stationary / walking / fast + farOutside
    // ================================================================

    @Test
    fun walking_recentFlip_blocksExit() = runBlocking {

        setLastFlipAgo(
            area.id_area,
            1_000L
        )

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_INSIDE,
                140f,
                accuracy = 10f,
                isFast = false,
                speed = 1f
            )
        )
    }

    @Test
    fun fast_recentFlip_blocksExitInsideFiveSeconds() = runBlocking {

        setLastFlipAgo(
            area.id_area,
            1_000L
        )

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_INSIDE,
                140f,
                accuracy = 10f,
                isFast = true,
                speed = 10f
            )
        )
    }

    @Test
    fun walking_expiredFlip_allowsExit() = runBlocking {

        setLastFlipAgo(
            area.id_area,
            11_000L
        )

        assertEquals(
            Definition.EVT_EXIT,
            event(
                Definition.ST_INSIDE,
                140f,
                accuracy = 10f,
                isFast = false,
                speed = 1f
            )
        )
    }

    @Test
    fun fast_expiredFlip_allowsExit() = runBlocking {

        setLastFlipAgo(
            area.id_area,
            6_000L
        )

        assertEquals(
            Definition.EVT_EXIT,
            event(
                Definition.ST_INSIDE,
                140f,
                accuracy = 10f,
                isFast = true,
                speed = 10f
            )
        )
    }

    @Test
    fun farOutside_bypassesActiveCooldown() = runBlocking {

        /*
         * R = 100
         * accuracy = 10
         *
         * EXIT = 130
         *
         * farOutside:
         *
         * 130 + max(15, 50, 10)
         * = 180
         */

        setLastFlipAgo(
            area.id_area,
            1_000L
        )

        assertEquals(
            Definition.EVT_EXIT,
            event(
                Definition.ST_INSIDE,
                181f,
                accuracy = 10f,
                isFast = false,
                speed = 1f
            )
        )
    }

    @Test
    fun notFarOutside_doesNotBypassActiveCooldown() = runBlocking {

        setLastFlipAgo(
            area.id_area,
            1_000L
        )

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_INSIDE,
                179f,
                accuracy = 10f,
                isFast = false,
                speed = 1f
            )
        )
    }

    @Test
    fun farOutsideException_appliesOnlyToExit_notEnter() = runBlocking {

        setLastFlipAgo(
            area.id_area,
            1_000L
        )

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_OUTSIDE,
                20f,
                accuracy = 5f,
                isFast = false,
                speed = 1f
            )
        )
    }

    // ================================================================
    // E. STATIONARY EXIT
    // ================================================================

    @Test
    fun stationary_exitNotClear_isBlocked() = runBlocking {

        /*
         * accuracy = 5
         *
         * EXIT = 125
         *
         * clearExitMargin:
         *
         * max(5, 5) = 5
         *
         * clear EXIT desde:
         *
         * 125 + 5 = 130
         */

        makeStationary(
            area.id_area,
            127f
        )

        setLastFlipAgo(
            area.id_area,
            9 * 60_000L
        )

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_INSIDE,
                127f,
                accuracy = 5f,
                speed = 0f
            )
        )
    }

    @Test
    fun stationary_clearExit_isAcceptedWhenCooldownExpired() = runBlocking {

        makeStationary(
            area.id_area,
            131f
        )

        setLastFlipAgo(
            area.id_area,
            9 * 60_000L
        )

        assertEquals(
            Definition.EVT_EXIT,
            event(
                Definition.ST_INSIDE,
                131f,
                accuracy = 5f,
                speed = 0f
            )
        )
    }

    @Test
    fun stationary_hasPriorityOverIsFast_forCooldown() = runBlocking {

        /*
         * 20 segundos serían suficientes para:
         *
         * fast    = 5 segundos
         * walking = 10 segundos
         *
         * Pero NO para stationary:
         *
         * stationary = 8 minutos
         */

        makeStationary(
            area.id_area,
            140f
        )

        setLastFlipAgo(
            area.id_area,
            20_000L
        )

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_INSIDE,
                140f,
                accuracy = 5f,
                isFast = true,
                speed = 0f
            )
        )
    }

    // ================================================================
    // F. ANTI-TELEPORT + DEBOUNCE
    // ================================================================

    @Test
    fun suspiciousExitByJump_firstDetection_returnsContinue() = runBlocking {

        /*
         * R = 100
         * accuracy = 10
         * EXIT = 130
         *
         * previous = 90
         * current  = 135
         *
         * jump = 45
         *
         * max(accuracy * 2, 25)
         * = max(20, 25)
         * = 25
         *
         * 45 > 25
         *
         * suspiciousExit = true
         *
         * overshoot:
         *
         * 135 - 130 = 5
         *
         * strong threshold = 12
         *
         * 5 < 12
         *
         * No es strong.
         *
         * Requiere 2 confirmaciones.
         */

        setPreviousDistance(
            area.id_area,
            90f
        )

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_INSIDE,
                135f,
                accuracy = 10f
            )
        )

        assertEquals(
            1,
            GeofenceTrackStore.getOutsideStreak(area.id_area)
        )
    }

    @Test
    fun suspiciousExit_secondConsecutiveDetection_isAccepted() = runBlocking {

        setPreviousDistance(
            area.id_area,
            90f
        )

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_INSIDE,
                135f,
                accuracy = 10f
            )
        )

        /*
         * Segunda lectura:
         *
         * previous = 135
         * current = 136
         *
         * Ya no existe salto sospechoso.
         *
         * El outsideStreak existente permite confirmar EXIT.
         */

        assertEquals(
            Definition.EVT_EXIT,
            event(
                Definition.ST_INSIDE,
                136f,
                accuracy = 10f
            )
        )
    }

    @Test
    fun suspiciousExit_interruptedByContinue_resetsStreak() = runBlocking {

        setPreviousDistance(
            area.id_area,
            90f
        )

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_INSIDE,
                135f,
                accuracy = 10f
            )
        )

        assertEquals(
            1,
            GeofenceTrackStore.getOutsideStreak(area.id_area)
        )

        /*
         * No hay candidato EXIT.
         *
         * determineCandidateEvent()
         * debe resetear streaks.
         */

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_INSIDE,
                110f,
                accuracy = 10f
            )
        )

        assertEquals(
            0,
            GeofenceTrackStore.getOutsideStreak(area.id_area)
        )

        /*
         * Generamos nuevamente un salto sospechoso.
         *
         * Debe volver a considerarse primera confirmación.
         */

        setPreviousDistance(
            area.id_area,
            90f
        )

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_INSIDE,
                135f,
                accuracy = 10f
            )
        )

        assertEquals(
            1,
            GeofenceTrackStore.getOutsideStreak(area.id_area)
        )
    }

    @Test
    fun strongExit_overridesSuspicious_andIsAcceptedImmediately() = runBlocking {

        /*
         * previous = 80
         * current = 150
         *
         * Existe salto sospechoso.
         *
         * EXIT = 130
         *
         * overshoot:
         *
         * 150 - 130 = 20
         *
         * strong threshold:
         *
         * max(10 * 1.2, 12)
         * = 12
         *
         * 20 >= 12
         *
         * strongExit = true
         *
         * Por lo tanto requiere una sola confirmación.
         */

        setPreviousDistance(
            area.id_area,
            80f
        )

        assertEquals(
            Definition.EVT_EXIT,
            event(
                Definition.ST_INSIDE,
                150f,
                accuracy = 10f
            )
        )
    }

    @Test
    fun suspiciousByAccuracyRatio_interactionWithAccuracyFilter() = runBlocking {

        /*
         * Este test documenta una interacción importante.
         *
         * Para R = 300:
         *
         * accuracy = 34 es válida.
         *
         * Anti-Teleport:
         *
         * 0.35 * R = 105
         *
         * 34 > 105 = false
         *
         * Por lo tanto NO es suspiciousExit por accuracy.
         */

        val large = newArea(radius = 300)

        assertEquals(
            Definition.EVT_EXIT,
            event(
                Definition.ST_INSIDE,
                400f,
                accuracy = 34f,
                areaToUse = large
            )
        )
    }

    // ================================================================
    // G. ENTER + DEBOUNCE + STREAKS OPUESTOS
    // ================================================================

    @Test
    fun enter_requiresOneConfirmation() = runBlocking {

        assertEquals(
            Definition.EVT_ENTER,
            event(
                Definition.ST_OUTSIDE,
                50f,
                accuracy = 10f
            )
        )
    }

    @Test
    fun enterResetsPreviousOutsideStreak() = runBlocking {

        /*
         * Primero generamos EXIT sospechoso.
         *
         * Queda:
         *
         * outsideStreak = 1
         */

        setPreviousDistance(
            area.id_area,
            90f
        )

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_INSIDE,
                135f,
                accuracy = 10f
            )
        )

        assertEquals(
            1,
            GeofenceTrackStore.getOutsideStreak(area.id_area)
        )

        /*
         * Ahora ENTER.
         *
         * confirmByStreaks debe:
         *
         * insideStreak++
         * outsideStreak = 0
         */

        assertEquals(
            Definition.EVT_ENTER,
            event(
                Definition.ST_OUTSIDE,
                50f,
                accuracy = 10f
            )
        )

        assertEquals(
            0,
            GeofenceTrackStore.getOutsideStreak(area.id_area)
        )
    }

    // ================================================================
    // H. SECUENCIAS REALES COMPLETAS
    // ================================================================

    @Test
    fun normalSequence_initInside_inside_exit() = runBlocking {

        assertEquals(
            Definition.EVT_ENTER,
            event(
                Definition.ST_INIT,
                40f,
                accuracy = 5f
            )
        )

        /*
         * Simula que ya pasó el cooldown desde
         * el ENTER aceptado.
         */

        setLastFlipAgo(
            area.id_area,
            11_000L
        )

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_INSIDE,
                80f,
                accuracy = 5f
            )
        )

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_INSIDE,
                110f,
                accuracy = 5f
            )
        )

        assertEquals(
            Definition.EVT_EXIT,
            event(
                Definition.ST_INSIDE,
                140f,
                accuracy = 5f
            )
        )
    }

    @Test
    fun normalSequence_initOutside_outside_enter() = runBlocking {

        assertEquals(
            Definition.EVT_EXIT,
            event(
                Definition.ST_INIT,
                160f,
                accuracy = 5f
            )
        )

        setLastFlipAgo(
            area.id_area,
            11_000L
        )

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_OUTSIDE,
                120f,
                accuracy = 5f
            )
        )

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_OUTSIDE,
                90f,
                accuracy = 5f
            )
        )

        assertEquals(
            Definition.EVT_ENTER,
            event(
                Definition.ST_OUTSIDE,
                60f,
                accuracy = 5f
            )
        )
    }

    @Test
    fun grayZoneReading_doesNotProduceEvent_thenClearExitNeedsConfirmation() = runBlocking {

        /*
         * Primera lectura:
         *
         * distance = 102
         *
         * Está en Gray Zone.
         *
         * No genera evento, pero previousDistance
         * queda actualizado a 102.
         */

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_INSIDE,
                102f,
                accuracy = 10f
            )
        )

        /*
         * Segunda lectura:
         *
         * previous = 102
         * current = 140
         *
         * jump = 38
         *
         * max(2 * accuracy, 25)
         * = max(20,25)
         * = 25
         *
         * 38 > 25
         *
         * Por lo tanto Anti-Teleport considera
         * el EXIT sospechoso.
         *
         * Primera confirmación -> CONTINUE.
         */

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_INSIDE,
                140f,
                accuracy = 10f
            )
        )

        assertEquals(
            1,
            GeofenceTrackStore.getOutsideStreak(area.id_area)
        )

        /*
         * Tercera lectura:
         *
         * previous = 140
         * current = 140
         *
         * No existe salto.
         *
         * Es la segunda confirmación consecutiva
         * del EXIT.
         */

        assertEquals(
            Definition.EVT_EXIT,
            event(
                Definition.ST_INSIDE,
                140f,
                accuracy = 10f
            )
        )
    }

    @Test
    fun badAccuracy_doesNotOverwritePreviousDistance() = runBlocking {

        /*
         * Cargamos previousDistance válido.
         */

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_INSIDE,
                90f,
                accuracy = 10f
            )
        )

        /*
         * Lectura inválida.
         *
         * blockByAccuracy ocurre ANTES de actualizar
         * previousDistance.
         */

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_INSIDE,
                200f,
                accuracy = 40f
            )
        )

        /*
         * previousDistance debe continuar siendo 90.
         *
         * 90 -> 135
         *
         * jump = 45
         *
         * Anti-Teleport debe exigir confirmación.
         */

        assertEquals(
            Definition.EVT_CONTINUE,
            event(
                Definition.ST_INSIDE,
                135f,
                accuracy = 10f
            )
        )

        assertEquals(
            1,
            GeofenceTrackStore.getOutsideStreak(area.id_area)
        )
    }

    // ================================================================
    // I. RADIOS DEL RANGO REAL DE LA APP
    // ================================================================

    @Test
    fun minimumConfiguredRadius30_supportsEnterAndExit() = runBlocking {

        val enterArea = newArea(radius = 30)

        assertEquals(
            Definition.EVT_ENTER,
            event(
                Definition.ST_OUTSIDE,
                10f,
                accuracy = 5f,
                areaToUse = enterArea
            )
        )

        GeofenceTrackStore.clearTrackForTest()

        val exitArea = newArea(radius = 30)

        assertEquals(
            Definition.EVT_EXIT,
            event(
                Definition.ST_INSIDE,
                50f,
                accuracy = 5f,
                areaToUse = exitArea
            )
        )
    }

    @Test
    fun maximumConfiguredRadius300_supportsEnterAndExit() = runBlocking {

        val enterArea = newArea(radius = 300)

        assertEquals(
            Definition.EVT_ENTER,
            event(
                Definition.ST_OUTSIDE,
                200f,
                accuracy = 10f,
                areaToUse = enterArea
            )
        )

        GeofenceTrackStore.clearTrackForTest()

        val exitArea = newArea(radius = 300)

        assertEquals(
            Definition.EVT_EXIT,
            event(
                Definition.ST_INSIDE,
                400f,
                accuracy = 10f,
                areaToUse = exitArea
            )
        )
    }
}