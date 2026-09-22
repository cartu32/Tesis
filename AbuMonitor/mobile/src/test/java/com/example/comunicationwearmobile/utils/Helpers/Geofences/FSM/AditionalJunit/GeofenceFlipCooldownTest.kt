package com.example.comunicationwearmobile.utils.Helpers.Geofences.FSM.AditionalJunit

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
class GeofenceFlipCooldownTest {

    private val areaId = 1001L
    private val initialTime = 1_000_000L


    // ============================================================
    // RAPIDO - COOLDOWN 5 SEGUNDOS
    // ============================================================

    @Test
    fun rapido_antesDe5Segundos_debeBloquear() = runBlocking {

        GeofenceTrackStore.acceptEvent(
            areaId, initialTime, 50f
        )

        val blocked = GeofenceTrackStore.isFlipBlocked(
            areaId, initialTime + 4_999L, 5_000L
        )

        assertTrue(blocked)
    }


    @Test
    fun rapido_exactamente5Segundos_debePermitir() = runBlocking {

        GeofenceTrackStore.acceptEvent(
            areaId, initialTime, 50f
        )

        val blocked = GeofenceTrackStore.isFlipBlocked(
            areaId, initialTime + 5_000L, 5_000L
        )

        assertFalse(blocked)
    }


    @Test
    fun rapido_despuesDe5Segundos_debePermitir() = runBlocking {

        GeofenceTrackStore.acceptEvent(
            areaId, initialTime, 50f
        )

        val blocked = GeofenceTrackStore.isFlipBlocked(
            areaId, initialTime + 5_001L, 5_000L
        )

        assertFalse(blocked)
    }


    // ============================================================
    // CAMINANDO/NORMAL - COOLDOWN 10 SEGUNDOS
    // ============================================================

    @Test
    fun caminando_antesDe10Segundos_debeBloquear() = runBlocking {

        GeofenceTrackStore.acceptEvent(
            areaId, initialTime, 50f
        )

        val blocked = GeofenceTrackStore.isFlipBlocked(
            areaId, initialTime + 9_999L, 10_000L
        )

        assertTrue(blocked)
    }


    @Test
    fun caminando_exactamente10Segundos_debePermitir() = runBlocking {

        GeofenceTrackStore.acceptEvent(
            areaId, initialTime, 50f
        )

        val blocked = GeofenceTrackStore.isFlipBlocked(
            areaId, initialTime + 10_000L, 10_000L
        )

        assertFalse(blocked)
    }


    @Test
    fun caminando_despuesDe10Segundos_debePermitir() = runBlocking {

        GeofenceTrackStore.acceptEvent(
            areaId, initialTime, 50f
        )

        val blocked = GeofenceTrackStore.isFlipBlocked(
            areaId, initialTime + 10_001L, 10_000L
        )

        assertFalse(blocked)
    }


    // ============================================================
    // ESTACIONARIO - COOLDOWN 8 MINUTOS
    // ============================================================

    @Test
    fun estacionario_antesDe8Minutos_debeBloquear() = runBlocking {

        GeofenceTrackStore.acceptEvent(
            areaId, initialTime, 50f
        )

        val blocked = GeofenceTrackStore.isFlipBlocked(
            areaId, initialTime + 479_999L, 480_000L
        )

        assertTrue(blocked)
    }


    @Test
    fun estacionario_exactamente8Minutos_debePermitir() = runBlocking {

        GeofenceTrackStore.acceptEvent(
            areaId, initialTime, 50f
        )

        val blocked = GeofenceTrackStore.isFlipBlocked(
            areaId, initialTime + 480_000L, 480_000L
        )

        assertFalse(blocked)
    }


    @Test
    fun estacionario_despuesDe8Minutos_debePermitir() = runBlocking {

        GeofenceTrackStore.acceptEvent(
            areaId, initialTime, 50f
        )

        val blocked = GeofenceTrackStore.isFlipBlocked(
            areaId, initialTime + 480_001L, 480_000L
        )

        assertFalse(blocked)
    }


    // ============================================================
    // PRUEBA DE LOS VALORES DEVUELTOS POR flipCooldownMs
    // ============================================================

    @Test
    fun cooldownRapido_debeSer5Segundos() {

        val cooldown = GeofenceEventFsmDetector.flipCooldownMs(
            isFast = true, stationary = false
        )

        assertTrue(cooldown == 5_000L)
    }


    @Test
    fun cooldownCaminando_debeSer10Segundos() {

        val cooldown = GeofenceEventFsmDetector.flipCooldownMs(
            isFast = false, stationary = false
        )

        assertTrue(cooldown == 10_000L)
    }


    @Test
    fun cooldownEstacionario_debeSer8Minutos() {

        val cooldown = GeofenceEventFsmDetector.flipCooldownMs(
            isFast = false, stationary = true
        )

        assertTrue(cooldown == 480_000L)
    }


    @Test
    fun estacionarioTienePrioridadSobreRapido() {

        val cooldown = GeofenceEventFsmDetector.flipCooldownMs(
            isFast = true, stationary = true
        )

        assertTrue(cooldown == 480_000L)
    }
}