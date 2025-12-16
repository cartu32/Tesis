package com.example.comunicationwearmobile.ui.utils.Helpers.Alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log
import com.example.abumonitor.constants.Definition

object AlarmHelper {


    private fun setAlarmInternal(
        context: Context,
        alarmId: Int,
        triggerAtMillis: Long,
        type: Int,
        areaId: Long? =null,
        action: String,
        receiverClass: Class<out BroadcastReceiver>
    ): Boolean {
        return try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                ?: return false


            val intent = Intent(context, receiverClass).apply {
                this.action = action
                putExtra(Definition.INTENT_ALARM_ID, alarmId)
                putExtra(Definition.INTENT_ALARM_TIME, triggerAtMillis)
                if(areaId!=null)
                    putExtra(Definition.INTENT_ALARM_PARAM1,areaId)
            }

            val pi = PendingIntent.getBroadcast(
                context,
                alarmId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            configExactAlarm(triggerAtMillis, type, pi, alarmManager)
        } catch (e: Exception) {
            Log.e(Definition.TAG_DEBUG, "Error al programar alarma: ${e.message}")
            false
        }
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
    ): Boolean {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, receiverClass).apply { this.action = action }

        // Verifica si existe la alarma
        val existingPi = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        return if (existingPi != null) {
            // Si existe, la cancelamos
            am.cancel(existingPi)
            existingPi.cancel() // cancela también el PendingIntent
            Log.d(Definition.TAG_DEBUG, "Alarma cancelada correctamente (ID=$alarmId, action=$action)")
            true
        } else {
            Log.w(Definition.TAG_DEBUG, "No se encontró una alarma activa (ID=$alarmId, action=$action)")
            false
        }
    }

    /**
     * Configura una alarma exacta, manejando el permiso en Android 12+.
     */
    private fun configExactAlarm(
        triggerAtMillis: Long,
        type: Int,
        pendingIntent: PendingIntent,
        alarmManager: AlarmManager
    ): Boolean {
       try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(type, triggerAtMillis, pendingIntent)
                } else {
                    // Manual strategy: inexacta (podría demorarse por batching)
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

    // Alarma relativa (en X milisegundos) usando ELAPSED_REALTIME_WAKEUP
    // se usa para activar la alarma para que se active cada determinado tiempo
    // por ejemplo:cada 3 minutos, cada 5 minutos, etc.
    fun setNextAlarmInXTime(context: Context, alarmId: Int, delayMillis: Long, action: String, receiverClass: Class<out BroadcastReceiver>,areaId: Long?=null): Boolean {
        val triggerAtElapsed = SystemClock.elapsedRealtime() + delayMillis

        //cancelo la alrma si anteriormente esta configurada
        cancelAlarm(context, alarmId, action, receiverClass)

        //configuro la alarma
        return setAlarmInternal(
            context,
            alarmId,
            triggerAtMillis = triggerAtElapsed,
            type = AlarmManager.ELAPSED_REALTIME_WAKEUP,
            action = action,
            areaId = areaId,
            receiverClass = receiverClass
        )
    }


    //Alarma en una fecha/hora exacta de calendario usando RTC_WAKEUP
    //se usa para activar la alarma exacta en una hora determinada
    //por ejemplo: el lunes 15 a las 20:30
    fun setNextAlarmAtExactTime(
        context: Context,
        alarmId: Int,
        triggerAtMillis: Long, // epoch time (System.currentTimeMillis-based)
        action: String,
        receiverClass: Class<out BroadcastReceiver>
    ): Boolean {

        //cancelo la alrma si anteriormente esta configurada
        cancelAlarm(context, alarmId, action, receiverClass)

        //configuro la alarma
        return setAlarmInternal(
            context,
            alarmId,
            triggerAtMillis = triggerAtMillis,
            type = AlarmManager.RTC_WAKEUP,
            action = action,
            receiverClass = receiverClass
        )
    }
}

