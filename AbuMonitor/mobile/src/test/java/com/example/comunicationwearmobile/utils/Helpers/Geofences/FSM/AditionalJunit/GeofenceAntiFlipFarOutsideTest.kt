package com.example.comunicationwearmobile.utils.Helpers.Geofences.FSM.AditionalJunit

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.comunicationwearmobile.ui.model.extra.Metrics
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.FSM.GeofenceEventFsmDetector
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.FSM.GeofenceTrackStore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class GeofenceAntiFlipFarOutsideTest {

    private val context: Context =
        ApplicationProvider.getApplicationContext()

    private val areaId = 2001L

    private val area = EntityAreaGeofence(
        id_area = areaId,
        meters = 100
    )

    private val initialTime = 1_000_000L

    private val meterForExit = 120f


    // ============================================================
    // RAPIDO - SIN FAR OUTSIDE
    // ============================================================

    @Test
    fun rapido_3Segundos_noFarOutside_debeBloquear() = runBlocking {

        prepararEventoAnterior()

        val metrics = createMetrics(
            distance = 150f
        )

        val blocked =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context = context,
                area = area,
                now = initialTime + 3_000L,
                isFast = true,
                stationary = false,
                candidate = Definition.EVT_EXIT,
                m = metrics,
                meterForExit = meterForExit
            )

        assertTrue(blocked)
    }


    @Test
    fun rapido_4999ms_noFarOutside_debeBloquear() = runBlocking {

        prepararEventoAnterior()

        val metrics = createMetrics(
            distance = 150f
        )

        val blocked =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context = context,
                area = area,
                now = initialTime + 4_999L,
                isFast = true,
                stationary = false,
                candidate = Definition.EVT_EXIT,
                m = metrics,
                meterForExit = meterForExit
            )

        assertTrue(blocked)
    }


    @Test
    fun rapido_5Segundos_noFarOutside_debePermitir() = runBlocking {

        prepararEventoAnterior()

        val metrics = createMetrics(
            distance = 150f
        )

        val blocked =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context = context,
                area = area,
                now = initialTime + 5_000L,
                isFast = true,
                stationary = false,
                candidate = Definition.EVT_EXIT,
                m = metrics,
                meterForExit = meterForExit
            )

        assertFalse(blocked)
    }


    // ============================================================
    // CAMINANDO - SIN FAR OUTSIDE
    // ============================================================

    @Test
    fun caminando_5Segundos_noFarOutside_debeBloquear() = runBlocking {

        prepararEventoAnterior()

        val metrics = createMetrics(
            distance = 150f
        )

        val blocked =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context = context,
                area = area,
                now = initialTime + 5_000L,
                isFast = false,
                stationary = false,
                candidate = Definition.EVT_EXIT,
                m = metrics,
                meterForExit = meterForExit
            )

        assertTrue(blocked)
    }


    @Test
    fun caminando_9999ms_noFarOutside_debeBloquear() = runBlocking {

        prepararEventoAnterior()

        val metrics = createMetrics(
            distance = 150f
        )

        val blocked =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context = context,
                area = area,
                now = initialTime + 9_999L,
                isFast = false,
                stationary = false,
                candidate = Definition.EVT_EXIT,
                m = metrics,
                meterForExit = meterForExit
            )

        assertTrue(blocked)
    }


    @Test
    fun caminando_10Segundos_noFarOutside_debePermitir() = runBlocking {

        prepararEventoAnterior()

        val metrics = createMetrics(
            distance = 150f
        )

        val blocked =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context = context,
                area = area,
                now = initialTime + 10_000L,
                isFast = false,
                stationary = false,
                candidate = Definition.EVT_EXIT,
                m = metrics,
                meterForExit = meterForExit
            )

        assertFalse(blocked)
    }


    // ============================================================
    // ESTACIONARIO - SIN FAR OUTSIDE
    // ============================================================

    @Test
    fun estacionario_10Segundos_noFarOutside_debeBloquear() = runBlocking {

        prepararEventoAnterior()

        val metrics = createMetrics(
            distance = 150f
        )

        val blocked =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context = context,
                area = area,
                now = initialTime + 10_000L,
                isFast = false,
                stationary = true,
                candidate = Definition.EVT_EXIT,
                m = metrics,
                meterForExit = meterForExit
            )

        assertTrue(blocked)
    }


    @Test
    fun estacionario_479999ms_noFarOutside_debeBloquear() = runBlocking {

        prepararEventoAnterior()

        val metrics = createMetrics(
            distance = 150f
        )

        val blocked =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context = context,
                area = area,
                now = initialTime + 479_999L,
                isFast = false,
                stationary = true,
                candidate = Definition.EVT_EXIT,
                m = metrics,
                meterForExit = meterForExit
            )

        assertTrue(blocked)
    }


    @Test
    fun estacionario_8Minutos_noFarOutside_debePermitir() = runBlocking {

        prepararEventoAnterior()

        val metrics = createMetrics(
            distance = 150f
        )

        val blocked =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context = context,
                area = area,
                now = initialTime + 480_000L,
                isFast = false,
                stationary = true,
                candidate = Definition.EVT_EXIT,
                m = metrics,
                meterForExit = meterForExit
            )

        assertFalse(blocked)
    }


    // ============================================================
    // FAR OUTSIDE
    // Debe permitir EXIT aunque siga activo el cooldown
    // ============================================================

    @Test
    fun rapido_3Segundos_farOutside_debePermitir() = runBlocking {

        prepararEventoAnterior()

        val metrics = createMetrics(
            distance = 170f
        )

        val blocked =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context = context,
                area = area,
                now = initialTime + 3_000L,
                isFast = true,
                stationary = false,
                candidate = Definition.EVT_EXIT,
                m = metrics,
                meterForExit = meterForExit
            )

        assertFalse(blocked)
    }


    @Test
    fun caminando_3Segundos_farOutside_debePermitir() = runBlocking {

        prepararEventoAnterior()

        val metrics = createMetrics(
            distance = 170f
        )

        val blocked =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context = context,
                area = area,
                now = initialTime + 3_000L,
                isFast = false,
                stationary = false,
                candidate = Definition.EVT_EXIT,
                m = metrics,
                meterForExit = meterForExit
            )

        assertFalse(blocked)
    }


    @Test
    fun estacionario_3Segundos_farOutside_debePermitir() = runBlocking {

        prepararEventoAnterior()

        val metrics = createMetrics(
            distance = 170f
        )

        val blocked =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context = context,
                area = area,
                now = initialTime + 3_000L,
                isFast = false,
                stationary = true,
                candidate = Definition.EVT_EXIT,
                m = metrics,
                meterForExit = meterForExit
            )

        assertFalse(blocked)
    }


    // ============================================================
    // LIMITES EXACTOS DE FAR OUTSIDE
    // ============================================================

    @Test
    fun farOutside_justoAntesDelLimite_debeBloquear() = runBlocking {

        prepararEventoAnterior()

        val metrics = createMetrics(
            distance = 169.999f
        )

        val blocked =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context = context,
                area = area,
                now = initialTime + 3_000L,
                isFast = false,
                stationary = true,
                candidate = Definition.EVT_EXIT,
                m = metrics,
                meterForExit = meterForExit
            )

        assertTrue(blocked)
    }


    @Test
    fun farOutside_exactamenteEnElLimite_debePermitir() = runBlocking {

        prepararEventoAnterior()

        val metrics = createMetrics(
            distance = 170f
        )

        val blocked =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context = context,
                area = area,
                now = initialTime + 3_000L,
                isFast = false,
                stationary = true,
                candidate = Definition.EVT_EXIT,
                m = metrics,
                meterForExit = meterForExit
            )

        assertFalse(blocked)
    }


    // ============================================================
    // FAR OUTSIDE SOLO SE APLICA A EXIT
    // ============================================================

    @Test
    fun enter_muyLejos_noDebeAplicarFarOutside() = runBlocking {

        prepararEventoAnterior()

        val metrics = createMetrics(
            distance = 200f
        )

        val blocked =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context = context,
                area = area,
                now = initialTime + 3_000L,
                isFast = false,
                stationary = true,
                candidate = Definition.EVT_ENTER,
                m = metrics,
                meterForExit = meterForExit
            )

        assertTrue(blocked)
    }


    // ============================================================
    // FUNCIONES AUXILIARES
    // ============================================================

    private suspend fun prepararEventoAnterior() {

        GeofenceTrackStore.acceptEvent(
            areaId,
            initialTime,
            100f
        )
    }


    private fun createMetrics(
        distance: Float
    ): Metrics {

        return Metrics(
            distance = distance,
            radiusMeters = 100f,
            accuracy = 10f,
            distanceToBorder =
            kotlin.math.abs(distance - 100f)
        )
    }
}