package com.example.abumonitor


import android.app.Application
import android.util.Log
import android.widget.Toast
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.common.SharedVariables
import com.example.comunicationwearmobile.ui.model.datasource.local.dbInitializer
import com.example.comunicationwearmobile.ui.utils.Helpers.AlarmHelper
import com.example.comunicationwearmobile.ui.utils.broadcast.AlarmDailyForChecksBroadcastReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//import leakcanary.LeakCanary


class AbuMonitorApplicationMobile : Application() {
    override fun onCreate() {
        super.onCreate()

        initilizerDB()
        initializeAlarm()
        configLeakCanary()

      }



    private fun initilizerDB() {
        val dbInitializer = dbInitializer()

        // Iniciar la base de datos en background
        CoroutineScope(Dispatchers.Default).launch {
            dbInitializer.checkAndInitDatabase(applicationContext)
        }
    }

    private fun initializeAlarm() {
        intializeAlarmForChecks()
    }

    private fun intializeAlarmForChecks() {
        val alarmHelper = AlarmHelper()

/*        with(SharedVariables) {
            //Como es la primera vez que se ejecuta la app seteo las alarmas por default
            hourAlarmBetweenCheck   = Definition.DEFAULT_HOUR_ALARM_BETWEEN_CHECKS
            minuteAlramBetweenCheck = Definition.DEFAULT_MINUTE_ALARM_BETWEEN_CHECKS


            val resultSetAlarm= alarmHelper.setAlarmAfterOfTime(
                this@AbuMonitorApplicationMobile,
                Definition.ALARM_ID_BETWEEN_CHECKS,
                hourAlarmBetweenCheck,
                minuteAlramBetweenCheck,
                Definition.ACTION_ALARM_FOR_CHECKS,
                AlarmDailyForChecksBroadcastReceiver::class.java
            )

            if(resultSetAlarm) {
                Log.d(Definition.TAG_DEBUG, "Alarma de checkeo configurada correctamente")
                Toast.makeText(this@AbuMonitorApplicationMobile, "Alarma de  checkeo configurada correctamente", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this@AbuMonitorApplicationMobile,"No se pudo configurar la alarma", Toast.LENGTH_SHORT).show()
            }
        }*/
    }

    private fun configLeakCanary() {
        // Configuración adicional si es necesario
        //   LeakCanary.config = LeakCanary.config.copy(dumpHeap = false)
    }

}

