package com.example.comunicationwearmobile.utils.Helpers.Geofences.FSM.AditionalJunit

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
class GeofenceSpamVsFlipTest {

    private val initialTime = 1_000_000L

    /*
     * Se utilizan IDs diferentes para evitar que el estado almacenado
     * por un test pueda afectar a otro.
     */


    // ============================================================
    // CASO 1
    // RAPIDO - 3 segundos
    //
    // Anti-spam: bloquea (< 10 s)
    // Anti-flip: bloquea (< 5 s)
    //
    // Ambos coinciden.
    // ============================================================

    @Test
    fun rapido_3Segundos_ambosDebenBloquear() = runBlocking {

        val areaId = 3001L

        GeofenceTrackStore.acceptEvent(
            areaId,
            initialTime,
            100f
        )

        val spamBlocked =
            GeofenceTrackStore.isSpamBlocked(
                areaId,
                initialTime + 3_000L,
                10_000L
            )

        val flipBlocked =
            GeofenceTrackStore.isFlipBlocked(
                areaId,
                initialTime + 3_000L,
                5_000L
            )

        assertTrue(spamBlocked)
        assertTrue(flipBlocked)
    }


    // ============================================================
    // CASO 2 - IMPORTANTE
    // RAPIDO - exactamente 5 segundos
    //
    // Anti-spam: bloquea
    // Anti-flip: permite
    // ============================================================

    @Test
    fun rapido_5Segundos_spamBloqueaPeroFlipPermite() = runBlocking {

        val areaId = 3002L

        GeofenceTrackStore.acceptEvent(
            areaId,
            initialTime,
            100f
        )

        val spamBlocked =
            GeofenceTrackStore.isSpamBlocked(
                areaId,
                initialTime + 5_000L,
                10_000L
            )

        val flipBlocked =
            GeofenceTrackStore.isFlipBlocked(
                areaId,
                initialTime + 5_000L,
                5_000L
            )

        assertTrue(spamBlocked)
        assertFalse(flipBlocked)
    }


    // ============================================================
    // CASO 3 - MUY IMPORTANTE
    // RAPIDO - 6 segundos
    //
    // Anti-spam: bloquea
    // Anti-flip: permite
    //
    // Demuestra la interferencia.
    // ============================================================

    @Test
    fun rapido_6Segundos_spamBloqueaPeroFlipPermite() = runBlocking {

        val areaId = 3003L

        GeofenceTrackStore.acceptEvent(
            areaId,
            initialTime,
            100f
        )

        val spamBlocked =
            GeofenceTrackStore.isSpamBlocked(
                areaId,
                initialTime + 6_000L,
                10_000L
            )

        val flipBlocked =
            GeofenceTrackStore.isFlipBlocked(
                areaId,
                initialTime + 6_000L,
                5_000L
            )

        assertTrue(spamBlocked)
        assertFalse(flipBlocked)
    }


    // ============================================================
    // CASO 4
    // RAPIDO - 9.999 segundos
    //
    // Anti-spam: todavía bloquea
    // Anti-flip: ya permite
    // ============================================================

    @Test
    fun rapido_9999ms_spamBloqueaPeroFlipPermite() = runBlocking {

        val areaId = 3004L

        GeofenceTrackStore.acceptEvent(
            areaId,
            initialTime,
            100f
        )

        val spamBlocked =
            GeofenceTrackStore.isSpamBlocked(
                areaId,
                initialTime + 9_999L,
                10_000L
            )

        val flipBlocked =
            GeofenceTrackStore.isFlipBlocked(
                areaId,
                initialTime + 9_999L,
                5_000L
            )

        assertTrue(spamBlocked)
        assertFalse(flipBlocked)
    }


    // ============================================================
    // CASO 5
    // RAPIDO - exactamente 10 segundos
    //
    // Ambos permiten.
    // ============================================================

    @Test
    fun rapido_10Segundos_ambosDebenPermitir() = runBlocking {

        val areaId = 3005L

        GeofenceTrackStore.acceptEvent(
            areaId,
            initialTime,
            100f
        )

        val spamBlocked =
            GeofenceTrackStore.isSpamBlocked(
                areaId,
                initialTime + 10_000L,
                10_000L
            )

        val flipBlocked =
            GeofenceTrackStore.isFlipBlocked(
                areaId,
                initialTime + 10_000L,
                5_000L
            )

        assertFalse(spamBlocked)
        assertFalse(flipBlocked)
    }


    // ============================================================
    // CASO 6
    // CAMINANDO - 9.999 segundos
    //
    // Ambos tienen 10 segundos.
    // Ambos bloquean.
    // ============================================================

    @Test
    fun caminando_9999ms_ambosDebenBloquear() = runBlocking {

        val areaId = 3006L

        GeofenceTrackStore.acceptEvent(
            areaId,
            initialTime,
            100f
        )

        val spamBlocked =
            GeofenceTrackStore.isSpamBlocked(
                areaId,
                initialTime + 9_999L,
                10_000L
            )

        val flipBlocked =
            GeofenceTrackStore.isFlipBlocked(
                areaId,
                initialTime + 9_999L,
                10_000L
            )

        assertTrue(spamBlocked)
        assertTrue(flipBlocked)
    }


    // ============================================================
    // CASO 7
    // CAMINANDO - exactamente 10 segundos
    //
    // Ambos permiten.
    // ============================================================

    @Test
    fun caminando_10Segundos_ambosDebenPermitir() = runBlocking {

        val areaId = 3007L

        GeofenceTrackStore.acceptEvent(
            areaId,
            initialTime,
            100f
        )

        val spamBlocked =
            GeofenceTrackStore.isSpamBlocked(
                areaId,
                initialTime + 10_000L,
                10_000L
            )

        val flipBlocked =
            GeofenceTrackStore.isFlipBlocked(
                areaId,
                initialTime + 10_000L,
                10_000L
            )

        assertFalse(spamBlocked)
        assertFalse(flipBlocked)
    }


    // ============================================================
    // CASO 8
    // ESTACIONARIO - 10 segundos
    //
    // Anti-spam ya permite.
    // Anti-flip continúa bloqueando durante 8 minutos.
    //
    // Demuestra que anti-flip es más restrictivo.
    // ============================================================

    @Test
    fun estacionario_10Segundos_spamPermitePeroFlipBloquea() = runBlocking {

        val areaId = 3008L

        GeofenceTrackStore.acceptEvent(
            areaId,
            initialTime,
            100f
        )

        val spamBlocked =
            GeofenceTrackStore.isSpamBlocked(
                areaId,
                initialTime + 10_000L,
                10_000L
            )

        val flipBlocked =
            GeofenceTrackStore.isFlipBlocked(
                areaId,
                initialTime + 10_000L,
                480_000L
            )

        assertFalse(spamBlocked)
        assertTrue(flipBlocked)
    }


    // ============================================================
    // CASO 9
    // ESTACIONARIO - 1 minuto
    //
    // Anti-spam permite.
    // Anti-flip bloquea.
    // ============================================================

    @Test
    fun estacionario_1Minuto_spamPermitePeroFlipBloquea() = runBlocking {

        val areaId = 3009L

        GeofenceTrackStore.acceptEvent(
            areaId,
            initialTime,
            100f
        )

        val spamBlocked =
            GeofenceTrackStore.isSpamBlocked(
                areaId,
                initialTime + 60_000L,
                10_000L
            )

        val flipBlocked =
            GeofenceTrackStore.isFlipBlocked(
                areaId,
                initialTime + 60_000L,
                480_000L
            )

        assertFalse(spamBlocked)
        assertTrue(flipBlocked)
    }


    // ============================================================
    // CASO 10
    // ESTACIONARIO - exactamente 8 minutos
    //
    // Ambos permiten.
    // ============================================================

    @Test
    fun estacionario_8Minutos_ambosDebenPermitir() = runBlocking {

        val areaId = 3010L

        GeofenceTrackStore.acceptEvent(
            areaId,
            initialTime,
            100f
        )

        val spamBlocked =
            GeofenceTrackStore.isSpamBlocked(
                areaId,
                initialTime + 480_000L,
                10_000L
            )

        val flipBlocked =
            GeofenceTrackStore.isFlipBlocked(
                areaId,
                initialTime + 480_000L,
                480_000L
            )

        assertFalse(spamBlocked)
        assertFalse(flipBlocked)
    }
}