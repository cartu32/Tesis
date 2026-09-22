package com.example.comunicationwearmobile.utils.Helpers.Geofences.FSM.AditionalJunit.AntiFlip


import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.FSM.GeofenceEventFsmDetector
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class GeofenceAntiTeleportTest {

    private lateinit var context: Context
    private lateinit var area: EntityAreaGeofence

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()

        area = EntityAreaGeofence().apply {
            id_area = 1L
            meters = 100
        }
    }

    /**
     * Movimiento gradual.
     *
     * previous = 120
     * actual   = 130
     * jump     = 10
     *
     * accuracy = 10
     * umbral jump = max(20, 25) = 25
     *
     * 10 > 25 = false
     *
     * No suspicious.
     * No strong.
     *
     * Resultado esperado: 1.
     */
    @Test
    fun gradualMovement_doesNotRequireSecondConfirmation() = runBlocking {

        val result = GeofenceEventFsmDetector.computeRequiredExitConfirmAndLog(
            context = context,
            area = area,
            candidate = Definition.EVT_EXIT,
            distance = 130f,
            meterForExit = 120f,
            accuracy = 10f,
            radiusMeters = 100f,
            previousDistance = 120f
        )

        assertEquals(1, result)
    }

    /**
     * Salto GPS.
     *
     * previous = 130
     * actual   = 160
     * jump     = 30
     *
     * accuracy = 10
     * umbral = max(20,25) = 25
     *
     * 30 > 25 -> suspicious
     *
     * Evitamos strong usando meterForExit=155:
     * overshoot = 5
     *
     * Resultado esperado: 2.
     */
    @Test
    fun gpsJump_requiresTwoConfirmations() = runBlocking {

        val result = GeofenceEventFsmDetector.computeRequiredExitConfirmAndLog(
            context = context,
            area = area,
            candidate = Definition.EVT_EXIT,
            distance = 160f,
            meterForExit = 155f,
            accuracy = 10f,
            radiusMeters = 100f,
            previousDistance = 130f
        )

        assertEquals(2, result)
    }

    /**
     * Primera lectura.
     *
     * previousDistance = -1
     *
     * No existe lectura anterior.
     * Por lo tanto jump=0 y no puede activar suspicious por salto.
     */
    @Test
    fun firstLocation_withoutPreviousDistance_doesNotDetectJump() = runBlocking {

        val result = GeofenceEventFsmDetector.computeRequiredExitConfirmAndLog(
            context = context,
            area = area,
            candidate = Definition.EVT_EXIT,
            distance = 125f,
            meterForExit = 120f,
            accuracy = 10f,
            radiusMeters = 100f,
            previousDistance = -1f
        )

        assertEquals(1, result)
    }

    /**
     * Accuracy alto.
     *
     * R=100
     * 35% R = 35
     *
     * accuracy=36 > 35
     *
     * suspicious=true aunque no exista salto.
     *
     * Evitamos strong.
     */
    @Test
    fun highAccuracyError_requiresTwoConfirmations() = runBlocking {

        val result = GeofenceEventFsmDetector.computeRequiredExitConfirmAndLog(
            context = context,
            area = area,
            candidate = Definition.EVT_EXIT,
            distance = 125f,
            meterForExit = 120f,
            accuracy = 36f,
            radiusMeters = 100f,
            previousDistance = 124f
        )

        assertEquals(2, result)
    }

    /**
     * Strong EXIT.
     *
     * accuracy=10
     *
     * strong threshold:
     * max(10*1.2,12) = 12
     *
     * overshoot=15
     *
     * Resultado esperado: 1.
     */
    @Test
    fun strongExit_requiresOnlyOneConfirmation() = runBlocking {

        val result = GeofenceEventFsmDetector.computeRequiredExitConfirmAndLog(
            context = context,
            area = area,
            candidate = Definition.EVT_EXIT,
            distance = 135f,
            meterForExit = 120f,
            accuracy = 10f,
            radiusMeters = 100f,
            previousDistance = 130f
        )

        assertEquals(1, result)
    }

    /**
     * suspicious + strong simultáneamente.
     *
     * accuracy=40 > 35 -> suspicious
     *
     * overshoot=50
     * strong threshold=max(48,12)=48
     * 50 >= 48 -> strong
     *
     * strong tiene prioridad.
     *
     * Resultado esperado: 1.
     */
    @Test
    fun strongAndSuspicious_strongHasPriority() = runBlocking {

        val result = GeofenceEventFsmDetector.computeRequiredExitConfirmAndLog(
            context = context,
            area = area,
            candidate = Definition.EVT_EXIT,
            distance = 170f,
            meterForExit = 120f,
            accuracy = 40f,
            radiusMeters = 100f,
            previousDistance = 169f
        )

        assertEquals(1, result)
    }

    /**
     * FRONTERA: jump exactamente 25.
     *
     * accuracy=10 -> 2*accuracy=20
     * threshold=max(20,25)=25
     *
     * Código usa:
     * jump > threshold
     *
     * 25 > 25 = false
     *
     * Resultado esperado: 1.
     */
    @Test
    fun jumpExactly25_isNotSuspicious() = runBlocking {

        val result = GeofenceEventFsmDetector.computeRequiredExitConfirmAndLog(
            context = context,
            area = area,
            candidate = Definition.EVT_EXIT,
            distance = 150f,
            meterForExit = 145f,
            accuracy = 10f,
            radiusMeters = 100f,
            previousDistance = 125f
        )

        assertEquals(1, result)
    }

    /**
     * Apenas superior a 25.
     *
     * jump=25.1
     *
     * 25.1 > 25 -> suspicious.
     */
    @Test
    fun jumpJustAbove25_isSuspicious() = runBlocking {

        val result = GeofenceEventFsmDetector.computeRequiredExitConfirmAndLog(
            context = context,
            area = area,
            candidate = Definition.EVT_EXIT,
            distance = 150.1f,
            meterForExit = 145f,
            accuracy = 10f,
            radiusMeters = 100f,
            previousDistance = 125f
        )

        assertEquals(2, result)
    }

    /**
     * FRONTERA 2*accuracy.
     *
     * accuracy=20
     * 2*accuracy=40
     *
     * threshold=max(40,25)=40
     *
     * jump=40 exactos.
     *
     * 40 > 40 = false.
     */
    @Test
    fun jumpExactlyTwoTimesAccuracy_isNotSuspicious() = runBlocking {

        val result = GeofenceEventFsmDetector.computeRequiredExitConfirmAndLog(
            context = context,
            area = area,
            candidate = Definition.EVT_EXIT,
            distance = 160f,
            meterForExit = 155f,
            accuracy = 20f,
            radiusMeters = 100f,
            previousDistance = 120f
        )

        assertEquals(1, result)
    }

    /**
     * Apenas superior a 2*accuracy.
     *
     * accuracy=20
     * threshold=40
     * jump=40.1
     *
     * suspicious=true.
     */
    @Test
    fun jumpJustAboveTwoTimesAccuracy_isSuspicious() = runBlocking {

        val result = GeofenceEventFsmDetector.computeRequiredExitConfirmAndLog(
            context = context,
            area = area,
            candidate = Definition.EVT_EXIT,
            distance = 160.1f,
            meterForExit = 155f,
            accuracy = 20f,
            radiusMeters = 100f,
            previousDistance = 120f
        )

        assertEquals(2, result)
    }

    /**
     * FRONTERA strong: overshoot exactamente 12.
     *
     * accuracy=10
     * accuracy*1.2=12
     *
     * threshold=max(12,12)=12
     *
     * Código usa >=.
     *
     * Resultado: strong=true -> 1.
     */
    @Test
    fun overshootExactly12_isStrongExit() = runBlocking {

        val result = GeofenceEventFsmDetector.computeRequiredExitConfirmAndLog(
            context = context,
            area = area,
            candidate = Definition.EVT_EXIT,
            distance = 132f,
            meterForExit = 120f,
            accuracy = 10f,
            radiusMeters = 100f,
            previousDistance = 130f
        )

        assertEquals(1, result)
    }

    /**
     * Justo debajo de 12.
     *
     * overshoot=11.9
     *
     * No strong.
     * Tampoco suspicious.
     */
    @Test
    fun overshootJustBelow12_isNotStrongExit() = runBlocking {

        val result = GeofenceEventFsmDetector.computeRequiredExitConfirmAndLog(
            context = context,
            area = area,
            candidate = Definition.EVT_EXIT,
            distance = 131.9f,
            meterForExit = 120f,
            accuracy = 10f,
            radiusMeters = 100f,
            previousDistance = 130f
        )

        assertEquals(1, result)
    }

    /**
     * FRONTERA 1.2*accuracy cuando domina accuracy.
     *
     * accuracy=20
     * 1.2*20=24
     *
     * threshold=max(24,12)=24
     *
     * overshoot=24 exactos.
     *
     * >= -> strong.
     */
    @Test
    fun overshootExactlyOnePointTwoAccuracy_isStrongExit() = runBlocking {

        val result = GeofenceEventFsmDetector.computeRequiredExitConfirmAndLog(
            context = context,
            area = area,
            candidate = Definition.EVT_EXIT,
            distance = 144f,
            meterForExit = 120f,
            accuracy = 20f,
            radiusMeters = 100f,
            previousDistance = 140f
        )

        assertEquals(1, result)
    }

    /**
     * Justo debajo de 1.2*accuracy.
     *
     * accuracy=20
     * threshold=24
     *
     * overshoot=23.9
     *
     * No strong.
     */
    @Test
    fun overshootJustBelowOnePointTwoAccuracy_isNotStrongExit() = runBlocking {

        val result = GeofenceEventFsmDetector.computeRequiredExitConfirmAndLog(
            context = context,
            area = area,
            candidate = Definition.EVT_EXIT,
            distance = 143.9f,
            meterForExit = 120f,
            accuracy = 20f,
            radiusMeters = 100f,
            previousDistance = 140f
        )

        assertEquals(1, result)
    }

    /**
     * ENTER nunca debe ser afectado por anti-teleport.
     *
     * Aunque haya un salto enorme y accuracy alto.
     */
    @Test
    fun enter_isNeverAffectedByAntiTeleport() = runBlocking {

        val result = GeofenceEventFsmDetector.computeRequiredExitConfirmAndLog(
            context = context,
            area = area,
            candidate = Definition.EVT_ENTER,
            distance = 50f,
            meterForExit = 120f,
            accuracy = 50f,
            radiusMeters = 100f,
            previousDistance = 200f
        )

        assertEquals(1, result)
    }
}