package com.example.comunicationwearmobile.utils.Helpers.Geofences.FSM

import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.FSM.GeofenceEventFsmDetector

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FSMAntiTeleportFilterTest {

    private lateinit var context: Context
    private lateinit var area: EntityAreaGeofence

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        area = EntityAreaGeofence(
            id_area = 90001L,
            latitude = "0",
            longitude = "0",
            meters = 100
        )
    }

    private fun required(
        candidate: String = Definition.EVT_EXIT,
        distance: Float,
        meterForExit: Float = 120f,
        accuracy: Float = 10f,
        radius: Float = 100f,
        previousDistance: Float
    ): Int = runBlocking {
        GeofenceEventFsmDetector.computeRequiredExitConfirmAndLog(
            context = context,
            area = area,
            candidate = candidate,
            distance = distance,
            meterForExit = meterForExit,
            accuracy = accuracy,
            radiusMeters = radius,
            previousDistance = previousDistance
        )
    }

    // ============================================================
    // 1. PRIMERA LECTURA
    // ============================================================

    @Test
    fun firstLocation_withoutPreviousDistance_requiresOne() {
        assertEquals(
            1,
            required(
                distance = 125f,
                previousDistance = -1f
            )
        )
    }

    @Test
    fun firstLocation_highAccuracy_withoutStrongExit_requiresTwo() {
        assertEquals(
            2,
            required(
                distance = 125f,
                accuracy = 36f,
                previousDistance = -1f
            )
        )
    }

    @Test
    fun firstLocation_strongExit_requiresOne() {
        assertEquals(
            1,
            required(
                distance = 140f,
                accuracy = 10f,
                previousDistance = -1f
            )
        )
    }

    // ============================================================
    // 2. ACCURACY: 35 % DEL RADIO
    // radius=100 -> límite=35
    // suspicious solamente si accuracy > 35
    // ============================================================

    @Test
    fun accuracy_below35Percent_notSuspicious() {
        assertEquals(
            1,
            required(
                distance = 125f,
                accuracy = 34.9f,
                previousDistance = 120f
            )
        )
    }

    @Test
    fun accuracy_exactly35Percent_notSuspicious() {
        assertEquals(
            1,
            required(
                distance = 125f,
                accuracy = 35f,
                previousDistance = 120f
            )
        )
    }

    @Test
    fun accuracy_above35Percent_isSuspicious() {
        assertEquals(
            2,
            required(
                distance = 125f,
                accuracy = 35.1f,
                previousDistance = 120f
            )
        )
    }

    // ============================================================
    // 3. JUMP CON UMBRAL MÍNIMO DE 25 m
    // accuracy=10 -> 2*accuracy=20
    // max(20,25)=25
    // ============================================================

    @Test
    fun jump_below25_notSuspicious() {
        assertEquals(
            1,
            required(
                distance = 125f,
                accuracy = 10f,
                previousDistance = 100.1f
            )
        )
    }

    @Test
    fun jump_exactly25_notSuspicious() {
        assertEquals(
            1,
            required(
                distance = 125f,
                accuracy = 10f,
                previousDistance = 100f
            )
        )
    }

    @Test
    fun jump_above25_isSuspicious() {
        assertEquals(
            2,
            required(
                distance = 125f,
                accuracy = 10f,
                previousDistance = 99.9f
            )
        )
    }

    // ============================================================
    // 4. JUMP CUANDO DOMINA 2*accuracy
    // accuracy=20 -> max(40,25)=40
    // ============================================================

    @Test
    fun jump_belowTwoAccuracy_notSuspicious() {
        assertEquals(
            1,
            required(
                distance = 125f,
                accuracy = 20f,
                previousDistance = 85.1f
            )
        )
    }

    @Test
    fun jump_exactlyTwoAccuracy_notSuspicious() {
        assertEquals(
            1,
            required(
                distance = 125f,
                accuracy = 20f,
                previousDistance = 85f
            )
        )
    }

    @Test
    fun jump_aboveTwoAccuracy_isSuspicious() {
        assertEquals(
            2,
            required(
                distance = 125f,
                accuracy = 20f,
                previousDistance = 84.9f
            )
        )
    }

    // ============================================================
    // 5. JUMP FUNCIONA EN AMBAS DIRECCIONES POR abs()
    // ============================================================

    @Test
    fun largePositiveJump_isSuspicious() {
        assertEquals(
            2,
            required(
                distance = 125f,
                accuracy = 10f,
                previousDistance = 90f
            )
        )
    }

    @Test
    fun largeNegativeJump_isSuspicious() {
        assertEquals(
            2,
            required(
                distance = 125f,
                accuracy = 10f,
                previousDistance = 160f
            )
        )
    }

    @Test
    fun noJump_notSuspicious() {
        assertEquals(
            1,
            required(
                distance = 125f,
                accuracy = 10f,
                previousDistance = 125f
            )
        )
    }

    // ============================================================
    // 6. STRONG EXIT - UMBRAL MÍNIMO 12 m
    // accuracy=5 -> 1.2*5=6 -> domina 12
    // ============================================================

    @Test
    fun strongExit_justBelow12_notStrong() {
        assertEquals(
            1,
            required(
                distance = 131.9f,
                accuracy = 5f,
                previousDistance = 130f
            )
        )
    }

    @Test
    fun strongExit_exactly12_isStrong() {
        assertEquals(
            1,
            required(
                distance = 132f,
                accuracy = 5f,
                previousDistance = 130f
            )
        )
    }

    @Test
    fun strongExit_above12_isStrong() {
        assertEquals(
            1,
            required(
                distance = 132.1f,
                accuracy = 5f,
                previousDistance = 130f
            )
        )
    }

    // ============================================================
    // 7. STRONG EXIT CUANDO DOMINA 1.2*accuracy
    // accuracy=20 -> 24 metros
    // ============================================================

    @Test
    fun strongExit_belowAccuracyThreshold_notStrong() {
        assertEquals(
            1,
            required(
                distance = 143.9f,
                accuracy = 20f,
                previousDistance = 140f
            )
        )
    }

    @Test
    fun strongExit_exactAccuracyThreshold_isStrong() {
        assertEquals(
            1,
            required(
                distance = 144f,
                accuracy = 20f,
                previousDistance = 140f
            )
        )
    }

    @Test
    fun strongExit_aboveAccuracyThreshold_isStrong() {
        assertEquals(
            1,
            required(
                distance = 144.1f,
                accuracy = 20f,
                previousDistance = 140f
            )
        )
    }

    // ============================================================
    // 8. SUSPICIOUS + STRONG
    // STRONG TIENE PRIORIDAD
    // ============================================================

    @Test
    fun suspiciousByJump_andStrong_requiresOne() {
        assertEquals(
            1,
            required(
                distance = 140f,
                accuracy = 10f,
                previousDistance = 100f
            )
        )
    }

    @Test
    fun suspiciousByAccuracy_andStrong_requiresOne() {
        assertEquals(
            1,
            required(
                distance = 170f,
                accuracy = 40f,
                previousDistance = 160f
            )
        )
    }

    @Test
    fun suspiciousByAccuracyAndJump_andStrong_requiresOne() {
        assertEquals(
            1,
            required(
                distance = 170f,
                accuracy = 40f,
                previousDistance = 80f
            )
        )
    }

    // ============================================================
    // 9. SUSPICIOUS SIN STRONG
    // ============================================================

    @Test
    fun suspiciousOnlyByAccuracy_requiresTwo() {
        assertEquals(
            2,
            required(
                distance = 125f,
                accuracy = 36f,
                previousDistance = 120f
            )
        )
    }

    @Test
    fun suspiciousOnlyByJump_requiresTwo() {
        assertEquals(
            2,
            required(
                distance = 125f,
                accuracy = 10f,
                previousDistance = 90f
            )
        )
    }

    @Test
    fun suspiciousByAccuracyAndJump_requiresTwo() {
        assertEquals(
            2,
            required(
                distance = 125f,
                accuracy = 36f,
                previousDistance = 50f
            )
        )
    }

    // ============================================================
    // 10. CANDIDATOS QUE NO SON EXIT
    // ============================================================

    @Test
    fun enter_neverUsesAntiTeleport() {
        assertEquals(
            1,
            required(
                candidate = Definition.EVT_ENTER,
                distance = 200f,
                accuracy = 100f,
                previousDistance = 0f
            )
        )
    }

    @Test
    fun continue_neverUsesAntiTeleport() {
        assertEquals(
            1,
            required(
                candidate = Definition.EVT_CONTINUE,
                distance = 200f,
                accuracy = 100f,
                previousDistance = 0f
            )
        )
    }

    @Test
    fun unknownCandidate_neverUsesAntiTeleport() {
        assertEquals(
            1,
            required(
                candidate = "UNKNOWN",
                distance = 200f,
                accuracy = 100f,
                previousDistance = 0f
            )
        )
    }

    @Test
    fun emptyCandidate_neverUsesAntiTeleport() {
        assertEquals(
            1,
            required(
                candidate = "",
                distance = 200f,
                accuracy = 100f,
                previousDistance = 0f
            )
        )
    }

    // ============================================================
    // 11. RADIOS REALES DEL PROYECTO
    // ============================================================

    @Test
    fun radius30_accuracyAbove35Percent_isSuspicious() {
        // 35% de 30 = 10.5
        assertEquals(
            2,
            required(
                distance = 38f,
                meterForExit = 36f,
                accuracy = 10.6f,
                radius = 30f,
                previousDistance = 37f
            )
        )
    }

    @Test
    fun radius30_accuracyExactly35Percent_notSuspicious() {
        assertEquals(
            1,
            required(
                distance = 38f,
                meterForExit = 36f,
                accuracy = 10.5f,
                radius = 30f,
                previousDistance = 37f
            )
        )
    }

    @Test
    fun radius300_accuracyAbove35Percent_isSuspicious() {
        // 35% de 300 = 105
        assertEquals(
            2,
            required(
                distance = 365f,
                meterForExit = 360f,
                accuracy = 105.1f,
                radius = 300f,
                previousDistance = 364f
            )
        )
    }

    @Test
    fun radius300_accuracyExactly35Percent_notSuspicious() {
        assertEquals(
            1,
            required(
                distance = 365f,
                meterForExit = 360f,
                accuracy = 105f,
                radius = 300f,
                previousDistance = 364f
            )
        )
    }
}