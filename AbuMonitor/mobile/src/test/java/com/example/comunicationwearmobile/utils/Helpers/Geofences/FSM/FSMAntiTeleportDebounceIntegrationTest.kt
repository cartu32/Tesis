package com.example.comunicationwearmobile.utils.Helpers.Geofences.FSM

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.FSM.GeofenceEventFsmDetector
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.FSM.GeofenceTrackStore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FSMAntiTeleportDebounceIntegrationTest {

    private lateinit var context: Context
    private lateinit var area: EntityAreaGeofence

    private val areaId = 700001L

    @Before
    fun setUp() = runBlocking {
        context = ApplicationProvider.getApplicationContext()

        GeofenceTrackStore.clearTrackForTest()

        area = EntityAreaGeofence(
            id_area = areaId,
            latitude = "0",
            longitude = "0",
            meters = 100
        )
    }

    /**
     * Reproduce la combinación:
     *
     * previousDistance
     *      ↓
     * Anti-Teleport
     *      ↓
     * requiredExitConfirmCount
     *      ↓
     * Debounce
     *
     * Devuelve:
     * Pair(requiredConfirmations, confirmed)
     */
    private suspend fun processExit(
        distance: Float,
        meterForExit: Float = 120f,
        accuracy: Float = 10f,
        radius: Float = 100f
    ): Pair<Int, Boolean> {

        val previousDistance =
            GeofenceTrackStore.getAndUpdatePreviousLocationDistance(
                area.id_area,
                distance
            )

        val required =
            GeofenceEventFsmDetector.computeRequiredExitConfirmAndLog(
                context = context,
                area = area,
                candidate = Definition.EVT_EXIT,
                distance = distance,
                meterForExit = meterForExit,
                accuracy = accuracy,
                radiusMeters = radius,
                previousDistance = previousDistance
            )

        val confirmed =
            GeofenceTrackStore.confirmByStreaks(
                areaId = area.id_area,
                candidate = Definition.EVT_EXIT,
                requiredEnterConfirmCount = 1,
                requiredExitConfirmCount = required
            )

        return Pair(required, confirmed)
    }

    /**
     * Permite cargar una distancia anterior sin modificar los streaks.
     */
    private suspend fun setPreviousDistance(distance: Float) {
        GeofenceTrackStore.getAndUpdatePreviousLocationDistance(
            area.id_area,
            distance
        )
    }

    // ============================================================
    // 1. EXIT NORMAL
    // ============================================================

    @Test
    fun normalExit_requiresOne_andIsImmediatelyConfirmed() = runBlocking {

        setPreviousDistance(120f)

        val result = processExit(
            distance = 125f,
            accuracy = 10f
        )

        assertEquals(1, result.first)
        assertTrue(result.second)

        assertEquals(
            1,
            GeofenceTrackStore.getOutsideStreak(area.id_area)
        )
    }

    // ============================================================
    // 2. PRIMER EXIT SOSPECHOSO POR JUMP
    //
    // previous = 90
    // current  = 125
    // jump = 35
    //
    // threshold = max(2*10,25) = 25
    //
    // 35 > 25 -> suspicious
    // required = 2
    // ============================================================

    @Test
    fun suspiciousJump_firstExit_requiresTwo_andIsRejected() =
        runBlocking {

            setPreviousDistance(90f)

            val result = processExit(
                distance = 125f,
                accuracy = 10f
            )

            assertEquals(2, result.first)
            assertFalse(result.second)

            assertEquals(
                1,
                GeofenceTrackStore.getOutsideStreak(area.id_area)
            )
        }

    // ============================================================
    // 3. SOSPECHOSO -> NORMAL
    //
    // Primera:
    // 90 -> 125 = jump 35
    // required=2
    // streak=1
    // FALSE
    //
    // Segunda:
    // 125 -> 126 = jump 1
    // required=1
    // streak=2
    // TRUE
    // ============================================================

    @Test
    fun suspiciousExit_thenNormalExit_secondIsConfirmed() =
        runBlocking {

            setPreviousDistance(90f)

            val first = processExit(
                distance = 125f,
                accuracy = 10f
            )

            assertEquals(2, first.first)
            assertFalse(first.second)

            assertEquals(
                1,
                GeofenceTrackStore.getOutsideStreak(area.id_area)
            )

            val second = processExit(
                distance = 126f,
                accuracy = 10f
            )

            assertEquals(1, second.first)
            assertTrue(second.second)

            assertEquals(
                2,
                GeofenceTrackStore.getOutsideStreak(area.id_area)
            )
        }

    // ============================================================
    // 4. DOS EXIT SOSPECHOSOS CONSECUTIVOS
    //
    // Ambos requieren 2.
    // El primero no confirma.
    // El segundo sí confirma.
    // ============================================================

    @Test
    fun suspiciousExit_thenSuspiciousExit_secondIsConfirmed() =
        runBlocking {

            setPreviousDistance(80f)

            val first = processExit(
                distance = 110f,
                meterForExit = 100f,
                accuracy = 10f
            )

            assertEquals(2, first.first)
            assertFalse(first.second)

            // 110 -> 140 = jump 30.
            //
            // Evitamos que sea strongExit aumentando meterForExit.
            val second = processExit(
                distance = 140f,
                meterForExit = 135f,
                accuracy = 10f
            )

            assertEquals(2, second.first)
            assertTrue(second.second)

            assertEquals(
                2,
                GeofenceTrackStore.getOutsideStreak(area.id_area)
            )
        }

    // ============================================================
    // 5. SOSPECHOSO -> STRONG EXIT
    //
    // StrongExit tiene prioridad y requiere 1.
    // Como ya existe un streak anterior, confirma.
    // ============================================================

    @Test
    fun suspiciousExit_thenStrongExit_isConfirmed() =
        runBlocking {

            setPreviousDistance(90f)

            val first = processExit(
                distance = 125f,
                meterForExit = 120f,
                accuracy = 10f
            )

            assertEquals(2, first.first)
            assertFalse(first.second)

            val second = processExit(
                distance = 140f,
                meterForExit = 120f,
                accuracy = 10f
            )

            // overshoot = 20
            // strong threshold = max(12,12) = 12
            assertEquals(1, second.first)
            assertTrue(second.second)

            assertEquals(
                2,
                GeofenceTrackStore.getOutsideStreak(area.id_area)
            )
        }

    // ============================================================
    // 6. PRIMER EVENTO STRONG EXIT
    //
    // No necesita una segunda lectura.
    // ============================================================

    @Test
    fun strongExit_firstExit_isImmediatelyConfirmed() =
        runBlocking {

            setPreviousDistance(130f)

            val result = processExit(
                distance = 140f,
                meterForExit = 120f,
                accuracy = 10f
            )

            assertEquals(1, result.first)
            assertTrue(result.second)

            assertEquals(
                1,
                GeofenceTrackStore.getOutsideStreak(area.id_area)
            )
        }

    // ============================================================
    // 7. SUSPICIOUS POR ACCURACY
    //
    // Usamos R=50:
    // 35% R = 17.5
    // accuracy=20 -> suspicious
    //
    // Además este valor todavía puede superar el filtro de
    // accuracy del sistema completo:
    // 60% de 50 = 30.
    // ============================================================

    @Test
    fun suspiciousByAccuracy_requiresTwo() = runBlocking {

        setPreviousDistance(62f)

        val result = processExit(
            distance = 65f,
            meterForExit = 60f,
            accuracy = 20f,
            radius = 50f
        )

        assertEquals(2, result.first)
        assertFalse(result.second)

        assertEquals(
            1,
            GeofenceTrackStore.getOutsideStreak(area.id_area)
        )
    }

    // ============================================================
    // 8. ACCURACY SOSPECHOSO -> SEGUNDA LECTURA NORMAL
    // ============================================================

    @Test
    fun suspiciousAccuracy_thenNormalExit_isConfirmed() =
        runBlocking {

            setPreviousDistance(62f)

            val first = processExit(
                distance = 65f,
                meterForExit = 60f,
                accuracy = 20f,
                radius = 50f
            )

            assertEquals(2, first.first)
            assertFalse(first.second)

            val second = processExit(
                distance = 66f,
                meterForExit = 60f,
                accuracy = 10f,
                radius = 50f
            )

            assertEquals(1, second.first)
            assertTrue(second.second)

            assertEquals(
                2,
                GeofenceTrackStore.getOutsideStreak(area.id_area)
            )
        }

    // ============================================================
    // 9. EXACTAMENTE EN EL LIMITE DEL JUMP
    //
    // accuracy=10
    // threshold=max(20,25)=25
    //
    // jump == 25 NO es suspicious porque código usa >
    // ============================================================

    @Test
    fun jumpExactlyThreshold_requiresOne() = runBlocking {

        setPreviousDistance(100f)

        val result = processExit(
            distance = 125f,
            accuracy = 10f
        )

        assertEquals(1, result.first)
        assertTrue(result.second)
    }

    // ============================================================
    // 10. APENAS POR ENCIMA DEL LIMITE DEL JUMP
    // ============================================================

    @Test
    fun jumpJustAboveThreshold_requiresTwo() = runBlocking {

        setPreviousDistance(99.9f)

        val result = processExit(
            distance = 125f,
            accuracy = 10f
        )

        assertEquals(2, result.first)
        assertFalse(result.second)
    }

    // ============================================================
    // 11. STRONG EXACTAMENTE EN EL LIMITE
    //
    // accuracy=10
    // strong threshold=max(12,12)=12
    //
    // meterForExit=120
    // distance=132
    //
    // overshoot=12
    //
    // strong usa >=
    // ============================================================

    @Test
    fun strongExit_exactThreshold_requiresOne() = runBlocking {

        setPreviousDistance(130f)

        val result = processExit(
            distance = 132f,
            meterForExit = 120f,
            accuracy = 10f
        )

        assertEquals(1, result.first)
        assertTrue(result.second)
    }

    // ============================================================
    // 12. SUSPICIOUS + STRONG
    //
    // Strong tiene prioridad.
    // ============================================================

    @Test
    fun suspiciousAndStrong_strongWins_requiresOne() =
        runBlocking {

            setPreviousDistance(90f)

            val result = processExit(
                distance = 140f,
                meterForExit = 120f,
                accuracy = 10f
            )

            // jump=50 -> suspicious
            // overshoot=20 -> strong
            //
            // strong debe ganar.
            assertEquals(1, result.first)
            assertTrue(result.second)
        }

    // ============================================================
    // 13. EXIT SOSPECHOSO -> ENTER -> EXIT
    //
    // ENTER resetea outsideStreak.
    // El siguiente EXIT vuelve a ser la primera confirmación.
    // ============================================================

    @Test
    fun suspiciousExit_enter_exit_doesNotConfirmSecondExit() =
        runBlocking {

            setPreviousDistance(90f)

            val firstExit = processExit(
                distance = 125f,
                accuracy = 10f
            )

            assertEquals(2, firstExit.first)
            assertFalse(firstExit.second)

            assertEquals(
                1,
                GeofenceTrackStore.getOutsideStreak(area.id_area)
            )

            val enter =
                GeofenceTrackStore.confirmByStreaks(
                    areaId = area.id_area,
                    candidate = Definition.EVT_ENTER,
                    requiredEnterConfirmCount = 1,
                    requiredExitConfirmCount = 2
                )

            assertTrue(enter)

            assertEquals(
                0,
                GeofenceTrackStore.getOutsideStreak(area.id_area)
            )

            /*
             * Preparamos nuevamente un jump sospechoso.
             */
            setPreviousDistance(90f)

            val secondExit = processExit(
                distance = 125f,
                accuracy = 10f
            )

            assertEquals(2, secondExit.first)
            assertFalse(secondExit.second)

            assertEquals(
                1,
                GeofenceTrackStore.getOutsideStreak(area.id_area)
            )
        }

    // ============================================================
    // 14. EXIT SOSPECHOSO -> CONTINUE -> EXIT
    //
    // En getEvent(), CONTINUE llama resetStreaks().
    // Acá reproducimos ese comportamiento explícitamente.
    // ============================================================

    @Test
    fun suspiciousExit_continue_exit_doesNotConfirmSecondExit() =
        runBlocking {

            setPreviousDistance(90f)

            val firstExit = processExit(
                distance = 125f,
                accuracy = 10f
            )

            assertEquals(2, firstExit.first)
            assertFalse(firstExit.second)

            assertEquals(
                1,
                GeofenceTrackStore.getOutsideStreak(area.id_area)
            )

            // Reproduce resetStreaksAndReturnIfContinue().
            GeofenceTrackStore.resetStreaks(area.id_area)

            assertEquals(
                0,
                GeofenceTrackStore.getOutsideStreak(area.id_area)
            )

            /*
             * Preparamos nuevamente una lectura sospechosa.
             */
            setPreviousDistance(90f)

            val secondExit = processExit(
                distance = 125f,
                accuracy = 10f
            )

            assertEquals(2, secondExit.first)
            assertFalse(secondExit.second)

            assertEquals(
                1,
                GeofenceTrackStore.getOutsideStreak(area.id_area)
            )
        }

    // ============================================================
    // 15. TRES EXIT SOSPECHOSOS
    //
    // 1 -> false
    // 2 -> true
    // 3 -> true
    // ============================================================

    @Test
    fun threeSuspiciousExits_firstRejected_restConfirmed() =
        runBlocking {

            setPreviousDistance(80f)

            val first = processExit(
                distance = 110f,
                meterForExit = 105f,
                accuracy = 10f
            )

            assertEquals(2, first.first)
            assertFalse(first.second)

            val second = processExit(
                distance = 140f,
                meterForExit = 135f,
                accuracy = 10f
            )

            assertEquals(2, second.first)
            assertTrue(second.second)

            val third = processExit(
                distance = 170f,
                meterForExit = 165f,
                accuracy = 10f
            )

            assertEquals(2, third.first)
            assertTrue(third.second)

            assertEquals(
                3,
                GeofenceTrackStore.getOutsideStreak(area.id_area)
            )
        }

    // ============================================================
    // 16. DOS AREAS INDEPENDIENTES
    // ============================================================

    @Test
    fun antiTeleportDebounce_differentAreas_areIndependent() =
        runBlocking {

            val secondArea = EntityAreaGeofence(
                id_area = 700002L,
                latitude = "0",
                longitude = "0",
                meters = 100
            )

            // AREA 1
            GeofenceTrackStore.getAndUpdatePreviousLocationDistance(
                area.id_area,
                90f
            )

            val previous1 =
                GeofenceTrackStore.getAndUpdatePreviousLocationDistance(
                    area.id_area,
                    125f
                )

            val required1 =
                GeofenceEventFsmDetector.computeRequiredExitConfirmAndLog(
                    context = context,
                    area = area,
                    candidate = Definition.EVT_EXIT,
                    distance = 125f,
                    meterForExit = 120f,
                    accuracy = 10f,
                    radiusMeters = 100f,
                    previousDistance = previous1
                )

            val confirmed1 =
                GeofenceTrackStore.confirmByStreaks(
                    area.id_area,
                    Definition.EVT_EXIT,
                    1,
                    required1
                )

            // AREA 2
            GeofenceTrackStore.getAndUpdatePreviousLocationDistance(
                secondArea.id_area,
                120f
            )

            val previous2 =
                GeofenceTrackStore.getAndUpdatePreviousLocationDistance(
                    secondArea.id_area,
                    125f
                )

            val required2 =
                GeofenceEventFsmDetector.computeRequiredExitConfirmAndLog(
                    context = context,
                    area = secondArea,
                    candidate = Definition.EVT_EXIT,
                    distance = 125f,
                    meterForExit = 120f,
                    accuracy = 10f,
                    radiusMeters = 100f,
                    previousDistance = previous2
                )

            val confirmed2 =
                GeofenceTrackStore.confirmByStreaks(
                    secondArea.id_area,
                    Definition.EVT_EXIT,
                    1,
                    required2
                )

            assertEquals(2, required1)
            assertFalse(confirmed1)

            assertEquals(1, required2)
            assertTrue(confirmed2)

            assertEquals(
                1,
                GeofenceTrackStore.getOutsideStreak(area.id_area)
            )

            assertEquals(
                1,
                GeofenceTrackStore.getOutsideStreak(secondArea.id_area)
            )
        }
}
