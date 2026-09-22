package com.example.comunicationwearmobile.utils.Helpers.Geofences.FSM

import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.FSM.GeofenceTrackStore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FSMDebounceFilterTest {

    private val areaId = 500001L
    private val areaId2 = 500002L

    @Before
    fun setUp() = runBlocking {
        GeofenceTrackStore.clearTrackForTest()
    }

    // ============================================================
    // PREVIOUS LOCATION DISTANCE
    // ============================================================

    @Test
    fun previousDistance_firstCall_returnsInitialValue() = runBlocking {

        val previous =
            GeofenceTrackStore.getAndUpdatePreviousLocationDistance(
                areaId,
                100f
            )

        assertEquals(-1f, previous, 0.001f)
    }

    @Test
    fun previousDistance_secondCall_returnsFirstDistance() = runBlocking {

        GeofenceTrackStore.getAndUpdatePreviousLocationDistance(
            areaId,
            100f
        )

        val previous =
            GeofenceTrackStore.getAndUpdatePreviousLocationDistance(
                areaId,
                110f
            )

        assertEquals(100f, previous, 0.001f)
    }

    @Test
    fun previousDistance_threeCalls_alwaysReturnsImmediatelyPrevious() =
        runBlocking {

            GeofenceTrackStore.getAndUpdatePreviousLocationDistance(
                areaId,
                100f
            )

            val second =
                GeofenceTrackStore.getAndUpdatePreviousLocationDistance(
                    areaId,
                    110f
                )

            val third =
                GeofenceTrackStore.getAndUpdatePreviousLocationDistance(
                    areaId,
                    150f
                )

            assertEquals(100f, second, 0.001f)
            assertEquals(110f, third, 0.001f)
        }

    @Test
    fun previousDistance_sameDistance_isPreserved() = runBlocking {

        GeofenceTrackStore.getAndUpdatePreviousLocationDistance(
            areaId,
            100f
        )

        val previous =
            GeofenceTrackStore.getAndUpdatePreviousLocationDistance(
                areaId,
                100f
            )

        assertEquals(100f, previous, 0.001f)
    }

    @Test
    fun previousDistance_decreasingDistance_works() = runBlocking {

        GeofenceTrackStore.getAndUpdatePreviousLocationDistance(
            areaId,
            150f
        )

        val previous =
            GeofenceTrackStore.getAndUpdatePreviousLocationDistance(
                areaId,
                100f
            )

        assertEquals(150f, previous, 0.001f)
    }

    @Test
    fun previousDistance_differentAreas_areIndependent() = runBlocking {

        GeofenceTrackStore.getAndUpdatePreviousLocationDistance(
            areaId,
            50f
        )

        GeofenceTrackStore.getAndUpdatePreviousLocationDistance(
            areaId2,
            200f
        )

        val previous1 =
            GeofenceTrackStore.getAndUpdatePreviousLocationDistance(
                areaId,
                60f
            )

        val previous2 =
            GeofenceTrackStore.getAndUpdatePreviousLocationDistance(
                areaId2,
                210f
            )

        assertEquals(50f, previous1, 0.001f)
        assertEquals(200f, previous2, 0.001f)
    }

    // ============================================================
    // EXIT - UNA CONFIRMACION
    // ============================================================

    @Test
    fun exit_requiredOne_firstExitIsConfirmed() = runBlocking {

        val result =
            GeofenceTrackStore.confirmByStreaks(
                areaId = areaId,
                candidate = Definition.EVT_EXIT,
                requiredEnterConfirmCount = 1,
                requiredExitConfirmCount = 1
            )

        assertTrue(result)

        assertEquals(
            1,
            GeofenceTrackStore.getOutsideStreak(areaId)
        )

        assertEquals(
            0,
            GeofenceTrackStore.getInsideStreak(areaId)
        )
    }

    // ============================================================
    // EXIT - DOS CONFIRMACIONES
    // ============================================================

    @Test
    fun exit_requiredTwo_firstExitIsNotConfirmed() = runBlocking {

        val result =
            GeofenceTrackStore.confirmByStreaks(
                areaId = areaId,
                candidate = Definition.EVT_EXIT,
                requiredEnterConfirmCount = 1,
                requiredExitConfirmCount = 2
            )

        assertFalse(result)

        assertEquals(
            1,
            GeofenceTrackStore.getOutsideStreak(areaId)
        )

        assertEquals(
            0,
            GeofenceTrackStore.getInsideStreak(areaId)
        )
    }

    @Test
    fun exit_requiredTwo_secondConsecutiveExitIsConfirmed() =
        runBlocking {

            val first =
                GeofenceTrackStore.confirmByStreaks(
                    areaId = areaId,
                    candidate = Definition.EVT_EXIT,
                    requiredEnterConfirmCount = 1,
                    requiredExitConfirmCount = 2
                )

            val second =
                GeofenceTrackStore.confirmByStreaks(
                    areaId = areaId,
                    candidate = Definition.EVT_EXIT,
                    requiredEnterConfirmCount = 1,
                    requiredExitConfirmCount = 2
                )

            assertFalse(first)
            assertTrue(second)

            assertEquals(
                2,
                GeofenceTrackStore.getOutsideStreak(areaId)
            )

            assertEquals(
                0,
                GeofenceTrackStore.getInsideStreak(areaId)
            )
        }

    @Test
    fun exit_requiredTwo_thirdExit_remainsConfirmed() = runBlocking {

        val first =
            GeofenceTrackStore.confirmByStreaks(
                areaId,
                Definition.EVT_EXIT,
                1,
                2
            )

        val second =
            GeofenceTrackStore.confirmByStreaks(
                areaId,
                Definition.EVT_EXIT,
                1,
                2
            )

        val third =
            GeofenceTrackStore.confirmByStreaks(
                areaId,
                Definition.EVT_EXIT,
                1,
                2
            )

        assertFalse(first)
        assertTrue(second)
        assertTrue(third)

        assertEquals(
            3,
            GeofenceTrackStore.getOutsideStreak(areaId)
        )
    }

    // ============================================================
    // ENTER
    // ============================================================

    @Test
    fun enter_requiredOne_firstEnterIsConfirmed() = runBlocking {

        val result =
            GeofenceTrackStore.confirmByStreaks(
                areaId = areaId,
                candidate = Definition.EVT_ENTER,
                requiredEnterConfirmCount = 1,
                requiredExitConfirmCount = 2
            )

        assertTrue(result)

        assertEquals(
            1,
            GeofenceTrackStore.getInsideStreak(areaId)
        )

        assertEquals(
            0,
            GeofenceTrackStore.getOutsideStreak(areaId)
        )
    }

    @Test
    fun enter_multipleConsecutive_incrementsInsideStreak() =
        runBlocking {

            GeofenceTrackStore.confirmByStreaks(
                areaId,
                Definition.EVT_ENTER,
                1,
                2
            )

            GeofenceTrackStore.confirmByStreaks(
                areaId,
                Definition.EVT_ENTER,
                1,
                2
            )

            assertEquals(
                2,
                GeofenceTrackStore.getInsideStreak(areaId)
            )

            assertEquals(
                0,
                GeofenceTrackStore.getOutsideStreak(areaId)
            )
        }

    // ============================================================
    // CAMBIO EXIT -> ENTER
    // ============================================================

    @Test
    fun enter_resetsPreviousExitStreak() = runBlocking {

        val firstExit =
            GeofenceTrackStore.confirmByStreaks(
                areaId,
                Definition.EVT_EXIT,
                1,
                2
            )

        assertFalse(firstExit)

        assertEquals(
            1,
            GeofenceTrackStore.getOutsideStreak(areaId)
        )

        val enter =
            GeofenceTrackStore.confirmByStreaks(
                areaId,
                Definition.EVT_ENTER,
                1,
                2
            )

        assertTrue(enter)

        assertEquals(
            0,
            GeofenceTrackStore.getOutsideStreak(areaId)
        )

        assertEquals(
            1,
            GeofenceTrackStore.getInsideStreak(areaId)
        )
    }

    // ============================================================
    // CAMBIO ENTER -> EXIT
    // ============================================================

    @Test
    fun exit_resetsPreviousEnterStreak() = runBlocking {

        GeofenceTrackStore.confirmByStreaks(
            areaId,
            Definition.EVT_ENTER,
            1,
            2
        )

        assertEquals(
            1,
            GeofenceTrackStore.getInsideStreak(areaId)
        )

        val exit =
            GeofenceTrackStore.confirmByStreaks(
                areaId,
                Definition.EVT_EXIT,
                1,
                2
            )

        assertFalse(exit)

        assertEquals(
            0,
            GeofenceTrackStore.getInsideStreak(areaId)
        )

        assertEquals(
            1,
            GeofenceTrackStore.getOutsideStreak(areaId)
        )
    }

    // ============================================================
    // EXIT -> ENTER -> EXIT
    // ============================================================

    @Test
    fun suspiciousExit_enter_exit_doesNotCountAsTwoConsecutiveExits() =
        runBlocking {

            val exit1 =
                GeofenceTrackStore.confirmByStreaks(
                    areaId,
                    Definition.EVT_EXIT,
                    1,
                    2
                )

            assertFalse(exit1)

            assertEquals(
                1,
                GeofenceTrackStore.getOutsideStreak(areaId)
            )

            val enter =
                GeofenceTrackStore.confirmByStreaks(
                    areaId,
                    Definition.EVT_ENTER,
                    1,
                    2
                )

            assertTrue(enter)

            assertEquals(
                0,
                GeofenceTrackStore.getOutsideStreak(areaId)
            )

            val exit2 =
                GeofenceTrackStore.confirmByStreaks(
                    areaId,
                    Definition.EVT_EXIT,
                    1,
                    2
                )

            // Debe ser nuevamente el primer EXIT consecutivo.
            assertFalse(exit2)

            assertEquals(
                1,
                GeofenceTrackStore.getOutsideStreak(areaId)
            )

            assertEquals(
                0,
                GeofenceTrackStore.getInsideStreak(areaId)
            )
        }

    // ============================================================
    // RESET COMPLETO
    // ============================================================

    @Test
    fun resetStreaks_resetsBothCounters() = runBlocking {

        GeofenceTrackStore.confirmByStreaks(
            areaId,
            Definition.EVT_EXIT,
            1,
            2
        )

        assertEquals(
            1,
            GeofenceTrackStore.getOutsideStreak(areaId)
        )

        GeofenceTrackStore.resetStreaks(areaId)

        assertEquals(
            0,
            GeofenceTrackStore.getOutsideStreak(areaId)
        )

        assertEquals(
            0,
            GeofenceTrackStore.getInsideStreak(areaId)
        )
    }

    // ============================================================
    // RESET SOLO OUTSIDE
    // ============================================================

    @Test
    fun resetOutsideStreak_resetsOutsideToZero() = runBlocking {

        GeofenceTrackStore.confirmByStreaks(
            areaId,
            Definition.EVT_EXIT,
            1,
            2
        )

        assertEquals(
            1,
            GeofenceTrackStore.getOutsideStreak(areaId)
        )

        GeofenceTrackStore.resetOutsideStreak(areaId)

        assertEquals(
            0,
            GeofenceTrackStore.getOutsideStreak(areaId)
        )
    }

    // ============================================================
    // CONTINUE
    // ============================================================

    @Test
    fun continue_returnsFalse() = runBlocking {

        val result =
            GeofenceTrackStore.confirmByStreaks(
                areaId,
                Definition.EVT_CONTINUE,
                1,
                2
            )

        assertFalse(result)

        assertEquals(
            0,
            GeofenceTrackStore.getInsideStreak(areaId)
        )

        assertEquals(
            0,
            GeofenceTrackStore.getOutsideStreak(areaId)
        )
    }

    // ============================================================
    // CANDIDATO DESCONOCIDO
    // ============================================================

    @Test
    fun unknownCandidate_returnsFalse() = runBlocking {

        val result =
            GeofenceTrackStore.confirmByStreaks(
                areaId,
                "UNKNOWN",
                1,
                2
            )

        assertFalse(result)

        assertEquals(
            0,
            GeofenceTrackStore.getInsideStreak(areaId)
        )

        assertEquals(
            0,
            GeofenceTrackStore.getOutsideStreak(areaId)
        )
    }

    // ============================================================
    // INDEPENDENCIA ENTRE AREAS
    // ============================================================

    @Test
    fun streaks_differentAreas_areIndependent() = runBlocking {

        val area1FirstExit =
            GeofenceTrackStore.confirmByStreaks(
                areaId,
                Definition.EVT_EXIT,
                1,
                2
            )

        val area2FirstExit =
            GeofenceTrackStore.confirmByStreaks(
                areaId2,
                Definition.EVT_EXIT,
                1,
                2
            )

        assertFalse(area1FirstExit)
        assertFalse(area2FirstExit)

        assertEquals(
            1,
            GeofenceTrackStore.getOutsideStreak(areaId)
        )

        assertEquals(
            1,
            GeofenceTrackStore.getOutsideStreak(areaId2)
        )

        val area1SecondExit =
            GeofenceTrackStore.confirmByStreaks(
                areaId,
                Definition.EVT_EXIT,
                1,
                2
            )

        assertTrue(area1SecondExit)

        assertEquals(
            2,
            GeofenceTrackStore.getOutsideStreak(areaId)
        )

        // Área 2 debe seguir teniendo solamente una confirmación.
        assertEquals(
            1,
            GeofenceTrackStore.getOutsideStreak(areaId2)
        )
    }
}