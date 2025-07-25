package com.example.comunicationwearmobile.ui.utils.Helpers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.abumonitor.constants.Definition
import java.util.Calendar
class AlarmHelper {

    companion object {
        private var alarmIdCounter: Int = 0
    }

    /**
     * Crea una alarma diaria exacta.
     * @return el ID único de la alarma, útil para cancelarla después.
     */
    fun setDailyAlarm(
        context: Context,
        hour: Int,
        minute: Int,
        mAction: String,
        receiverClass: Class<out BroadcastReceiver>
    ): Int {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val alarmId = ++alarmIdCounter // ID único para esta alarma

        val intent = Intent(context, receiverClass).apply {
            action = mAction
            putExtra(Definition.INTENT_ALARM_ALARM_ID, alarmId)
            putExtra(Definition.INTENT_ALARM_HOUR, hour)
            putExtra(Definition.INTENT_ALARM_MINUTE, minute)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId, // ahora usamos un ID único
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        configExactAlarm(calendar, pendingIntent, alarmManager)

        return alarmId
    }

    /**
     * Cancela una alarma previamente creada usando el mismo alarmId.
     */
    fun cancelAlarm(context: Context, alarmId: Int, mAction: String, receiverClass: Class<out BroadcastReceiver>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, receiverClass).apply {
            action = mAction
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId, // debe coincidir con el usado en setDailyAlarm
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    private fun configExactAlarm(
        calendar: Calendar,
        pendingIntent: PendingIntent,
        alarmManager: AlarmManager
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.w(Definition.TAG_DEBUG, "No tiene permiso para alarmas exactas. Se usa alarma inexacta.")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.e("AlarmHelper", "Error al programar alarma exacta: ${e.message}")
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
}
