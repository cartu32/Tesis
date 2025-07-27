package com.example.abumonitor


import android.app.Application
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.common.SharedVariables
import com.example.comunicationwearmobile.ui.model.datasource.local.dbInitializer
import com.example.comunicationwearmobile.ui.utils.Helpers.AlarmHelper
import com.example.comunicationwearmobile.ui.utils.broadcast.AlarmDailyActivateGeofReceiver
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
        initializeAlarmActivateGeofence()
        intializeAlarmCheckAssistance()
    }

    private fun intializeAlarmCheckAssistance() {
        val alarmHelper = AlarmHelper()

        with(SharedVariables) {
            //Como es la primera vez que se ejecuta la app seteo las alarmas por default
            hourDailyCheckAssitance   = Definition.DEFAULT_HOUR_DAILY_CHECK_ASSISTANCE
            minuteDailyCheckAssitance = Definition.DEFAULT_MINUTE_DAILY_CHECK_ASSISTANCE

            //inicio la alarma que checkear las asistencia a las citas del dia actual
            alarmIdActivateGeofence =
                alarmHelper.setDailyAlarm(
                    this@AbuMonitorApplicationMobile,
                    hourDailyCheckAssitance,
                    minuteDailyCheckAssitance,
                    Definition.ACTION_ALARM_DAILY_CHECK_ASSISTANCE,
                    AlarmDailyActivateGeofReceiver::class.java
                )
        }
    }

    private fun initializeAlarmActivateGeofence() {
        val alarmHelper = AlarmHelper()

        with(SharedVariables) {
            //Como es la primera vez que se ejecuta la app seteo las alarmas por default
            hourDailyActivateGeofence = Definition.DEFAULT_HOUR_DAILY_ACTIVATION_GEOF
            minuteDailyActivateGeofence =Definition.DEFAULT_MINUTE_DAILY_ACTIVATION_GEOF

            //inicio la alarma que va activar y desactivar
            //las areas de geofence del dia actual
            alarmIdCheckAssitance =
                alarmHelper.setDailyAlarm(
                    this@AbuMonitorApplicationMobile,
                    hourDailyActivateGeofence,
                    minuteDailyActivateGeofence,
                    Definition.ACTION_ALARM_DAILY_ACTIVATION_GEOF,
                    AlarmDailyActivateGeofReceiver::class.java
                )
        }
    }
    private fun configLeakCanary() {
        // Configuración adicional si es necesario
        //   LeakCanary.config = LeakCanary.config.copy(dumpHeap = false)
    }

}

