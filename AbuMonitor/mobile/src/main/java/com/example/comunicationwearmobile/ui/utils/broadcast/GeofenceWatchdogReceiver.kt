package com.example.comunicationwearmobile.ui.utils.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDebugLogger
import com.example.comunicationwearmobile.ui.utils.Helpers.Alarm.GeofenceWatchdogScheduler
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.GeofenceActivatorHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.GeofenceWatchDogHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceWatchdogReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Definition.ACTION_GEOFENCE_WATCHDOG) return

        val pendingResult = goAsync()

        val repo = RepositoryAreaDB.getInstance(context)
        val watchdog = GeofenceWatchDogHelper(repo)

        RepositoryDebugLogger.log(context, "WatchdogReceiver ejecutado")
        CoroutineScope(Dispatchers.Default).launch {
            try {
                watchdog.reRegisterAllActiveGeofences(context)
            } catch (e: Exception) {
                Log.e(Definition.TAG_DEBUG, "Error en watchdog de geofence", e)
            } finally {
                GeofenceWatchdogScheduler.scheduleNext(
                    context,
                    reason = "FROM_ONRECEIVE_WATCHDOG"
                )
                pendingResult.finish()
            }
        }
    }
}
