package com.example.comunicationwearmobile.ui.utils.Helpers.Alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDebugLogger
import com.example.comunicationwearmobile.ui.utils.broadcast.GeofenceWatchdogReceiver

object GeofenceWatchdogScheduler {

    private fun getPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, GeofenceWatchdogReceiver::class.java).apply {
            action = Definition.ACTION_GEOFENCE_WATCHDOG
        }

        return PendingIntent.getBroadcast(context, Definition.REQUEST_CODE_GEOFENCE_WATCHDOG, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
    }

    /**
     * Programa la próxima ejecución del watchdog usando setExactAndAllowWhileIdle.
     */
    fun scheduleNext(
        context: Context,
        intervalMillis: Long = Definition.INTERVAL_WATCHDOG_MS,
        reason: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val triggerAtMillis = System.currentTimeMillis() + intervalMillis
        val pendingIntent = getPendingIntent(context)

        Log.d(Definition.TAG_DEBUG, "WatchdogScheduler: próxima ejecución en ${intervalMillis / 1000 / 60} minutos|(reason=$reason)")
        RepositoryDebugLogger.log(context,"WatchdogScheduler: próxima ejecución en ${intervalMillis / 1000 / 60} minutos |(reason=$reason)")
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    /**
     * Por si alguna vez querés cancelar la alarma (ej: al desactivar todo).
     */
    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = getPendingIntent(context)
        alarmManager.cancel(pendingIntent)
    }
}
