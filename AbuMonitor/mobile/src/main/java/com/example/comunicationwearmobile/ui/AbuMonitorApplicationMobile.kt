package com.example.abumonitor


import android.app.Application
import com.example.abumonitor.constants.Definition
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
        val alarmHelper= AlarmHelper()

        //inicio la alarma que va activar y desactivar
        //las areas de geofence del dia actual
        alarmHelper.setDailyAlarm(
            this,
            Definition.HOUR_DAILY_ACTIVATION_GEOF,
            Definition.MINUTE_DAILY_ACTIVATION_GEOF,
            AlarmDailyActivateGeofReceiver::class.java
        )
    }

}

