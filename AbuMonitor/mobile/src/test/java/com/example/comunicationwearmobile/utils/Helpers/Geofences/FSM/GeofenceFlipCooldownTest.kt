package com.example.comunicationwearmobile.utils.Helpers.Geofences.FSM

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.comunicationwearmobile.ui.model.extra.Metrics
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.FSM.GeofenceEventFsmDetector
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.FSM.GeofenceTrackStore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class GeofenceFlipCooldownTest {

    private lateinit var context: Context
    private lateinit var area: EntityAreaGeofence

    private val areaId = 99999L

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        area = EntityAreaGeofence()
        area.id_area = areaId
    }

    // ============================================================
    // 1. SELECCION DEL COOLDOWN
    // ============================================================

    @Test
    fun `stationary true e isFast false devuelve 8 minutos`() {
        val result = GeofenceEventFsmDetector.flipCooldownMs(
            isFast = false,
            stationary = true
        )

        assertEquals(480_000L, result)
    }

    @Test
    fun `stationary true e isFast true tiene prioridad stationary`() {
        val result = GeofenceEventFsmDetector.flipCooldownMs(
            isFast = true,
            stationary = true
        )

        assertEquals(480_000L, result)
    }

    @Test
    fun `stationary false e isFast true devuelve 5 segundos`() {
        val result = GeofenceEventFsmDetector.flipCooldownMs(
            isFast = true,
            stationary = false
        )

        assertEquals(5_000L, result)
    }

    @Test
    fun `stationary false e isFast false devuelve 10 segundos`() {
        val result = GeofenceEventFsmDetector.flipCooldownMs(
            isFast = false,
            stationary = false
        )

        assertEquals(10_000L, result)
    }

    // ============================================================
    // 2. COOLDOWN ESTACIONARIO - 8 MINUTOS
    // ============================================================

    @Test
    fun `stationary bloquea inmediatamente despues del ultimo evento`() = runBlocking {

        val lastFlip = 1_000_000L
        GeofenceTrackStore.acceptEvent(areaId, lastFlip, 50f)

        val blocked = callFilter(
            now = lastFlip,
            stationary = true,
            isFast = false,
            candidate = Definition.EVT_ENTER
        )

        assertEquals(true, blocked)
    }

    @Test
    fun `stationary bloquea un milisegundo antes de 8 minutos`() = runBlocking {

        val lastFlip = 1_000_000L
        GeofenceTrackStore.acceptEvent(areaId, lastFlip, 50f)

        val blocked = callFilter(
            now = lastFlip + 479_999L,
            stationary = true,
            isFast = false,
            candidate = Definition.EVT_ENTER
        )

        assertEquals(true, blocked)
    }

    @Test
    fun `stationary permite exactamente a los 8 minutos`() = runBlocking {

        val lastFlip = 1_000_000L
        GeofenceTrackStore.acceptEvent(areaId, lastFlip, 50f)

        val blocked = callFilter(
            now = lastFlip + 480_000L,
            stationary = true,
            isFast = false,
            candidate = Definition.EVT_ENTER
        )

        assertEquals(false, blocked)
    }

    @Test
    fun `stationary permite despues de 8 minutos`() = runBlocking {

        val lastFlip = 1_000_000L
        GeofenceTrackStore.acceptEvent(areaId, lastFlip, 50f)

        val blocked = callFilter(
            now = lastFlip + 480_001L,
            stationary = true,
            isFast = false,
            candidate = Definition.EVT_ENTER
        )

        assertEquals(false, blocked)
    }

    // ============================================================
    // 3. MOVIMIENTO RAPIDO - 5 SEGUNDOS
    // ============================================================

    @Test
    fun `movimiento rapido bloquea antes de 5 segundos`() = runBlocking {

        val lastFlip = 1_000_000L
        GeofenceTrackStore.acceptEvent(areaId, lastFlip, 50f)

        val blocked = callFilter(
            now = lastFlip + 4_999L,
            stationary = false,
            isFast = true,
            candidate = Definition.EVT_ENTER
        )

        assertEquals(true, blocked)
    }

    @Test
    fun `movimiento rapido permite exactamente a los 5 segundos`() = runBlocking {

        val lastFlip = 1_000_000L
        GeofenceTrackStore.acceptEvent(areaId, lastFlip, 50f)

        val blocked = callFilter(
            now = lastFlip + 5_000L,
            stationary = false,
            isFast = true,
            candidate = Definition.EVT_ENTER
        )

        assertEquals(false, blocked)
    }

    @Test
    fun `movimiento rapido permite despues de 5 segundos`() = runBlocking {

        val lastFlip = 1_000_000L
        GeofenceTrackStore.acceptEvent(areaId, lastFlip, 50f)

        val blocked = callFilter(
            now = lastFlip + 5_001L,
            stationary = false,
            isFast = true,
            candidate = Definition.EVT_ENTER
        )

        assertEquals(false, blocked)
    }

    // ============================================================
    // 4. MOVIMIENTO NORMAL - 10 SEGUNDOS
    // ============================================================

    @Test
    fun `movimiento normal bloquea antes de 10 segundos`() = runBlocking {

        val lastFlip = 1_000_000L
        GeofenceTrackStore.acceptEvent(areaId, lastFlip, 50f)

        val blocked = callFilter(
            now = lastFlip + 9_999L,
            stationary = false,
            isFast = false,
            candidate = Definition.EVT_ENTER
        )

        assertEquals(true, blocked)
    }

    @Test
    fun `movimiento normal permite exactamente a los 10 segundos`() = runBlocking {

        val lastFlip = 1_000_000L
        GeofenceTrackStore.acceptEvent(areaId, lastFlip, 50f)

        val blocked = callFilter(
            now = lastFlip + 10_000L,
            stationary = false,
            isFast = false,
            candidate = Definition.EVT_ENTER
        )

        assertEquals(false, blocked)
    }

    @Test
    fun `movimiento normal permite despues de 10 segundos`() = runBlocking {

        val lastFlip = 1_000_000L
        GeofenceTrackStore.acceptEvent(areaId, lastFlip, 50f)

        val blocked = callFilter(
            now = lastFlip + 10_001L,
            stationary = false,
            isFast = false,
            candidate = Definition.EVT_ENTER
        )

        assertEquals(false, blocked)
    }

    // ============================================================
    // 5. PRIORIDAD DE STATIONARY SOBRE ISFAST
    // ============================================================

    @Test
    fun `stationary tiene prioridad aunque isFast sea true`() = runBlocking {

        val lastFlip = 1_000_000L
        GeofenceTrackStore.acceptEvent(areaId, lastFlip, 50f)

        // Pasaron 6 segundos.
        // Si solamente se considerara isFast, ya estaría liberado.
        // Pero stationary tiene prioridad y mantiene los 8 minutos.
        val blocked = callFilter(
            now = lastFlip + 6_000L,
            stationary = true,
            isFast = true,
            candidate = Definition.EVT_ENTER
        )

        assertEquals(true, blocked)
    }

    @Test
    fun `stationary e isFast permite exactamente a los 8 minutos`() = runBlocking {

        val lastFlip = 1_000_000L
        GeofenceTrackStore.acceptEvent(areaId, lastFlip, 50f)

        val blocked = callFilter(
            now = lastFlip + 480_000L,
            stationary = true,
            isFast = true,
            candidate = Definition.EVT_ENTER
        )

        assertEquals(false, blocked)
    }

    // ============================================================
    // 6. FAR OUTSIDE
    //
    // radius = 100
    // accuracy = 10
    // meterForExit = 120
    //
    // margenExtra =
    // max(accuracy * 1.5, radius * 0.5, 10)
    // max(15, 50, 10) = 50
    //
    // farOutside = 120 + 50 = 170 metros
    // ============================================================

    @Test
    fun `EXIT debajo de farOutside permanece bloqueado`() = runBlocking {

        val lastFlip = 1_000_000L
        GeofenceTrackStore.acceptEvent(areaId, lastFlip, 50f)

        val blocked = callFilter(
            now = lastFlip + 1_000L,
            stationary = true,
            isFast = false,
            candidate = Definition.EVT_EXIT,
            distance = 169.99f
        )

        assertEquals(true, blocked)
    }

    @Test
    fun `EXIT exactamente en farOutside ignora cooldown`() = runBlocking {

        val lastFlip = 1_000_000L
        GeofenceTrackStore.acceptEvent(areaId, lastFlip, 50f)

        val blocked = callFilter(
            now = lastFlip + 1_000L,
            stationary = true,
            isFast = false,
            candidate = Definition.EVT_EXIT,
            distance = 170f
        )

        assertEquals(false, blocked)
    }

    @Test
    fun `EXIT por encima de farOutside ignora cooldown`() = runBlocking {

        val lastFlip = 1_000_000L
        GeofenceTrackStore.acceptEvent(areaId, lastFlip, 50f)

        val blocked = callFilter(
            now = lastFlip + 1_000L,
            stationary = true,
            isFast = false,
            candidate = Definition.EVT_EXIT,
            distance = 200f
        )

        assertEquals(false, blocked)
    }

    // ============================================================
    // 7. FAR OUTSIDE SOLO FUNCIONA PARA EXIT
    // ============================================================

    @Test
    fun `ENTER muy lejos no utiliza excepcion farOutside`() = runBlocking {

        val lastFlip = 1_000_000L
        GeofenceTrackStore.acceptEvent(areaId, lastFlip, 50f)

        val blocked = callFilter(
            now = lastFlip + 1_000L,
            stationary = true,
            isFast = false,
            candidate = Definition.EVT_ENTER,
            distance = 500f
        )

        assertEquals(true, blocked)
    }

    // ============================================================
    // 8. ACCURACY DOMINA EL MARGEN FAROUTSIDE
    //
    // radius = 100 -> radius * 0.5 = 50
    // accuracy = 50 -> accuracy * 1.5 = 75
    //
    // max(75, 50, 10) = 75
    //
    // meterForExit = 120
    // farOutside = 120 + 75 = 195 metros
    // ============================================================

    @Test
    fun `accuracy alto aumenta distancia necesaria para farOutside`() = runBlocking {

        val lastFlip = 1_000_000L
        GeofenceTrackStore.acceptEvent(areaId, lastFlip, 50f)

        val blocked = callFilter(
            now = lastFlip + 1_000L,
            stationary = true,
            isFast = false,
            candidate = Definition.EVT_EXIT,
            distance = 194.99f,
            accuracy = 50f
        )

        assertEquals(true, blocked)
    }

    @Test
    fun `accuracy alto permite farOutside al alcanzar nuevo limite`() = runBlocking {

        val lastFlip = 1_000_000L
        GeofenceTrackStore.acceptEvent(areaId, lastFlip, 50f)

        val blocked = callFilter(
            now = lastFlip + 1_000L,
            stationary = true,
            isFast = false,
            candidate = Definition.EVT_EXIT,
            distance = 195f,
            accuracy = 50f
        )

        assertEquals(false, blocked)
    }

    // ============================================================
    // 9. FAROUTSIDE NO IMPORTA SI EL COOLDOWN YA TERMINO
    // ============================================================

    @Test
    fun `EXIT se permite al terminar cooldown aunque no sea farOutside`() = runBlocking {

        val lastFlip = 1_000_000L
        GeofenceTrackStore.acceptEvent(areaId, lastFlip, 50f)

        val blocked = callFilter(
            now = lastFlip + 480_000L,
            stationary = true,
            isFast = false,
            candidate = Definition.EVT_EXIT,
            distance = 130f
        )

        assertEquals(false, blocked)
    }

    // ============================================================
    // FUNCION AUXILIAR
    // ============================================================

    private suspend fun callFilter(
        now: Long,
        stationary: Boolean,
        isFast: Boolean,
        candidate: String,
        distance: Float = 100f,
        radius: Float = 100f,
        accuracy: Float = 10f,
        meterForExit: Float = 120f
    ): Boolean {

        val metrics = Metrics(
            distance = distance,
            radiusMeters = radius,
            accuracy = accuracy,
            distanceToBorder = kotlin.math.abs(distance - radius)
        )

        return GeofenceEventFsmDetector.blockByFlipCooldown(
            context = context,
            area = area,
            now = now,
            isFast = isFast,
            stationary = stationary,
            candidate = candidate,
            m = metrics,
            meterForExit = meterForExit
        )
    }
}