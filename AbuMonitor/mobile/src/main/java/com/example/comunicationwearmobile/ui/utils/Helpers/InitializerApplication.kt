package com.example.comunicationwearmobile.ui.utils.Helpers

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.datasource.local.dbInitializer
import com.example.comunicationwearmobile.ui.model.repository.RepositoryConfigAppSPref
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.GeofenceEventProcessorHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.Notification.NotificationHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.Notification.NotificationManager
import com.example.comunicationwearmobile.ui.utils.Helpers.Notification.SmsHelper
import com.example.comunicationwearmobile.ui.utils.Tools
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object InitializerApplication {
    private lateinit var appContext: Context

    fun init(application: Application) {
        appContext = application.applicationContext

        initilizerDB()
        initializeAlarms()
        initilizeSPRememberAppointment()
        initSmsHelper()
        initGeofenceEventProcessorHelper()
        initNotificationMannager()
        configLeakCanary()


    }

    private fun initNotificationMannager() {
        NotificationManager.init(appContext)
    }

    private fun initSmsHelper() {
        SmsHelper.registerSMSReceivers(appContext)
    }

    private fun initGeofenceEventProcessorHelper() {
        GeofenceEventProcessorHelper.init(appContext)
    }

    private fun initilizeSPRememberAppointment() {
        val hourTimeRemember    = Definition.DEFAULT_HOUR_REMEMER_APPOINTMENT
        val minuteTimeRemember  = Definition.DEFAULT_MINUTE_REMEMER_APPOINTMENT
        val repository = RepositoryConfigAppSPref.getInstance(appContext)

        val timeRememberAppointment=repository.getTimeRememberAppointmentSync()

        if (timeRememberAppointment == Definition.NO_STORED_VALUE) {
            Log.w(Definition.TAG_DEBUG, "No hay tiempo configurado para recordar cita. Uso valores por defecto.")

            val timeParcialMillis= Tools.getTimeInMillis(hourTimeRemember, minuteTimeRemember)
            repository.saveTimeRememberAppointmentSync(timeParcialMillis)
        }
    }

    private fun initilizerDB() {
        val dbInitializer = dbInitializer()

        // Iniciar la base de datos en background
        CoroutineScope(Dispatchers.Default).launch {
            dbInitializer.checkAndInitDatabase(appContext)
        }
    }

    private fun initializeAlarms() {
       // initializeAlarmAssistance()
    }


    fun onTerminate() {
        SmsHelper.onDestroy()
        NotificationHelper.getInstance(appContext)?.cancelCorutineInit()
    }


    private fun configLeakCanary() {
        // Configuración adicional si es necesario
        //   LeakCanary.config = LeakCanary.config.copy(dumpHeap = false)
    }
}
