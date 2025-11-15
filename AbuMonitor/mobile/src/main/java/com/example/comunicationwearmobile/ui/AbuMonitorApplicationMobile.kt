package com.example.abumonitor


import android.app.Application
import android.util.Log
import android.widget.Toast
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.datasource.local.dbInitializer
import com.example.comunicationwearmobile.ui.model.repository.RepositoryScheduleAlarmSPref
import com.example.comunicationwearmobile.ui.utils.Helpers.AlarmHelper
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
        initializeAlarm()
        initilizeSPRememberAppointment()
        configLeakCanary()

      }

    private fun initilizeSPRememberAppointment() {
        var hourTimeRemember=Definition.DEFAULT_HOUR_REMEMER_APPOINTMENT
        val repository = RepositoryScheduleAlarmSPref.getInstance(this)

        val timeRememberAppointment=repository.getTimeRememberAppointmentSync()

        if (timeRememberAppointment != Definition.NO_STORED_VALUE) {
            val (h, m) = Tools.getHourMinOfParcial(timeRememberAppointment)
            hourTimeRemember   = h
        } else {
            Log.w(Definition.TAG_DEBUG, "No hay intervalo configurado para los chequeos. Uso valores por defecto.")

            val timeParcialMillis=Tools.getTimeInMillis(hourTimeRemember, 0)
            repository.saveTimeRememberAppointmentSync(timeParcialMillis)
        }
    }


    private fun initilizerDB() {
        val dbInitializer = dbInitializer()

        // Iniciar la base de datos en background
        CoroutineScope(Dispatchers.Default).launch {
            dbInitializer.checkAndInitDatabase(applicationContext)
        }
    }

    private fun initializeAlarm() {
        val repository = RepositoryScheduleAlarmSPref.getInstance(this)
        val alarmHelper = AlarmHelper()

        // Valores por defecto
        var hourAlarmBetweenCheck   = Definition.DEFAULT_HOUR_ALARM_BETWEEN_CHECKS
        var minuteAlramBetweenCheck = Definition.DEFAULT_MINUTE_ALARM_BETWEEN_CHECKS

        val timeBetweenChecks = repository.getTimeBetweenChecksSync()

        if (timeBetweenChecks != Definition.NO_STORED_VALUE) {
            val (h, m) = Tools.getHourMinOfParcial(timeBetweenChecks)
            hourAlarmBetweenCheck   = h
            minuteAlramBetweenCheck = m
        } else {
            Log.w(Definition.TAG_DEBUG, "No hay intervalo configurado para los chequeos. Uso valores por defecto.")

            val timeParcialMillis=Tools.getTimeInMillis(hourAlarmBetweenCheck, minuteAlramBetweenCheck)
            repository.saveTimeBetweenChecksSync(timeParcialMillis)
        }

        cancelAlarmPrevious(alarmHelper)
        initAlarm(hourAlarmBetweenCheck, minuteAlramBetweenCheck,alarmHelper)

    }

    private fun cancelAlarmPrevious(alarmHelper: AlarmHelper) {
        alarmHelper.cancelAlarm(
            this,
            Definition.ALARM_ID_BETWEEN_CHECKS,
            Definition.ACTION_ALARM_FOR_CHECKS,
            AlarmDailyForChecksBroadcastReceiver::class.java
        )
    }

    private fun initAlarm(hour: Int, minute: Int,alarmHelper: AlarmHelper){

        val resultSetAlarm = alarmHelper.setAlarmAfterOfTime(
            this,
            Definition.ALARM_ID_BETWEEN_CHECKS,
            hour,
            minute,
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

}

