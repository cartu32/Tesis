package com.example.comunicationwearmobile.ui.utils.Helpers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.utils.Tools
import java.util.Calendar
class AlarmHelper {

    /**
     * Programa una alarma exacta en una hora del día (próxima ocurrencia).
     * Usa reloj de pared (RTC).
     */
    fun setAlarmAtSpecificTime(
        context: Context,
        alarmId: Int,
        hour: Int,
        minute: Int,
        action: String,
        receiverClass: Class<out BroadcastReceiver>
    ): Boolean{
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

            if (alarmManager==null)
                return false

            val intent = Intent(context, receiverClass).apply {
                this.action = action
                putExtra(Definition.INTENT_ALARM_ALARM_ID, alarmId)
                putExtra(Definition.INTENT_ALARM_HOUR, hour)
                putExtra(Definition.INTENT_ALARM_MINUTE, minute)
            }

            val pi = PendingIntent.getBroadcast(
                context,
                alarmId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Instante absoluto (epoch ms) para HOY a (hour:minute), o mañana si ya pasó
            val triggerAtMillisRtc = Tools.getHourInMillis(hour, minute)

            configExactAlarm(
                triggerAtMillis = triggerAtMillisRtc,
                type = AlarmManager.RTC_WAKEUP,
                pendingIntent = pi,
                alarmManager = alarmManager,
                context = context
            )
        } catch (e: Exception) {
            Log.e(Definition.TAG_DEBUG, "Error al programar alarma (RTC): ${e.message}")
            return  false
        }
        return true
    }

    /**
     * Programa una alarma exacta para “dentro de” (h, m) usando reloj relativo.
     *
     */
    fun setAlarmAfterOfTime(
        context: Context,
        alarmId: Int,
        hours: Int,
        minutes: Int,
        action: String,
        receiverClass: Class<out BroadcastReceiver>
    ): Boolean {
        try {
            val alarmManager =context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

            if (alarmManager==null)
                return false

            val intent = Intent(context, receiverClass).apply {
                this.action = action
                putExtra(Definition.INTENT_ALARM_ALARM_ID, alarmId)
                putExtra(Definition.INTENT_ALARM_HOUR, hours)
                putExtra(Definition.INTENT_ALARM_MINUTE, minutes)
            }

            val pi = PendingIntent.getBroadcast(
                context,
                alarmId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Delay en ms (duración), convertido a instante relativo
            val delayMs = Tools.getTimeInMillis(hours, minutes) // e.j. h*3600000 + m*60000
            val triggerAtElapsed = SystemClock.elapsedRealtime() + delayMs

            configExactAlarm(
                triggerAtMillis = triggerAtElapsed,
                type = AlarmManager.ELAPSED_REALTIME_WAKEUP,
                pendingIntent = pi,
                alarmManager = alarmManager,
                context = context
            )
        } catch (e: Exception) {
            Log.e(Definition.TAG_DEBUG, "Error al programar alarma (ELAPSED): ${e.message}")
            return false
        }
        return true
    }

    /**
     * Cancela una alarma previamente creada.
     * Importante: action, requestCode (alarmId) y componente deben coincidir.
     */
    fun cancelAlarm(
        context: Context,
        alarmId: Int,
        action: String,
        receiverClass: Class<out BroadcastReceiver>
    ) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, receiverClass).apply { this.action = action }
        val pi = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
    }

    /**
     * Configura una alarma exacta, manejando el permiso en Android 12+.
     */
    private fun configExactAlarm(
        triggerAtMillis: Long,
        type: Int,
        pendingIntent: PendingIntent,
        alarmManager: AlarmManager,
        context: Context
    ): Boolean {
       try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(type, triggerAtMillis, pendingIntent)
                } else {
                    // Fallback: inexacta (podría demorarse por batching)
                    alarmManager.set(type, triggerAtMillis, pendingIntent)
                    Log.w(Definition.TAG_DEBUG, "Sin permiso de alarmas exactas. Usando inexacta")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(type, triggerAtMillis, pendingIntent)
            }
        } catch (se: SecurityException) {
            Log.e(Definition.TAG_DEBUG, "SECURITY: ${se.message}")
            return false
        }
        return true
    }
}

