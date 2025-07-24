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

class AlarmHelper() {

    fun setDailyAlarm(context: Context, hour: Int, minute: Int, receiverClass: Class<out BroadcastReceiver>) {
        val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, receiverClass).apply {
            putExtra("hour", hour)
            putExtra("minute", minute)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Si la hora ya pasó hoy, programar para mañana
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }


        configExactAlarm(calendar,pendingIntent,alarmManager)

    }

    private fun configExactAlarm(
        calendar: Calendar,
        pendingIntent: PendingIntent,
        alarmManager: AlarmManager
    ) {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    // No tiene permiso para alarmas exactas
                    // Aquí podés usar alarmas inexactas como fallback
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    // O mostrar UI para que usuario habilite permiso
                    // También podés informar con un Toast o Log
                    Log.w(Definition.TAG_DEBUG, "No tiene permiso para alarmas exactas. Se usa alarma inexacta.")
                }
            } else {
                // Android < 12, no hay restricciones
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Capturar excepción si falta permiso (por si acaso)
            Log.e("Alarm", "SecurityException al programar alarma exacta: ${e.message}")
            // Podés intentar usar alarma inexacta como fallback
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

}