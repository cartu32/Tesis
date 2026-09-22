package com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.FSM

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.comunicationwearmobile.ui.model.extra.Metrics
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FSMAntiFlipCooldownFilterTest {

    companion object {
        private const val NOW = 1_000_000L

        private const val FAST_COOLDOWN = 5_000L
        private const val WALKING_COOLDOWN = 10_000L
        private const val STATIONARY_COOLDOWN = 480_000L
    }

    private val context: Context =  ApplicationProvider.getApplicationContext()

    /*
     * ============================================================
     * AUXILIARES
     * ============================================================
     */

    private fun area(id: Long): EntityAreaGeofence {
        return EntityAreaGeofence(
            id_area = id,
            meters = 100
        )
    }

    private fun metrics(
        distance: Float,
        radius: Float = 100f,
        accuracy: Float = 10f
    ): Metrics {
        return Metrics(
            distance = distance,
            radiusMeters = radius,
            accuracy = accuracy,
            distanceToBorder = 0f
        )
    }

    private suspend fun prepareLastFlip(
        areaId: Long,
        elapsed: Long
    ) {
        GeofenceTrackStore.acceptEvent(
            areaId = areaId,
            now = NOW - elapsed,
            distance = 0f
        )
    }


    /*
     * ============================================================
     * 1. SELECCION DEL COOLDOWN
     * ============================================================
     */

    @Test
    fun `caminando usa cooldown de 10 segundos`() {
        val result =
            GeofenceEventFsmDetector.flipCooldownMs(
                isFast = false,
                stationary = false
            )

        assertTrue(result == WALKING_COOLDOWN)
    }

    @Test
    fun `movimiento rapido usa cooldown de 5 segundos`() {
        val result =
            GeofenceEventFsmDetector.flipCooldownMs(
                isFast = true,
                stationary = false
            )

        assertTrue(result == FAST_COOLDOWN)
    }

    @Test
    fun `estacionario usa cooldown de 8 minutos`() {
        val result =
            GeofenceEventFsmDetector.flipCooldownMs(
                isFast = false,
                stationary = true
            )

        assertTrue(result == STATIONARY_COOLDOWN)
    }

    @Test
    fun `stationary tiene prioridad sobre isFast`() {
        val result =
            GeofenceEventFsmDetector.flipCooldownMs(
                isFast = true,
                stationary = true
            )

        assertTrue(result == STATIONARY_COOLDOWN)
    }


    /*
     * ============================================================
     * 2. CAMINANDO - COOLDOWN 10 SEGUNDOS
     * ============================================================
     */

    @Test
    fun `caminando antes de 10 segundos bloquea ENTER`() = runBlocking {

        val area = area(1001L)

        prepareLastFlip(
            areaId = area.id_area,
            elapsed = 9_999L
        )

        val result =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context = context,
                area = area,
                now = NOW,
                isFast = false,
                stationary = false,
                candidate = Definition.EVT_ENTER,
                m = metrics(distance = 50f),
                meterForExit = 120f
            )

        assertTrue(result)
    }

    @Test
    fun `caminando exactamente a 10 segundos no bloquea ENTER`() = runBlocking {

        val area = area(1002L)

        prepareLastFlip(
            areaId = area.id_area,
            elapsed = 10_000L
        )

        val result =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context,
                area,
                NOW,
                false,
                false,
                Definition.EVT_ENTER,
                metrics(50f),
                120f
            )

        assertFalse(result)
    }

    @Test
    fun `caminando despues de 10 segundos no bloquea ENTER`() = runBlocking {

        val area = area(1003L)

        prepareLastFlip(
            areaId = area.id_area,
            elapsed = 10_001L
        )

        val result =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context,
                area,
                NOW,
                false,
                false,
                Definition.EVT_ENTER,
                metrics(50f),
                120f
            )

        assertFalse(result)
    }


    /*
     * ============================================================
     * 3. MOVIMIENTO RAPIDO - COOLDOWN 5 SEGUNDOS
     * ============================================================
     */

    @Test
    fun `rapido antes de 5 segundos bloquea ENTER`() = runBlocking {

        val area = area(2001L)

        prepareLastFlip(area.id_area, 4_999L)

        val result =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context,
                area,
                NOW,
                true,
                false,
                Definition.EVT_ENTER,
                metrics(50f),
                120f
            )

        assertTrue(result)
    }

    @Test
    fun `rapido exactamente a 5 segundos no bloquea ENTER`() = runBlocking {

        val area = area(2002L)

        prepareLastFlip(area.id_area, 5_000L)

        val result =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context,
                area,
                NOW,
                true,
                false,
                Definition.EVT_ENTER,
                metrics(50f),
                120f
            )

        assertFalse(result)
    }

    @Test
    fun `rapido despues de 5 segundos no bloquea ENTER`() = runBlocking {

        val area = area(2003L)

        prepareLastFlip(area.id_area, 5_001L)

        val result =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context,
                area,
                NOW,
                true,
                false,
                Definition.EVT_ENTER,
                metrics(50f),
                120f
            )

        assertFalse(result)
    }


    /*
     * ============================================================
     * 4. ESTACIONARIO - COOLDOWN 8 MINUTOS
     * ============================================================
     */

    @Test
    fun `estacionario antes de 8 minutos bloquea ENTER`() = runBlocking {

        val area = area(3001L)

        prepareLastFlip(area.id_area, 479_999L)

        val result =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context,
                area,
                NOW,
                false,
                true,
                Definition.EVT_ENTER,
                metrics(50f),
                120f
            )

        assertTrue(result)
    }

    @Test
    fun `estacionario exactamente a 8 minutos no bloquea ENTER`() = runBlocking {

        val area = area(3002L)

        prepareLastFlip(area.id_area, 480_000L)

        val result =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context,
                area,
                NOW,
                false,
                true,
                Definition.EVT_ENTER,
                metrics(50f),
                120f
            )

        assertFalse(result)
    }

    @Test
    fun `estacionario despues de 8 minutos no bloquea ENTER`() = runBlocking {

        val area = area(3003L)

        prepareLastFlip(area.id_area, 480_001L)

        val result =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context,
                area,
                NOW,
                false,
                true,
                Definition.EVT_ENTER,
                metrics(50f),
                120f
            )

        assertFalse(result)
    }


    /*
     * ============================================================
     * 5. FAR OUTSIDE - DOMINA RADIO
     *
     * meterForExit = 120
     * radius = 100
     * accuracy = 10
     *
     * max(
     *   accuracy * 1.5 = 15,
     *   radius * 0.5 = 50,
     *   10
     * ) = 50
     *
     * farOutside = distance >= 170
     * ============================================================
     */

    @Test
    fun `EXIT a 169_99 durante cooldown queda bloqueado`() = runBlocking {

        val area = area(4001L)

        prepareLastFlip(area.id_area, 1_000L)

        val result =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context,
                area,
                NOW,
                false,
                false,
                Definition.EVT_EXIT,
                metrics(
                    distance = 169.99f,
                    radius = 100f,
                    accuracy = 10f
                ),
                120f
            )

        assertTrue(result)
    }

    @Test
    fun `EXIT exactamente a 170 durante cooldown usa farOutside y no bloquea`() = runBlocking {

        val area = area(4002L)

        prepareLastFlip(area.id_area, 1_000L)

        val result =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context,
                area,
                NOW,
                false,
                false,
                Definition.EVT_EXIT,
                metrics(
                    distance = 170f,
                    radius = 100f,
                    accuracy = 10f
                ),
                120f
            )

        assertFalse(result)
    }

    @Test
    fun `EXIT mayor a 170 durante cooldown usa farOutside y no bloquea`() = runBlocking {

        val area = area(4003L)

        prepareLastFlip(area.id_area, 1_000L)

        val result =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context,
                area,
                NOW,
                false,
                false,
                Definition.EVT_EXIT,
                metrics(
                    distance = 170.01f,
                    radius = 100f,
                    accuracy = 10f
                ),
                120f
            )

        assertFalse(result)
    }


    /*
     * ============================================================
     * 6. FAR OUTSIDE - DOMINA ACCURACY
     *
     * meterForExit = 120
     * accuracy = 40  -> 60
     * radius = 100   -> 50
     *
     * limite = 180
     * ============================================================
     */

    @Test
    fun `accuracy dominante justo antes del farOutside bloquea`() = runBlocking {

        val area = area(5001L)

        prepareLastFlip(area.id_area, 1_000L)

        val result =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context,
                area,
                NOW,
                false,
                false,
                Definition.EVT_EXIT,
                metrics(
                    distance = 179.99f,
                    radius = 100f,
                    accuracy = 40f
                ),
                120f
            )

        assertTrue(result)
    }

    @Test
    fun `accuracy dominante exactamente en farOutside no bloquea`() = runBlocking {

        val area = area(5002L)

        prepareLastFlip(area.id_area, 1_000L)

        val result =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context,
                area,
                NOW,
                false,
                false,
                Definition.EVT_EXIT,
                metrics(
                    distance = 180f,
                    radius = 100f,
                    accuracy = 40f
                ),
                120f
            )

        assertFalse(result)
    }

    @Test
    fun `accuracy dominante despues de farOutside no bloquea`() = runBlocking {

        val area = area(5003L)

        prepareLastFlip(area.id_area, 1_000L)

        val result =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context,
                area,
                NOW,
                false,
                false,
                Definition.EVT_EXIT,
                metrics(
                    distance = 180.01f,
                    radius = 100f,
                    accuracy = 40f
                ),
                120f
            )

        assertFalse(result)
    }


    /*
     * ============================================================
     * 7. FAR OUTSIDE - DOMINA CONSTANTE 10
     * ============================================================
     */

    @Test
    fun `constante 10 domina y antes del limite bloquea`() = runBlocking {

        val area = area(6001L)

        prepareLastFlip(area.id_area, 1_000L)

        val result =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context,
                area,
                NOW,
                false,
                false,
                Definition.EVT_EXIT,
                metrics(
                    distance = 49.99f,
                    radius = 10f,
                    accuracy = 2f
                ),
                40f
            )

        assertTrue(result)
    }

    @Test
    fun `constante 10 domina y exactamente en limite no bloquea`() = runBlocking {

        val area = area(6002L)

        prepareLastFlip(area.id_area, 1_000L)

        val result =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context,
                area,
                NOW,
                false,
                false,
                Definition.EVT_EXIT,
                metrics(
                    distance = 50f,
                    radius = 10f,
                    accuracy = 2f
                ),
                40f
            )

        assertFalse(result)
    }

    @Test
    fun `constante 10 domina y despues del limite no bloquea`() = runBlocking {

        val area = area(6003L)

        prepareLastFlip(area.id_area, 1_000L)

        val result =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context,
                area,
                NOW,
                false,
                false,
                Definition.EVT_EXIT,
                metrics(
                    distance = 50.01f,
                    radius = 10f,
                    accuracy = 2f
                ),
                40f
            )

        assertFalse(result)
    }


    /*
     * ============================================================
     * 8. farOutside NO APLICA A ENTER
     * ============================================================
     */

    @Test
    fun `ENTER extremadamente lejos sigue bloqueado durante cooldown`() = runBlocking {

        val area = area(7001L)

        prepareLastFlip(area.id_area, 1_000L)

        val result =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context,
                area,
                NOW,
                false,
                false,
                Definition.EVT_ENTER,
                metrics(
                    distance = 1_000f,
                    radius = 100f,
                    accuracy = 10f
                ),
                120f
            )

        assertTrue(result)
    }


    /*
     * ============================================================
     * 9. EXIT NORMAL CUANDO TERMINO EL COOLDOWN
     * ============================================================
     */

    @Test
    fun `EXIT normal se permite cuando termino cooldown`() = runBlocking {

        val area = area(8001L)

        prepareLastFlip(area.id_area, 10_000L)

        val result =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context,
                area,
                NOW,
                false,
                false,
                Definition.EVT_EXIT,
                metrics(
                    distance = 130f,
                    radius = 100f,
                    accuracy = 10f
                ),
                120f
            )

        assertFalse(result)
    }


    /*
     * ============================================================
     * 10. SIN FLIP ANTERIOR
     * ============================================================
     */

    @Test
    fun `sin evento anterior no bloquea`() = runBlocking {

        val area = area(9001L)

        /*
         * No llamamos a acceptEvent().
         * lastFlipAt comienza en 0.
         */

        val result =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context,
                area,
                NOW,
                false,
                false,
                Definition.EVT_ENTER,
                metrics(50f),
                120f
            )

        assertFalse(result)
    }


    /*
     * ============================================================
     * 11. CASO ANOMALO - lastFlipAt EN EL FUTURO
     * ============================================================
     */

    @Test
    fun `lastFlipAt futuro actualmente bloquea`() = runBlocking {

        val area = area(10001L)

        /*
         * Guardamos un evento 1 segundo "en el futuro".
         *
         * now - lastFlipAt = -1000
         *
         * -1000 < 10000
         *
         * Por la implementación actual debe bloquear.
         */

        GeofenceTrackStore.acceptEvent(
            area.id_area,
            NOW + 1_000L,
            0f
        )

        val result =
            GeofenceEventFsmDetector.blockByFlipCooldown(
                context,
                area,
                NOW,
                false,
                false,
                Definition.EVT_ENTER,
                metrics(50f),
                120f
            )

        assertTrue(result)
    }
}