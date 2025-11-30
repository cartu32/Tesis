package com.example.comunicationwearmobile.ui.utils.Helpers.Geofences

import android.content.Context
import android.location.Location
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDebugLogger
import com.example.comunicationwearmobile.ui.model.repository.RepositoryLocation

class GeofenceWatchDogHelper(
    private val repo: RepositoryAreaDB,
    private val activator: GeofenceActivatorHelper
) {

    suspend fun reRegisterAllActiveGeofences(context: Context) {
        val appContext = context.applicationContext

        // 1) Traigo todas las geofences activas desde BD
        val listAreas = repo.getAllActiveAreasWithEvents()
        RepositoryDebugLogger.log(appContext, "Watchdog: areas activas en BD = ${listAreas.size}")

        if (listAreas.isEmpty()) {
            RepositoryDebugLogger.log(appContext, "Watchdog: no hay áreas activas para re-registrar")
            return
        }

        // 2) Borro TODAS las geofences registradas
        val clearOk = activator.clearAllGeofence(appContext)
        RepositoryDebugLogger.log(appContext, "Watchdog: clearAllGeofence result = $clearOk")

        if (!clearOk) {
            RepositoryDebugLogger.log(appContext, "Watchdog: fallo clearAllGeofence, NO re-registro")
            return
        }

        // 3) Vuelvo a registrar una por una
        for (areaAux in listAreas) {
            val ok = activator.activateGeofence(appContext, areaAux)
            RepositoryDebugLogger.log(
                appContext,
                "Watchdog: re-add geofence id=${areaAux.entityAreaGeofence.id_area}, ok=$ok"
            )
        }

        // 4) "Pinchazo" de ubicación para despertar geofencing
        refreshLocation(context)
    }

    // --------------------------------------------------------------------------
    // NUEVA FUNCIÓN: solicitar ubicación REAL con diagnostico completo
    // --------------------------------------------------------------------------
    private suspend fun refreshLocation(context: Context) {
        RepositoryDebugLogger.log(context, "Watchdog: solicitando ubicación puntual (diagnóstico)")

        try {
            val repoLoc = RepositoryLocation.getInstance(context)

            // 🔥 OPCIÓN 1: Balanced (lo que ya usabas)
            // val loc = repoLoc.getSingleBalancedLocation()

            // 🔥 OPCIÓN 2: High Accuracy (recomendada para Android 15)
            val loc = repoLoc.getSingleHighAccuracyLocation()

            if (loc != null) {
                val now = System.currentTimeMillis()
                val age = now - loc.time
                val fresh = loc.isFreshAndAccurate()

                RepositoryDebugLogger.log(
                    context,
                    "Watchdog: ubicación -> lat=${loc.latitude}, lon=${loc.longitude}, " +
                            "acc=${loc.accuracy}, age=${age}ms, fresh=${fresh}"
                )
            } else {
                RepositoryDebugLogger.log(context, "Watchdog: NO se obtuvo ubicación puntual")
            }

        } catch (e: Exception) {
            RepositoryDebugLogger.log(context, "Watchdog: ERROR al obtener ubicación: ${e.message}")
        }
    }
}


// --------------------------------------------------------------------------
// EXTENSIÓN: permite detectar si la ubicación es REAL o BASURA CACHEADA
// --------------------------------------------------------------------------
fun Location.isFreshAndAccurate(
    maxAgeMillis: Long = 2 * 60 * 1000,  // máximo 2 min de antigüedad
    maxAccuracyMeters: Float = 50f       // precisión aceptable (<50m)
): Boolean {
    val now = System.currentTimeMillis()
    val age = now - this.time

    return age in 0..maxAgeMillis && this.accuracy in 0f..maxAccuracyMeters
}
