package com.example.comunicationwearmobile.ui.utils.Helpers

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.common.SharedVariables
import com.example.comunicationwearmobile.ui.model.datasource.local.dbInitializer
import com.example.comunicationwearmobile.ui.model.repository.RepositoryConfigAppSPref
import com.example.comunicationwearmobile.ui.utils.Helpers.Alarm.AlarmHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.GeofenceEventProcessorHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.Notification.NotificationHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.Notification.SmsHelper
import com.example.comunicationwearmobile.ui.utils.Tools
import com.example.comunicationwearmobile.ui.utils.broadcast.AlarmBroadcastReceiver
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
        configLeakCanary()


    }

    private fun initSmsHelper() {
        SmsHelper.registerSMSReceivers(appContext)
    }

    private fun initGeofenceEventProcessorHelper() {
        GeofenceEventProcessorHelper.init(appContext)
    }

    private fun initilizeSPRememberAppointment() {
        var hourTimeRemember    = Definition.DEFAULT_HOUR_REMEMER_APPOINTMENT
        var minuteTimeRemember  = Definition.DEFAULT_MINUTE_REMEMER_APPOINTMENT
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
        //initializeAlarmAssistance()
    }



    private fun initAlarmAssistance(intervalToAlarmMS: Long){

        val resultSetAlarm = AlarmHelper.setNextAlarmInXTime(
            appContext,
            Definition.ALARM_ID_FOR_CHECKS,
            intervalToAlarmMS,
            Definition.ACTION_ALARM_FOR_CHECKS,
            AlarmBroadcastReceiver::class.java
        )

        showStatusAlarm(resultSetAlarm,Definition.ACTION_ALARM_FOR_CHECKS)

    }

    private fun initializeAlarmAssistance() {

        val repository  = RepositoryConfigAppSPref.getInstance(appContext)


        //leo del SharedPreferences la hora de la alarma de chequeo
        val storedMillis = repository.getTimeBetweenChecksSync()

        //obtengo la hora, minutos de la alarma
        val intervalMillis = getIntervalForFirsTimeAlarm(storedMillis,repository)

        //cada vez que se inicia la app se vuelve a configurar una nueva alarama
        SharedVariables.timeAlarmChecksFirstTime = Tools.extractHourOfDateInMillis(System.currentTimeMillis() + intervalMillis)
        SharedVariables.isOpenAppFirsTime=true

        initAlarmAssistance (intervalMillis)
    }



    private fun getIntervalForFirsTimeAlarm(storedMillis: Long, repository: RepositoryConfigAppSPref): Long{

        //pregunto si la alarma esta incializada en el shared preference
        if (storedMillis == Definition.NO_STORED_VALUE) {
            Log.w(Definition.TAG_DEBUG, "No hay intervalo configurado para los chequeos. Uso valores por defecto.")

            val defaultHour   = Definition.DEFAULT_HOUR_ALARM_BETWEEN_CHECKS
            val defaultMinute = Definition.DEFAULT_MINUTE_ALARM_BETWEEN_CHECKS
            val defaultMillis = Tools.getTimeInMillis(defaultHour, defaultMinute)

            // Primero calculamos los millis y recién ahí los guardamos
            repository.saveTimeBetweenChecksSync(defaultMillis)

            return  defaultMillis
        }
        return storedMillis
    }


    private fun showStatusAlarm(resultSetAlarm: Boolean, nameAlarm: String) {
        if (resultSetAlarm) {
            Log.d(Definition.TAG_DEBUG, "Alarma de de $nameAlarm configurada correctamente")
            Toast.makeText(appContext, "Alarma de $nameAlarm configurada correctamente", Toast.LENGTH_SHORT).show()
        } else {
            Log.e(Definition.TAG_DEBUG, "No se pudo configurar la alarma")
            Toast.makeText(appContext, "No se pudo configurar la alarma", Toast.LENGTH_SHORT).show()
        }
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