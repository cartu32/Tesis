package com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.FSM

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.FSM.GeofenceEventFsmDetector.isInGrayZone
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class GeofenceGrayZoneTest {

    private val context: Context =
        ApplicationProvider.getApplicationContext()

    /*
     * ============================================================
     * ACCURACY BAJO
     * accuracy <= 8.57 m -> borderMargin = 3 m
     * ============================================================
     */

    @Test
    fun accuracyBajo_distanciaMenor3_debeEstarEnZonaGris() {
        assertTrue(
            isInGrayZone(
                context = context,
                radiusMeters = 30f,
                accuracy = 5f,
                distanceToBorder = 2.99f
            )
        )
    }

    @Test
    fun accuracyBajo_distanciaIgual3_debeEstarEnZonaGris() {
        assertTrue(
            isInGrayZone(
                context = context,
                radiusMeters = 30f,
                accuracy = 5f,
                distanceToBorder = 3f
            )
        )
    }

    @Test
    fun accuracyBajo_distanciaMayor3_noDebeEstarEnZonaGris() {
        assertFalse(
            isInGrayZone(
                context = context,
                radiusMeters = 30f,
                accuracy = 5f,
                distanceToBorder = 3.01f
            )
        )
    }

    /*
     * ============================================================
     * ACCURACY INTERMEDIO
     * accuracy = 10 m -> borderMargin = 3.5 m
     * ============================================================
     */

    @Test
    fun accuracy10_distanciaMenorMargen_debeEstarEnZonaGris() {
        assertTrue(
            isInGrayZone(
                context = context,
                radiusMeters = 30f,
                accuracy = 10f,
                distanceToBorder = 3.49f
            )
        )
    }

    @Test
    fun accuracy10_distanciaIgualMargen_debeEstarEnZonaGris() {
        assertTrue(
            isInGrayZone(
                context = context,
                radiusMeters = 30f,
                accuracy = 10f,
                distanceToBorder = 3.5f
            )
        )
    }

    @Test
    fun accuracy10_distanciaMayorMargen_noDebeEstarEnZonaGris() {
        assertFalse(
            isInGrayZone(
                context = context,
                radiusMeters = 30f,
                accuracy = 10f,
                distanceToBorder = 3.51f
            )
        )
    }

    /*
     * ============================================================
     * ACCURACY ALTO
     * accuracy >= 17.14 m -> borderMargin = 6 m
     * ============================================================
     */

    @Test
    fun accuracyAlto_distanciaMenor6_debeEstarEnZonaGris() {
        assertTrue(
            isInGrayZone(
                context = context,
                radiusMeters = 30f,
                accuracy = 20f,
                distanceToBorder = 5.99f
            )
        )
    }

    @Test
    fun accuracyAlto_distanciaIgual6_debeEstarEnZonaGris() {
        assertTrue(
            isInGrayZone(
                context = context,
                radiusMeters = 30f,
                accuracy = 20f,
                distanceToBorder = 6f
            )
        )
    }

    @Test
    fun accuracyAlto_distanciaMayor6_noDebeEstarEnZonaGris() {
        assertFalse(
            isInGrayZone(
                context = context,
                radiusMeters = 30f,
                accuracy = 20f,
                distanceToBorder = 6.01f
            )
        )
    }

    /*
     * ============================================================
     * RADIO MÍNIMO: 30 m
     * ============================================================
     */

    @Test
    fun radioMinimo30_funcionaCorrectamente() {
        assertTrue(
            isInGrayZone(
                context = context,
                radiusMeters = 30f,
                accuracy = 10f,
                distanceToBorder = 3.5f
            )
        )

        assertFalse(
            isInGrayZone(
                context = context,
                radiusMeters = 30f,
                accuracy = 10f,
                distanceToBorder = 3.51f
            )
        )
    }

    /*
     * ============================================================
     * RADIO MÁXIMO: 300 m
     * ============================================================
     */

    @Test
    fun radioMaximo300_funcionaCorrectamente() {
        assertTrue(
            isInGrayZone(
                context = context,
                radiusMeters = 300f,
                accuracy = 10f,
                distanceToBorder = 3.5f
            )
        )

        assertFalse(
            isInGrayZone(
                context = context,
                radiusMeters = 300f,
                accuracy = 10f,
                distanceToBorder = 3.51f
            )
        )
    }

    /*
     * ============================================================
     * TODOS LOS RADIOS 30..300
     *
     * Para accuracy = 10:
     * borderMargin = 3.5 m para TODOS los radios permitidos.
     * ============================================================
     */

    @Test
    fun todosLosRadios30a300_debenComportarseIgual() {

        for (radius in 30..300) {

            assertTrue(
                "Falló radio=$radius con distanceToBorder=3.5",
                isInGrayZone(
                    context = context,
                    radiusMeters = radius.toFloat(),
                    accuracy = 10f,
                    distanceToBorder = 3.5f
                )
            )

            assertFalse(
                "Falló radio=$radius con distanceToBorder=3.51",
                isInGrayZone(
                    context = context,
                    radiusMeters = radius.toFloat(),
                    accuracy = 10f,
                    distanceToBorder = 3.51f
                )
            )
        }
    }

    /*
     * ============================================================
     * PRUEBA AMPLIA DE COMBINACIONES
     * ============================================================
     */

    @Test
    fun probarCombinacionesRadiosAccuracyYDistancia() {

        val accuracies = listOf(
            0f,
            1f,
            5f,
            8f,
            8.57f,
            9f,
            10f,
            12f,
            15f,
            17f,
            17.14f,
            18f,
            20f,
            25f,
            35f
        )

        for (radius in 30..300) {

            for (accuracy in accuracies) {

                val expectedMargin =
                    minOf(
                        maxOf(accuracy * 0.35f, 3f),
                        6f
                    )

                assertTrue(
                    "Falló: radio=$radius accuracy=$accuracy " +
                            "distance=$expectedMargin",
                    isInGrayZone(
                        context = context,
                        radiusMeters = radius.toFloat(),
                        accuracy = accuracy,
                        distanceToBorder = expectedMargin
                    )
                )

                assertTrue(
                    "Falló dentro del margen: radio=$radius accuracy=$accuracy",
                    isInGrayZone(
                        context = context,
                        radiusMeters = radius.toFloat(),
                        accuracy = accuracy,
                        distanceToBorder = expectedMargin - 0.01f
                    )
                )

                assertFalse(
                    "Falló fuera del margen: radio=$radius accuracy=$accuracy",
                    isInGrayZone(
                        context = context,
                        radiusMeters = radius.toFloat(),
                        accuracy = accuracy,
                        distanceToBorder = expectedMargin + 0.01f
                    )
                )
            }
        }
    }

}