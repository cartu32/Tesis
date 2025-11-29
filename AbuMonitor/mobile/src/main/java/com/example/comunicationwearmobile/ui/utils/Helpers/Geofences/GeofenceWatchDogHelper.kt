package com.example.comunicationwearmobile.ui.utils.Helpers.Geofences

import android.content.Context
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDebugLogger

class GeofenceWatchDogHelper(private val repo: RepositoryAreaDB, private val activator: GeofenceActivatorHelper) {

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
    }
}
