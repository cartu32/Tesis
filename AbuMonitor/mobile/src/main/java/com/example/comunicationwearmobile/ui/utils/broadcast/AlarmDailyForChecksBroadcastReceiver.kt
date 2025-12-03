package com.example.comunicationwearmobile.ui.utils.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.repository.RepositoryConfigAppSPref
import com.example.comunicationwearmobile.ui.utils.Helpers.Alarm.AlarmHelper
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

    //Hago est afuncion asi para guarda en el sharedpreference la hora de la proxima alarma,
    //asi para poder mostralo en el menu de configuracion

    private fun setAlarmForNextTime(context: Context, action: String) {
        if (action != Definition.ACTION_ALARM_FOR_CHECKS) {
            Log.w(Definition.TAG_DEBUG, "Acción desconocida: $action")
            return
        }

        val repository = RepositoryConfigAppSPref.getInstance(context)
        //obtengo cada cuanto tiempo se debe hacer el checkeo
        val timeBetweenChecks = repository.getTimeBetweenChecksSync()

        saveHourMinutesNextAlarm(timeBetweenChecks,repository)
        if (timeBetweenChecks == Definition.NO_STORED_VALUE) {
            Log.w(Definition.TAG_DEBUG, "Sin intervalo válido para reprogramar.")
            return
        }

        val ok = AlarmHelper.setNextAlarmInXTime(
            context = context,
            alarmId = Definition.ALARM_ID_FOR_CHECKS,
            timeBetweenChecks,
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


    private fun saveHourMinutesNextAlarm(timeBetweenChecks: Long, repository: RepositoryConfigAppSPref) {
        //calculo la hora de la proxima alarma
        val aux=System.currentTimeMillis()+timeBetweenChecks
        val millisNextAlarm=Tools.extractHourOfDateInMillis(aux)

        //guardo en el sharedpreference la hora de la proxima alarma
        repository.saveTimeNextAlarmSync(millisNextAlarm)
    }
}

