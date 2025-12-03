package com.example.abumonitor


import android.app.Application
import android.util.Log
import android.widget.Toast
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.common.SharedVariables
import com.example.comunicationwearmobile.ui.model.datasource.local.dbInitializer
import com.example.comunicationwearmobile.ui.model.repository.RepositoryConfigAppSPref
import com.example.comunicationwearmobile.ui.utils.Helpers.Alarm.AlarmHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.Alarm.GeofenceWatchdogScheduler
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.GeofenceEventProcessorHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.Notification.NotificationHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.Notification.SmsHelper
import com.example.comunicationwearmobile.ui.utils.Tools
import com.example.comunicationwearmobile.ui.utils.broadcast.AlarmDailyForChecksBroadcastReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//import leakcanary.LeakCanary


class AbuMonitorApplicationMobile : Application() {
    override fun onCreate() {
        super.onCreate()

        initilizerDB()
        initializeAlarms()
        initilizeSPRememberAppointment()
        initSmsHelper()
        initGeofenceEventProcessorHelper()
        configLeakCanary()

      }


    private fun initSmsHelper() {
        SmsHelper.registerSMSReceivers(this)
    }

    private fun initGeofenceEventProcessorHelper() {
        GeofenceEventProcessorHelper.init(this)
    }

    private fun initilizeSPRememberAppointment() {
        var hourTimeRemember    = Definition.DEFAULT_HOUR_REMEMER_APPOINTMENT
        var minuteTimeRemember  = Definition.DEFAULT_MINUTE_REMEMER_APPOINTMENT
        val repository = RepositoryConfigAppSPref.getInstance(this)

        val timeRememberAppointment=repository.getTimeRememberAppointmentSync()

        if (timeRememberAppointment == Definition.NO_STORED_VALUE) {
            Log.w(Definition.TAG_DEBUG, "No hay tiempo configurado para recordar cita. Uso valores por defecto.")

            val timeParcialMillis=Tools.getTimeInMillis(hourTimeRemember, minuteTimeRemember)
            repository.saveTimeRememberAppointmentSync(timeParcialMillis)
        }
    }


    private fun initializeAlarms() {
        //initializeAlarmWatchdog()
        initializeAlarmAssistance()
    }

    private fun initializeAlarmWatchdog() {
        GeofenceWatchdogScheduler.scheduleNext(this,reason="FROM_APPLICATION_ONCREATE")

    }

    private fun initilizerDB() {
        val dbInitializer = dbInitializer()

        // Iniciar la base de datos en background
        CoroutineScope(Dispatchers.Default).launch {
            dbInitializer.checkAndInitDatabase(applicationContext)
        }
    }

    private fun initializeAlarmAssistance() {

        val repository  = RepositoryConfigAppSPref.getInstance(this)


        //leo del SharedPreferences la hora de la alarma de chequeo
        val storedMillis = repository.getTimeBetweenChecksSync()

        //obtengo la hora, minutos de la alarma
        val intervalMillis = getIntervalForFirsTimeAlarm(storedMillis,repository)

        //cada vez que se inicia la app se vuelve a configurar una nueva alarama
        SharedVariables.timeAlarmChecksFirstTime = Tools.extractHourOfDateInMillis(System.currentTimeMillis() + intervalMillis)
        SharedVariables.isOpenAppFirsTime=true

        initAlarmAssistance (intervalMillis)
    }

    private fun getIntervalForFirsTimeAlarm(storedMillis: Long, repository:RepositoryConfigAppSPref): Long{

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


    private fun initAlarmAssistance(intervalToAlarmMS: Long){

        val resultSetAlarm = AlarmHelper.setNextAlarmInXTime(
            this,
            Definition.ALARM_ID_FOR_CHECKS,
            intervalToAlarmMS,
            Definition.ACTION_ALARM_FOR_CHECKS,
            AlarmDailyForChecksBroadcastReceiver::class.java
        )

        if (resultSetAlarm) {
            Log.d(Definition.TAG_DEBUG, "Alarma de checkeo configurada correctamente")
            Toast.makeText(this, "Alarma de checkeo configurada correctamente", Toast.LENGTH_SHORT).show()
        } else {
            Log.e(Definition.TAG_DEBUG, "No se pudo configurar la alarma")
            Toast.makeText(this, "No se pudo configurar la alarma", Toast.LENGTH_SHORT).show()
        }

    }

    private fun configLeakCanary() {
        // Configuración adicional si es necesario
        //   LeakCanary.config = LeakCanary.config.copy(dumpHeap = false)
    }

    override fun onTerminate() {
        super.onTerminate()
        SmsHelper.onDestroy()
        NotificationHelper.getInstance(this)?.cancelCorutineInit()

    }

}

