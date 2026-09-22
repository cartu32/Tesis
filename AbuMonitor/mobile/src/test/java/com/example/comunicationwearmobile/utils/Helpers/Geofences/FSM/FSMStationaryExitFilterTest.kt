package com.example.comunicationwearmobile.utils.Helpers.Geofences.FSM

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.FSM.GeofenceEventFsmDetector
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FSMStationaryExitFilterTest {

    private lateinit var context: Context
    private lateinit var area: EntityAreaGeofence

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()

        area = EntityAreaGeofence()
        area.id_area = 99999L
    }

    // ============================================================
    // 1. NO ESTACIONARIO
    // ============================================================

    @Test
    fun notStationary_exitInsideThreshold_notBlocked() = runBlocking {
        val result = testFilter(
            stationary = false,
            candidate = Definition.EVT_EXIT,
            distance = 100f,
            meterForExit = 120f,
            accuracy = 10f
        )

        assertFalse(result)
    }

    @Test
    fun notStationary_exitBelowClearThreshold_notBlocked() = runBlocking {
        val result = testFilter(
            stationary = false,
            candidate = Definition.EVT_EXIT,
            distance = 125f,
            meterForExit = 120f,
            accuracy = 10f
        )

        assertFalse(result)
    }

    @Test
    fun notStationary_exitExactlyClearThreshold_notBlocked() = runBlocking {
        val result = testFilter(
            stationary = false,
            candidate = Definition.EVT_EXIT,
            distance = 130f,
            meterForExit = 120f,
            accuracy = 10f
        )

        assertFalse(result)
    }

    // ============================================================
    // 2. CANDIDATO DISTINTO DE EXIT
    // ============================================================

    @Test
    fun stationary_enter_notBlocked() = runBlocking {
        val result = testFilter(
            stationary = true,
            candidate = Definition.EVT_ENTER,
            distance = 100f,
            meterForExit = 120f,
            accuracy = 10f
        )

        assertFalse(result)
    }

    @Test
    fun stationary_continue_notBlocked() = runBlocking {
        val result = testFilter(
            stationary = true,
            candidate = Definition.EVT_CONTINUE,
            distance = 100f,
            meterForExit = 120f,
            accuracy = 10f
        )

        assertFalse(result)
    }

    // ============================================================
    // 3. ACCURACY MENOR QUE 5
    // Margen mínimo = 5 metros
    // meterForExit = 120 -> umbral = 125
    // ============================================================

    @Test
    fun stationaryExit_accuracyZero_belowThreshold_blocked() = runBlocking {
        val result = testFilter(
            stationary = true,
            candidate = Definition.EVT_EXIT,
            distance = 124.9f,
            meterForExit = 120f,
            accuracy = 0f
        )

        assertTrue(result)
    }

    @Test
    fun stationaryExit_accuracyZero_exactThreshold_notBlocked() = runBlocking {
        val result = testFilter(
            stationary = true,
            candidate = Definition.EVT_EXIT,
            distance = 125f,
            meterForExit = 120f,
            accuracy = 0f
        )

        assertFalse(result)
    }

    @Test
    fun stationaryExit_accuracyThree_belowThreshold_blocked() = runBlocking {
        val result = testFilter(
            stationary = true,
            candidate = Definition.EVT_EXIT,
            distance = 124.9f,
            meterForExit = 120f,
            accuracy = 3f
        )

        assertTrue(result)
    }

    @Test
    fun stationaryExit_accuracy499_belowThreshold_blocked() = runBlocking {
        val result = testFilter(
            stationary = true,
            candidate = Definition.EVT_EXIT,
            distance = 124.99f,
            meterForExit = 120f,
            accuracy = 4.99f
        )

        assertTrue(result)
    }

    // ============================================================
    // 4. FRONTERA EXACTA accuracy = 5
    // Margen = 5
    // Umbral = 125
    // ============================================================

    @Test
    fun stationaryExit_accuracyFive_justBelowThreshold_blocked() = runBlocking {
        val result = testFilter(
            stationary = true,
            candidate = Definition.EVT_EXIT,
            distance = 124.99f,
            meterForExit = 120f,
            accuracy = 5f
        )

        assertTrue(result)
    }

    @Test
    fun stationaryExit_accuracyFive_exactThreshold_notBlocked() = runBlocking {
        val result = testFilter(
            stationary = true,
            candidate = Definition.EVT_EXIT,
            distance = 125f,
            meterForExit = 120f,
            accuracy = 5f
        )

        assertFalse(result)
    }

    @Test
    fun stationaryExit_accuracyFive_aboveThreshold_notBlocked() = runBlocking {
        val result = testFilter(
            stationary = true,
            candidate = Definition.EVT_EXIT,
            distance = 125.01f,
            meterForExit = 120f,
            accuracy = 5f
        )

        assertFalse(result)
    }

    // ============================================================
    // 5. ACCURACY MAYOR QUE 5
    // El margen pasa a ser el accuracy
    // ============================================================

    @Test
    fun stationaryExit_accuracySix_belowThreshold_blocked() = runBlocking {
        // Umbral = 120 + 6 = 126

        val result = testFilter(
            stationary = true,
            candidate = Definition.EVT_EXIT,
            distance = 125.9f,
            meterForExit = 120f,
            accuracy = 6f
        )

        assertTrue(result)
    }

    @Test
    fun stationaryExit_accuracySix_exactThreshold_notBlocked() = runBlocking {
        val result = testFilter(
            stationary = true,
            candidate = Definition.EVT_EXIT,
            distance = 126f,
            meterForExit = 120f,
            accuracy = 6f
        )

        assertFalse(result)
    }

    // ============================================================
    // 6. ACCURACY = 10
    // Umbral = 130
    // ============================================================

    @Test
    fun stationaryExit_accuracyTen_belowThreshold_blocked() = runBlocking {
        val result = testFilter(
            stationary = true,
            candidate = Definition.EVT_EXIT,
            distance = 129.9f,
            meterForExit = 120f,
            accuracy = 10f
        )

        assertTrue(result)
    }

    @Test
    fun stationaryExit_accuracyTen_exactThreshold_notBlocked() = runBlocking {
        val result = testFilter(
            stationary = true,
            candidate = Definition.EVT_EXIT,
            distance = 130f,
            meterForExit = 120f,
            accuracy = 10f
        )

        assertFalse(result)
    }

    @Test
    fun stationaryExit_accuracyTen_aboveThreshold_notBlocked() = runBlocking {
        val result = testFilter(
            stationary = true,
            candidate = Definition.EVT_EXIT,
            distance = 130.1f,
            meterForExit = 120f,
            accuracy = 10f
        )

        assertFalse(result)
    }

    // ============================================================
    // 7. ACCURACY = 20
    // Umbral = 140
    // ============================================================

    @Test
    fun stationaryExit_accuracyTwenty_belowThreshold_blocked() = runBlocking {
        val result = testFilter(
            stationary = true,
            candidate = Definition.EVT_EXIT,
            distance = 139.9f,
            meterForExit = 120f,
            accuracy = 20f
        )

        assertTrue(result)
    }

    @Test
    fun stationaryExit_accuracyTwenty_exactThreshold_notBlocked() = runBlocking {
        val result = testFilter(
            stationary = true,
            candidate = Definition.EVT_EXIT,
            distance = 140f,
            meterForExit = 120f,
            accuracy = 20f
        )

        assertFalse(result)
    }

    // ============================================================
    // 8. ACCURACY = 35
    // Umbral = 155
    // ============================================================

    @Test
    fun stationaryExit_accuracy35_belowThreshold_blocked() = runBlocking {
        val result = testFilter(
            stationary = true,
            candidate = Definition.EVT_EXIT,
            distance = 154.9f,
            meterForExit = 120f,
            accuracy = 35f
        )

        assertTrue(result)
    }

    @Test
    fun stationaryExit_accuracy35_exactThreshold_notBlocked() = runBlocking {
        val result = testFilter(
            stationary = true,
            candidate = Definition.EVT_EXIT,
            distance = 155f,
            meterForExit = 120f,
            accuracy = 35f
        )

        assertFalse(result)
    }

    // ============================================================
    // 9. ACCURACY MUY GRANDE
    // ============================================================

    @Test
    fun stationaryExit_accuracy50_belowThreshold_blocked() = runBlocking {
        val result = testFilter(
            stationary = true,
            candidate = Definition.EVT_EXIT,
            distance = 169.9f,
            meterForExit = 120f,
            accuracy = 50f
        )

        assertTrue(result)
    }

    @Test
    fun stationaryExit_accuracy50_exactThreshold_notBlocked() = runBlocking {
        val result = testFilter(
            stationary = true,
            candidate = Definition.EVT_EXIT,
            distance = 170f,
            meterForExit = 120f,
            accuracy = 50f
        )

        assertFalse(result)
    }

    @Test
    fun stationaryExit_accuracy100_belowThreshold_blocked() = runBlocking {
        val result = testFilter(
            stationary = true,
            candidate = Definition.EVT_EXIT,
            distance = 219.9f,
            meterForExit = 120f,
            accuracy = 100f
        )

        assertTrue(result)
    }

    @Test
    fun stationaryExit_accuracy100_exactThreshold_notBlocked() = runBlocking {
        val result = testFilter(
            stationary = true,
            candidate = Definition.EVT_EXIT,
            distance = 220f,
            meterForExit = 120f,
            accuracy = 100f
        )

        assertFalse(result)
    }

    // ============================================================
    // 10. CASOS DEFENSIVOS / EXTREMOS
    // ============================================================

    @Test
    fun stationaryExit_negativeAccuracy_usesMinimumFiveMeters() = runBlocking {
        // maxOf(-10, 5) = 5
        // Umbral = 125

        val result = testFilter(
            stationary = true,
            candidate = Definition.EVT_EXIT,
            distance = 124f,
            meterForExit = 120f,
            accuracy = -10f
        )

        assertTrue(result)
    }

    @Test
    fun stationaryExit_nanDistance_blocked() = runBlocking {
        val result = testFilter(
            stationary = true,
            candidate = Definition.EVT_EXIT,
            distance = Float.NaN,
            meterForExit = 120f,
            accuracy = 10f
        )

        // NaN >= 130 es false -> !clearExit -> bloquea
        assertTrue(result)
    }

    // ============================================================
    // FUNCIÓN AUXILIAR
    // ============================================================

    private suspend fun testFilter(
        stationary: Boolean,
        candidate: String,
        distance: Float,
        meterForExit: Float,
        accuracy: Float
    ): Boolean {
        return GeofenceEventFsmDetector.blockStationaryExitIfNotClear(
            context = context,
            area = area,
            stationary = stationary,
            candidate = candidate,
            distance = distance,
            meterForExit = meterForExit,
            accuracy = accuracy
        )
    }
}