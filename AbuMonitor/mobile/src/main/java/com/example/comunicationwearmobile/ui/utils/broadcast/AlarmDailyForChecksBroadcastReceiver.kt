package com.example.comunicationwearmobile.ui.utils.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.repository.RepositoryScheduleAlarmSPref
import com.example.comunicationwearmobile.ui.utils.Helpers.AlarmHelper
import com.example.comunicationwearmobile.ui.utils.Tools
import com.example.comunicationwearmobile.ui.utils.services.GeofencesServices

class AlarmDailyForChecksBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(Definition.TAG_DEBUG, "Alarmas de chequeos ejecutada")

        val serviceIntent = Intent(context, GeofencesServices::class.java).apply {
            action = intent.action
            intent.extras?.let { putExtras(it) }
        }

        ContextCompat.startForegroundService(context, serviceIntent)

        intent.action?.let { setAlarmForNextTime(context, it) }
    }

    private fun setAlarmForNextTime(context: Context, action: String) {
        if (action != Definition.ACTION_ALARM_FOR_CHECKS) {
            Log.w(Definition.TAG_DEBUG, "Acción desconocida: $action")
            return
        }

        val repository = RepositoryScheduleAlarmSPref.getInstance(context)
        val timeBetweenChecks = repository.getTimeBetweenChecksSync()

        if (timeBetweenChecks == Definition.NO_STORED_VALUE) {
            Log.w(Definition.TAG_DEBUG, "Sin intervalo válido para reprogramar.")
            return
        }

        //Reprogramar exacta después de 'timeBetweenChecks' milis
        val alarmHelper = AlarmHelper()


        alarmHelper.cancelAlarm(
            context,
            Definition.ALARM_ID_BETWEEN_CHECKS,
            Definition.ACTION_ALARM_FOR_CHECKS,
            AlarmDailyForChecksBroadcastReceiver::class.java
        )

        val ok = alarmHelper.setAlarmAfterOfTime(
            context = context,
            alarmId = Definition.ALARM_ID_BETWEEN_CHECKS,
            hours = Tools.getHourMinOfParcial(timeBetweenChecks).first,
            minutes = Tools.getHourMinOfParcial(timeBetweenChecks).second,
            action = Definition.ACTION_ALARM_FOR_CHECKS,
            receiverClass = AlarmDailyForChecksBroadcastReceiver::class.java
        )

        if(ok) {
            Log.d(Definition.TAG_DEBUG, "Alarma reconfigurada")
            Toast.makeText(context, "Alarma reconfigurada", Toast.LENGTH_SHORT).show()
        }else {
            Log.d(Definition.TAG_DEBUG, "Fallo reconfiguración")
            Toast.makeText(context, "Fallo reconfiguración", Toast.LENGTH_SHORT).show()
        }
    }
}
