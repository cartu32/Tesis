package com.example.abumonitor


import android.app.Application
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.common.SharedVariables
import com.example.comunicationwearmobile.ui.utils.Helpers.AlarmHelper
import com.example.comunicationwearmobile.ui.utils.broadcast.AlarmDailyActivateGeofReceiver

//import leakcanary.LeakCanary


class AbuMonitorApplicationMobile : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeAplication()
        // Configuración adicional si es necesario
     //   LeakCanary.config = LeakCanary.config.copy(dumpHeap = false)
    }

    private fun initializeAplication() {
        val alarmHelper = AlarmHelper()

        with(SharedVariables) {
            //Como es la primera vez que se ejecuta la app seteo las alarmas por default
            hourDailyActivateGeofence = Definition.DEFAULT_HOUR_DAILY_ACTIVATION_GEOF
            minuteDailyActivateGeofence =Definition.DEFAULT_MINUTE_DAILY_ACTIVATION_GEOF

            //inicio la alarma que va activar y desactivar
            //las areas de geofence del dia actual
            alarmIdActivateGeofence =
                alarmHelper.setDailyAlarm(
                    this@AbuMonitorApplicationMobile,
                    hourDailyActivateGeofence,
                    minuteDailyActivateGeofence,
                    Definition.ACTION_ALARM_DAILY_ACTIVATION_GEOF,
                    AlarmDailyActivateGeofReceiver::class.java
                )
        }
    }

}

