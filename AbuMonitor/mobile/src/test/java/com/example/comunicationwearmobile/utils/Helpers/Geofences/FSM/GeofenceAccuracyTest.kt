import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.FSM.GeofenceEventFsmDetector.ABSOLUTE_LIMIT
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.FSM.GeofenceEventFsmDetector.RADIUS_PERCENTAGE_FOR_ACCURACY
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.FSM.GeofenceEventFsmDetector.isAccuracyTooBad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeofenceAccuracyTest {

    // ============================================================
    // RADIO MENOR A 25 m
    // Se utiliza solamente el límite relativo: radio * 0.60
    // ============================================================

    @Test
    fun radio10_accuracyMenorAlLimite_noBloquea() {
        // ratioLimit = 10 * 0.60 = 6
        assertFalse(isAccuracyTooBad(5f, 10f))
    }

    @Test
    fun radio10_accuracyIgualAlLimite_noBloquea() {
        // ratioLimit = 6
        assertFalse(isAccuracyTooBad(6f, 10f))
    }

    @Test
    fun radio10_accuracyMayorAlLimite_bloquea() {
        // ratioLimit = 6
        assertTrue(isAccuracyTooBad(6.1f, 10f))
    }


    // ============================================================
    // RADIO EXACTAMENTE 25 m
    // Frontera de MIN_RADIUS_ACCURACY
    // ratioLimit = 15
    // ============================================================

    @Test
    fun radio25_accuracyMenorAlRatio_noBloquea() {
        assertFalse(isAccuracyTooBad(14.9f, 25f))
    }

    @Test
    fun radio25_accuracyIgualAlRatio_noBloquea() {
        assertFalse(isAccuracyTooBad(15f, 25f))
    }

    @Test
    fun radio25_accuracyMayorAlRatio_bloquea() {
        assertTrue(isAccuracyTooBad(15.1f, 25f))
    }


    // ============================================================
    // RADIO INMEDIATAMENTE MAYOR A 25 m
    // Verifica el cambio de rama del if
    // ============================================================

    @Test
    fun radioMayor25_accuracyMenorAlRatio_noBloquea() {
        // ratioLimit = 25.1 * 0.60 = 15.06
        assertFalse(isAccuracyTooBad(15f, 25.1f))
    }

    @Test
    fun radioMayor25_accuracyMayorAlRatio_bloquea() {
        // ratioLimit = 15.06
        assertTrue(isAccuracyTooBad(15.1f, 25.1f))
    }


    // ============================================================
    // RADIO 30 m
    // ratioLimit = 18
    // ============================================================

    @Test
    fun radio30_accuracyMenorAlRatio_noBloquea() {
        assertFalse(isAccuracyTooBad(17.9f, 30f))
    }

    @Test
    fun radio30_accuracyIgualAlRatio_noBloquea() {
        assertFalse(isAccuracyTooBad(18f, 30f))
    }

    @Test
    fun radio30_accuracyMayorAlRatio_bloquea() {
        assertTrue(isAccuracyTooBad(18.1f, 30f))
    }


    // ============================================================
    // RADIO 40 m
    // ratioLimit = 24
    // ============================================================

    @Test
    fun radio40_accuracyMenorAlRatio_noBloquea() {
        assertFalse(isAccuracyTooBad(23.9f, 40f))
    }

    @Test
    fun radio40_accuracyIgualAlRatio_noBloquea() {
        assertFalse(isAccuracyTooBad(24f, 40f))
    }

    @Test
    fun radio40_accuracyMayorAlRatio_bloquea() {
        assertTrue(isAccuracyTooBad(24.1f, 40f))
    }


    // ============================================================
    // RADIO 50 m
    // ratioLimit = 30
    // ============================================================

    @Test
    fun radio50_accuracyMenorAlRatio_noBloquea() {
        assertFalse(isAccuracyTooBad(29.9f, 50f))
    }

    @Test
    fun radio50_accuracyIgualAlRatio_noBloquea() {
        assertFalse(isAccuracyTooBad(30f, 50f))
    }

    @Test
    fun radio50_accuracyMayorAlRatio_bloquea() {
        assertTrue(isAccuracyTooBad(30.1f, 50f))
    }


    // ============================================================
    // PUNTO DE CRUCE
    //
    // Es el radio donde:
    //
    // radio * 0.60 = 35
    //
    // No se utiliza 58.3333f directamente para evitar errores
    // producidos por la representación de números Float.
    // ============================================================

    @Test
    fun puntoCruce_accuracyMenor35_noBloquea() {
        val radius = ABSOLUTE_LIMIT / RADIUS_PERCENTAGE_FOR_ACCURACY

        assertFalse(
            isAccuracyTooBad(
                34.9f,
                radius
            )
        )
    }

    @Test
    fun puntoCruce_accuracyIgual35_noBloquea() {
        val radius = ABSOLUTE_LIMIT / RADIUS_PERCENTAGE_FOR_ACCURACY

        assertFalse(
            isAccuracyTooBad(
                35f,
                radius
            )
        )
    }

    @Test
    fun puntoCruce_accuracyMayor35_bloquea() {
        val radius = ABSOLUTE_LIMIT / RADIUS_PERCENTAGE_FOR_ACCURACY

        assertTrue(
            isAccuracyTooBad(
                35.1f,
                radius
            )
        )
    }


    // ============================================================
    // RADIO 60 m
    // ratioLimit = 36
    // El límite absoluto de 35 pasa a ser el más restrictivo
    // ============================================================

    @Test
    fun radio60_accuracyMenor35_noBloquea() {
        assertFalse(isAccuracyTooBad(34.9f, 60f))
    }

    @Test
    fun radio60_accuracyIgual35_noBloquea() {
        assertFalse(isAccuracyTooBad(35f, 60f))
    }

    @Test
    fun radio60_accuracyMayor35_bloquea() {
        assertTrue(isAccuracyTooBad(35.1f, 60f))
    }

    @Test
    fun radio60_accuracyIgualAlRatio_bloqueaPorLimiteAbsoluto() {
        // ratioLimit = 36
        //
        // 36 > 36 = false
        // 36 > 35 = true
        //
        // Debe bloquear por ABSOLUTE_LIMIT.
        assertTrue(isAccuracyTooBad(36f, 60f))
    }


    // ============================================================
    // RADIO 100 m
    // ratioLimit = 60
    // Debe dominar ABSOLUTE_LIMIT = 35
    // ============================================================

    @Test
    fun radio100_accuracyMenor35_noBloquea() {
        assertFalse(isAccuracyTooBad(34.9f, 100f))
    }

    @Test
    fun radio100_accuracyIgual35_noBloquea() {
        assertFalse(isAccuracyTooBad(35f, 100f))
    }

    @Test
    fun radio100_accuracyMayor35_bloquea() {
        assertTrue(isAccuracyTooBad(35.1f, 100f))
    }


    // ============================================================
    // RADIO MÁXIMO 300 m
    // ratioLimit = 180
    // Debe seguir dominando ABSOLUTE_LIMIT = 35
    // ============================================================

    @Test
    fun radio300_accuracyMenor35_noBloquea() {
        assertFalse(isAccuracyTooBad(34.9f, 300f))
    }

    @Test
    fun radio300_accuracy35_noBloquea() {
        assertFalse(isAccuracyTooBad(35f, 300f))
    }

    @Test
    fun radio300_accuracyMayor35_bloquea() {
        assertTrue(isAccuracyTooBad(35.1f, 300f))
    }


    // ============================================================
    // OTROS VALORES
    // ============================================================

    @Test
    fun accuracyCero_noBloquea() {
        assertFalse(isAccuracyTooBad(0f, 30f))
    }

    @Test
    fun accuracyMuyGrande_bloquea() {
        assertTrue(isAccuracyTooBad(1000f, 300f))
    }

    @Test
    fun accuracyInfinito_bloquea() {
        assertTrue(
            isAccuracyTooBad(
                Float.POSITIVE_INFINITY,
                50f
            )
        )
    }

    @Test
    fun accuracyNaN_noBloqueaConImplementacionActual() {
        /*
         * Este test documenta el comportamiento actual.
         *
         * Las comparaciones de NaN mediante > devuelven false,
         * por lo que actualmente NaN no provoca bloqueo.
         */
        assertFalse(
            isAccuracyTooBad(
                Float.NaN,
                50f
            )
        )
    }


    // ============================================================
    // PRUEBA SISTEMÁTICA
    //
    // Comprueba todos los radios enteros utilizados por la
    // aplicación entre 30 y 300 m y accuracies entre 0 y 300 m.
    //
    // La regla esperada es:
    //
    // accuracy > min(35, radio * 0.60)
    // ============================================================

    @Test
    fun todosLosRadios30a300_cumplenReglaEsperada() {

        for (radius in 30..300) {

            val radiusFloat = radius.toFloat()

            val expectedLimit = minOf(
                ABSOLUTE_LIMIT,
                radiusFloat * RADIUS_PERCENTAGE_FOR_ACCURACY
            )

            var accuracy = 0f

            while (accuracy <= 300f) {

                val expected = accuracy > expectedLimit

                val actual = isAccuracyTooBad(
                    accuracy,
                    radiusFloat
                )

                assertEquals(
                    "Falló con radius=$radiusFloat, " +
                            "accuracy=$accuracy, " +
                            "limit=$expectedLimit",
                    expected,
                    actual
                )

                accuracy += 0.1f
            }
        }
    }
}