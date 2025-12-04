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


    private fun setAlarmForNextTime(context: Context, action: String) {
        val timeNextAlarm:Long
        val alarmId:Int

        when(action){
            Definition.ACTION_ALARM_FOR_CHECKS-> {
                timeNextAlarm=getIntervalForNextChecks(context)
                alarmId=Definition.ALARM_ID_FOR_CHECKS
            }
            Definition.ACTION_GEOFENCE_WATCHDOG->{
                timeNextAlarm=Definition.INTERVAL_WATCHDOG_MS
                alarmId=Definition.ALARM_ID_FOR_WATCHDOG
            }
            else->{
                Log.w(Definition.TAG_DEBUG, "Acción desconocida: $action")
                return
            }

        }

        if (timeNextAlarm == Definition.NO_STORED_VALUE) {
            Log.w(Definition.TAG_DEBUG, "Sin intervalo válido para reprogramar.")
            return
        }

        val ok = AlarmHelper.setNextAlarmInXTime(
            context = context,
            alarmId = alarmId,
            delayMillis = timeNextAlarm,
            action = action,
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

    //Hago est afuncion asi para guarda en el sharedpreference la hora de la proxima alarma,
    //asi para poder mostralo en el menu de configuracion
    private fun getIntervalForNextChecks(context: Context): Long {
        val repository = RepositoryConfigAppSPref.getInstance(context)
        //obtengo cada cuanto tiempo se debe hacer el checkeo
        val timeBetweenChecks = repository.getTimeBetweenChecksSync()

        saveHourMinutesNextAlarm(timeBetweenChecks,repository)
        return timeBetweenChecks
    }


    private fun saveHourMinutesNextAlarm(timeBetweenChecks: Long, repository: RepositoryConfigAppSPref) {
        //calculo la hora de la proxima alarma
        val aux=System.currentTimeMillis()+timeBetweenChecks
        val millisNextAlarm=Tools.extractHourOfDateInMillis(aux)

        //guardo en el sharedpreference la hora de la proxima alarma
        repository.saveTimeNextAlarmSync(millisNextAlarm)
    }
}

