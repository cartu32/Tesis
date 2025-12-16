package com.example.comunicationwearmobile.ui.utils.Helpers.Geofences

import android.content.Context
import android.location.Location
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.model.dto.DataAreaGeofAux
import com.example.comunicationwearmobile.ui.model.dto.ResultAreaGenerateEvent
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDebugLogger
import com.example.comunicationwearmobile.ui.utils.Helpers.Alarm.AlarmHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.FSM.GeofenceFSM
import com.example.comunicationwearmobile.ui.utils.Tools
import com.example.comunicationwearmobile.ui.utils.broadcast.AlarmBroadcastReceiver
import com.google.android.gms.location.Geofence

object ManualGeofenceStrategyHelper {

    // ---- Anti-rearmado (RAM) ----
    private val scheduledDwell = mutableSetOf<Long>()

    // ---- Persistencia mínima (sobrevive proceso muerto) ----
    private const val DWELL_PREFS = "DWELL_PREFS"
    private const val KEY_DWELL_SCHEDULED_PREFIX = "DWELL_SCHEDULED_"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(DWELL_PREFS, Context.MODE_PRIVATE)

    private fun isDwellScheduledPersisted(context: Context, areaId: Long): Boolean =
        prefs(context).getBoolean(KEY_DWELL_SCHEDULED_PREFIX + areaId, false)

    private fun setDwellScheduledPersisted(context: Context, areaId: Long, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_DWELL_SCHEDULED_PREFIX + areaId, value).apply()
    }

    suspend fun callGeofenceManualStrategy(context: Context, location: Location) {
        try {
            RepositoryDebugLogger.log(context, "MANUAL_STRATEGY: inicio ejecución")
            Log.d(Definition.TAG_DEBUG, "MANUAL_STRATEGY: inicio ejecución")

            proccessManualStrategy(context, location)
        } catch (e: Exception) {
            RepositoryDebugLogger.log(context, "MANUAL_STRATEGY: error ${e.message}")
        }
    }

    private suspend fun proccessManualStrategy(context: Context, location: Location) {
        val appContext = context.applicationContext

        val activatedAreas = getActiveAreas(appContext)
        if (activatedAreas.isEmpty()) {
            RepositoryDebugLogger.log(appContext, "MANUAL_STRATEGY: no hay áreas activas")
            Log.d(Definition.TAG_DEBUG, "MANUAL_STRATEGY: no hay áreas activas")
            return
        }

        val globalEnterIds = mutableListOf<Long>()
        val globalExitIds = mutableListOf<Long>()

        val (speed, isFast) = determineSpeedElderly(location, appContext)

        for (dataArea in activatedAreas) {
            val areaResult = processSingleAreaManualStrategy(
                dataArea = dataArea,
                appContext = appContext,
                location = location,
                isFast = isFast,
                speed = speed
            )

            val areaId = areaResult.areaId

            // --- DWELL: anti-rearmado + persistencia ---
            // Si la FSM pide START pero ya estaba armada → NO reprogrames.
            if (areaResult.fireDwellStart) {
                val alreadyScheduled =
                    scheduledDwell.contains(areaId) || isDwellScheduledPersisted(appContext, areaId)

                if (!alreadyScheduled) {
                    startAlarmDwell(appContext, areaId, dataArea.secZoneDwellTime.dwell_time)
                    scheduledDwell.add(areaId)
                    setDwellScheduledPersisted(appContext, areaId, true)
                    RepositoryDebugLogger.log(appContext, "DWELL: alarma armada area=$areaId")
                } else {
                    RepositoryDebugLogger.log(appContext, "DWELL: ya armada, no reprog area=$areaId")
                }
            }

            // Cancelá DWELL tanto por CANCEL como por EXIT (si saliste, no hay dwell posible)
            if (areaResult.fireDwellCancel || areaResult.fireExit) {
                val wasScheduled =
                    scheduledDwell.contains(areaId) || isDwellScheduledPersisted(appContext, areaId)

                if (wasScheduled) {
                    cancelAlarmDwell(appContext, areaId)
                    scheduledDwell.remove(areaId)
                    setDwellScheduledPersisted(appContext, areaId, false)
                    RepositoryDebugLogger.log(appContext, "DWELL: alarma cancelada area=$areaId")
                }
            }

            if (areaResult.fireEnter) {
                globalEnterIds.add(areaId)
            }
            if (areaResult.fireExit) {
                globalExitIds.add(areaId)
            }
        }

        if (globalEnterIds.isNotEmpty()) {
            triggerActionAreaEntry(globalEnterIds, appContext)
        }

        if (globalExitIds.isNotEmpty()) {
            triggerActionAreaExit(globalExitIds, appContext)
        }
    }

    private fun cancelAlarmDwell(appContext: Context, areaId: Long) {
        val areaIdForAlarm = Tools.convertLongToInt(areaId, Definition.HASH_TYPE_DWELL)

        AlarmHelper.cancelAlarm(
            appContext,
            areaIdForAlarm,
            Definition.ACTION_ALARM_FOR_DWELL_TIME,
            AlarmBroadcastReceiver::class.java
        )
    }

    private fun startAlarmDwell(appContext: Context, areaId: Long, dwellTime: Long) {
        val areaIdForAlarm = Tools.convertLongToInt(areaId, Definition.HASH_TYPE_DWELL)

        AlarmHelper.setNextAlarmInXTime(
            context = appContext,
            alarmId = areaIdForAlarm,
            delayMillis = dwellTime, // (ms)
            action = Definition.ACTION_ALARM_FOR_DWELL_TIME,
            areaId = areaId,
            receiverClass = AlarmBroadcastReceiver::class.java
        )
    }

    private suspend fun processSingleAreaManualStrategy(
        dataArea: DataAreaGeofAux,
        appContext: Context,
        location: Location,
        isFast: Boolean,
        speed: Float
    ): ResultAreaGenerateEvent {

        val area = dataArea.entityAreaGeofence

        RepositoryDebugLogger.log(appContext, "Area Id: ${area.id_area} | descripcion: ${area.description}")
        Log.d(Definition.TAG_DEBUG, "Area Id: ${area.id_area} | descripcion: ${area.description}")

        val resultFsm = GeofenceFSM.proccessFSM(
            dataArea = dataArea,
            appContext = appContext,
            area = area,
            location = location,
            isFast = isFast,
            speed = speed
        )

        return ResultAreaGenerateEvent(
            areaId = area.id_area,
            fireEnter = resultFsm.triggerEnter,
            fireExit = resultFsm.triggerExit,
            fireDwellStart = resultFsm.triggerDwellStart,
            fireDwellCancel = resultFsm.triggerDwellCancel
        )
    }

    private suspend fun triggerActionAreaExit(
        exitIds: MutableList<Long>,
        appContext: Context,
    ) {
        if (exitIds.isNotEmpty()) {
            RepositoryDebugLogger.log(appContext, "MANUAL_STRATEGY: disparo EXIT para ids=$exitIds")
            Log.d(Definition.TAG_DEBUG, "MANUAL_STRATEGY: disparo EXIT para ids=$exitIds")

            GeofenceEventProcessorHelper.handleEvent(
                triggeringIds = exitIds,
                transition = Geofence.GEOFENCE_TRANSITION_EXIT
            )
        }
    }

    private suspend fun triggerActionAreaEntry(
        enterIds: MutableList<Long>,
        appContext: Context,
    ) {
        if (enterIds.isNotEmpty()) {
            RepositoryDebugLogger.log(appContext, "MANUAL_STRATEGY: disparo ENTER para ids=$enterIds")
            Log.d(Definition.TAG_DEBUG, "MANUAL_STRATEGY: disparo ENTER para ids=$enterIds")

            GeofenceEventProcessorHelper.handleEvent(
                triggeringIds = enterIds,
                transition = Geofence.GEOFENCE_TRANSITION_ENTER
            )
        }
    }

    private fun determineSpeedElderly(location: Location, context: Context): Pair<Float, Boolean> {
        val speedMps = location.speed
        val isFast = speedMps > Definition.LIMIT_SPEED_WALKING

        val speedInKmH=speedMps*Definition.CONVESION_METER_PER_SECOND

        val speedKmhNoDecimals = "%.0f".format(speedInKmH)

        Log.d(Definition.TAG_DEBUG, "Velocidad limite %.0f KM/H".format(Definition.LIMIT_SPEED_WALKING * Definition.CONVESION_METER_PER_SECOND))

        if (isFast) {
            RepositoryDebugLogger.log(context, "VELOCIDAD $speedKmhNoDecimals KM/H EN AUTO")
            Log.d(Definition.TAG_DEBUG, "VELOCIDAD $speedKmhNoDecimals KM/H EN AUTO")
        } else {
            RepositoryDebugLogger.log(context, "VELOCIDAD $speedKmhNoDecimals KM/H EN CAMINANDO")
            Log.d(Definition.TAG_DEBUG, "VELOCIDAD $speedKmhNoDecimals KM/H EN CAMINANDO")
        }

        return Pair(speedMps, isFast)
    }

    private suspend fun getActiveAreas(appContext: Context): List<DataAreaGeofAux> {
        val repoAreas = RepositoryAreaDB.getInstance(appContext)
        return repoAreas.getAllActiveAreasWithEvents()
    }
}

