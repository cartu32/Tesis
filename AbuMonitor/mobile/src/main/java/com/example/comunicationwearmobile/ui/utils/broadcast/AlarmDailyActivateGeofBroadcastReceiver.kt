package com.example.comunicationwearmobile.ui.utils.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.common.SharedVariables
import com.example.comunicationwearmobile.ui.utils.Helpers.AlarmHelper
import com.example.comunicationwearmobile.ui.utils.services.GeofencesServices

class AlarmDailyActivateGeofReceiver: BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {
        // Acción a ejecutar cuando se dispare la alarma
        Log.d(Definition.TAG_DEBUG, "Alarmas diaria ejecutada")
        Toast.makeText(context, "Alarmas diarias ejecutadas", Toast.LENGTH_SHORT)
            .show()

        //llamo al foregroundservice para poder ejecutar en background sin problemas
        val serviceIntent = Intent(context, GeofencesServices::class.java).apply {
            action = intent.action
            putExtras(intent.extras!!)
        }
        ContextCompat.startForegroundService(context, serviceIntent)

        //se vuelve a configurar la alarma para que se repita el dia siguiente a la misma hora
        intent.action?.let {
            setAlarmForNextDay(context, it)
        }
    }

    private fun setAlarmForNextDay(context: Context, action: String) {

        var hour    =0
        var minutes =0

        when(action){
            Definition.ACTION_ALARM_DAILY_ACTIVATION_GEOF->{
                hour=SharedVariables.hourDailyActivateGeofence
                minutes=SharedVariables.minuteDailyActivateGeofence
            }
            Definition.ACTION_ALARM_DAILY_CHECK_ASSISTANCE->{
                hour=SharedVariables.hourDailyCheckAssitance
                minutes=SharedVariables.minuteDailyCheckAssitance
            }
        }

        val alarmHelper=AlarmHelper()
        alarmHelper.setDailyAlarm(
            context,
            hour,
            minutes,action,
            AlarmDailyActivateGeofReceiver::class.java
        )
        Log.d(Definition.TAG_DEBUG, "Alarmas configuradas para el dia siguiente")

    }

}