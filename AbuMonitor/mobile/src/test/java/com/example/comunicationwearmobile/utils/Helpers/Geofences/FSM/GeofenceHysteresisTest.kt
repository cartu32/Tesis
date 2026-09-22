package com.example.comunicationwearmobile.utils.Helpers.Geofences.FSM

import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.FSM.GeofenceEventFsmDetector.calculateHysteris
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeofenceHysteresisTest {

    companion object {
        private const val DELTA = 0.0001f
    }



    /**
     * Prueba todas las combinaciones:
     *
     * Radio:    30..300 metros
     * Accuracy: 0..500 metros
     *
     * Verifica las propiedades fundamentales de la histéresis.
     */
    @Test
    fun testAllPossibleCombinations() {

        for (radius in 30..300) {

            for (accuracy in 0..500) {

                val (enter, exit) = calculateHysteris(
                    accuracy.toFloat(),
                    radius.toFloat()
                )

                // ENTER siempre debe estar dentro del radio.
                assertTrue(
                    "ENTER inválido: radius=$radius accuracy=$accuracy enter=$enter",
                    enter < radius
                )

                // EXIT siempre debe estar fuera del radio.
                assertTrue(
                    "EXIT inválido: radius=$radius accuracy=$accuracy exit=$exit",
                    exit > radius
                )

                // ENTER siempre debe ser menor que EXIT.
                assertTrue(
                    "Histéresis inválida: radius=$radius accuracy=$accuracy enter=$enter exit=$exit",
                    enter < exit
                )

                // Límites máximos permitidos por el algoritmo.
                assertTrue(
                    "ENTER fuera del rango esperado",
                    enter >= radius * 0.6f - DELTA
                )

                assertTrue(
                    "ENTER fuera del rango esperado",
                    enter <= radius * 0.8f + DELTA
                )

                assertTrue(
                    "EXIT fuera del rango esperado",
                    exit >= radius * 1.2f - DELTA
                )

                assertTrue(
                    "EXIT fuera del rango esperado",
                    exit <= radius * 1.4f + DELTA
                )
            }
        }
    }

    /**
     * Comprueba que accuracy = 0 produce exactamente
     * los factores base 0.8 y 1.2.
     */
    @Test
    fun testAccuracyZero() {

        for (radius in 30..300) {

            val (enter, exit) = calculateHysteris(
                0f,
                radius.toFloat()
            )

            assertEquals(radius * 0.8f, enter, DELTA)
            assertEquals(radius * 1.2f, exit, DELTA)
        }
    }

    /**
     * Comprueba exactamente el límite del 20 %.
     */
    @Test
    fun testAccuracyExactlyTwentyPercent() {

        for (radius in 30..300) {

            val accuracy = radius * 0.2f

            val (enter, exit) = calculateHysteris(
                accuracy,
                radius.toFloat()
            )

            assertEquals(radius * 0.6f, enter, DELTA)
            assertEquals(radius * 1.4f, exit, DELTA)
        }
    }

    /**
     * Comprueba que superar el 20 % de accuracy
     * no continúa aumentando la histéresis.
     */
    @Test
    fun testAccuracyGreaterThanTwentyPercent() {

        for (radius in 30..300) {

            for (accuracy in radius..500) {

                val (enter, exit) = calculateHysteris(
                    accuracy.toFloat(),
                    radius.toFloat()
                )

                assertEquals(radius * 0.6f, enter, DELTA)
                assertEquals(radius * 1.4f, exit, DELTA)
            }
        }
    }

    /**
     * Comprueba matemáticamente el resultado esperado
     * para cada combinación.
     */
    @Test
    fun testExpectedFormulaForAllCombinations() {

        for (radius in 30..300) {

            for (accuracy in 0..500) {

                val radiusFloat = radius.toFloat()
                val accuracyFloat = accuracy.toFloat()

                val expectedExtra =
                    (accuracyFloat / radiusFloat).coerceAtMost(0.2f)

                val expectedEnter =
                    radiusFloat * (0.8f - expectedExtra)

                val expectedExit =
                    radiusFloat * (1.2f + expectedExtra)

                val (actualEnter, actualExit) =
                    calculateHysteris(accuracyFloat, radiusFloat)

                assertEquals(
                    "ENTER incorrecto: radius=$radius accuracy=$accuracy",
                    expectedEnter,
                    actualEnter,
                    DELTA
                )

                assertEquals(
                    "EXIT incorrecto: radius=$radius accuracy=$accuracy",
                    expectedExit,
                    actualExit,
                    DELTA
                )
            }
        }
    }
}