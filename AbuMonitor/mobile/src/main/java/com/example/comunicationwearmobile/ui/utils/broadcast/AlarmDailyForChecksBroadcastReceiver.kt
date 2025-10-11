package com.example.comunicationwearmobile.ui.utils.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.utils.Helpers.AlarmHelper
import com.example.comunicationwearmobile.ui.utils.services.GeofencesServices

class AlarmDailyForChecksBroadcastReceiver: BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {
        // Acción a ejecutar cuando se dispare la alarma
        Log.d(Definition.TAG_DEBUG, "Alarmas de checkeos ejecutada")
        Toast.makeText(context, "Alarmas de checkeos ejecutada", Toast.LENGTH_SHORT)
            .show()

        //llamo al foregroundservice para poder ejecutar en background sin problemas
        val serviceIntent = Intent(context, GeofencesServices::class.java).apply {
            action = intent.action
            putExtras(intent.extras!!)
        }
        ContextCompat.startForegroundService(context, serviceIntent)

        //se vuelve a configurar la alarma para que se repita despues de determinada cantidad de tiempo
        //de acuerdo a lo que se haya configurado el usuario en el menu de configuracion.
        intent.action?.let {
            setAlarmForNextTime(context, it)
        }
    }

    private fun setAlarmForNextTime(context: Context, action: String) {

        var hour    =0
        var minutes =0

/*        when(action){
            Definition.ACTION_ALARM_FOR_CHECKS->{
                hour=hourAlarmBetweenCheck
                minutes=minuteAlramBetweenCheck
            }
        }

        val alarmHelper=AlarmHelper()

        val resultSetAlarm= alarmHelper.setAlarmAfterOfTime(
            context,
            Definition.ALARM_ID_BETWEEN_CHECKS,
            hour,
            minutes,
            Definition.ACTION_ALARM_FOR_CHECKS,
            AlarmDailyForChecksBroadcastReceiver::class.java
        )

        if(resultSetAlarm) {
            Log.d(Definition.TAG_DEBUG, "Alarma de checkeo reconfigurada correctamente")
        }else{
            Log.d(Definition.TAG_DEBUG, "No se pudo volver a reconfigurar la alarma de checkeo")
        }
*/
    }

}