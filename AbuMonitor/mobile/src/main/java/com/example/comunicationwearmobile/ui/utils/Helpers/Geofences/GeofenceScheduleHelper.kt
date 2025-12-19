package com.example.comunicationwearmobile.ui.utils.Helpers.Geofences

import android.content.Context
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityScheduledAssistance
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.common.SharedVariables
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDispatcherWearable
import com.example.comunicationwearmobile.ui.model.repository.RepositoryConfigAppSPref
import com.example.comunicationwearmobile.ui.model.repository.RepositoryScheduleAssistance
import com.example.comunicationwearmobile.ui.utils.Helpers.Alarm.AlarmHelper.cancelAlarm
import com.example.comunicationwearmobile.ui.utils.Helpers.Alarm.AlarmHelper.setNextAlarmAtExactTime
import com.example.comunicationwearmobile.ui.utils.Helpers.Notification.NotificationHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.Notification.SmsHelper
import com.example.comunicationwearmobile.ui.utils.Tools
import com.example.comunicationwearmobile.ui.utils.broadcast.AlarmBroadcastReceiver
import com.example.shared_library.SharedData
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class TimeWindow(val start: Long, val end: Long)

class GeofenceScheduleHelper(mContext:Context) {

    private val context=mContext.applicationContext
    private var repositoryScheduleAssistance:RepositoryScheduleAssistance
    private var repositoryConfigAppSPref: RepositoryConfigAppSPref
    private var repositoryAreaDB:RepositoryAreaDB


    init {
        repositoryScheduleAssistance=RepositoryScheduleAssistance.getInstance(context)
        repositoryConfigAppSPref= RepositoryConfigAppSPref.getInstance(context)
        repositoryAreaDB=RepositoryAreaDB.getInstance(context)
    }

    suspend fun activateGeofenceScheduled(timeCurrentAlarm: Long)=
        SharedVariables.mutexAssistanceDateAlarm.withLock {

        // 1) Activo las áreas cuya cita es exactamente la de esta alarma (pueden ser varias)
        activateGeofence(timeCurrentAlarm)

        // 2) Reprogramo la próxima alarma usando como ancla el horario lógico de esta alarma,
        scheduledNextStartAlarmAppointment(timeCurrentAlarm)
    }


    suspend fun deactivateGeofenceScheduled(timeCurrentAlarm: Long)=
        SharedVariables.mutexAssistanceDateAlarm.withLock {
        // 1) Desactivo las áreas cuya cita es exactamente la de esta alarma (pueden ser varias)
        desactivateGeofence(timeCurrentAlarm)

        // 2) Reprogramo la próxima alarma usando como ancla el horario lógico de esta alarma,
        scheduledNextEndAlarmAppointment(timeCurrentAlarm)
    }

    private suspend fun scheduledNextEndAlarmAppointment(lastAlarmTime: Long) {
        val nextEnd =repositoryScheduleAssistance.getEndTimeOfNextAppointment(initIntervalAlarma = lastAlarmTime)
                ?: return

        setNextAlarmAtExactTime(
            context = context,
            alarmId = Definition.ALARM_ID_FOR_DESACTIVATION_AREAS,
            triggerAtMillis = nextEnd,
            action = Definition.ACTION_ALARM_FOR_DESACTIVATION_AREA,
            receiverClass = AlarmBroadcastReceiver::class.java
        )
    }

    private suspend fun scheduledNextStartAlarmAppointment(lastAlarmTime: Long) {

        // Busco la próxima cita estrictamente posterior a la que acabo de procesar
        val timeNextAppointment =
            repositoryScheduleAssistance.getStartTimeOfNextAppointment(lastAlarmTime)
                ?: return

        setNextAlarmAtExactTime(
            context = context,
            alarmId = Definition.ALARM_ID_FOR_ACTIVATION_AREAS,
            triggerAtMillis = timeNextAppointment,
            action = Definition.ACTION_ALARM_FOR_ACTIVATION_AREA,
            receiverClass = AlarmBroadcastReceiver::class.java
        )
    }


    private suspend fun activateGeofence(timeCurrentAlarm: Long):Boolean{
        Log.d(Definition.TAG_DEBUG,"Alarma de activacion de geofences programadas ejecutada")


        val resultUpdate=repositoryScheduleAssistance.activateNextAppointmentArea(timeCurrentAlarm)

        if (resultUpdate==0)
            return false
        return true

    }


    private suspend fun desactivateGeofence(timeCurrentAlarm: Long):Boolean{
        Log.d(Definition.TAG_DEBUG,"Alarma de desactivacion de geofences programadas ejecutada")


        val resultUpdate=repositoryScheduleAssistance.desactivateNextAppointmentArea(timeCurrentAlarm)

        if (resultUpdate==0)
            return false
        return true

    }


    suspend fun reportInassistanceScheduled(listAppointWithoutAssisntace:List<EntityScheduledAssistance>) {

        //se genera un resumen de las citas a la que no asistio la persona en el dia de la fecha
        val msg=generateMessageInTable(listAppointWithoutAssisntace)
        Log.d(Definition.TAG_DEBUG, msg)

        //Envio SMS notificando el problema
        SmsHelper.sendSMSPlainText(context,msg)
    }

    private fun generateMessageInTable(listAppointWithoutAssisntace: List<EntityScheduledAssistance>): String {

        val message = buildString {
            appendLine("Citas sin asistencia:")
            appendLine("")
            appendLine("--------------------------------")
            appendLine("Descripción         Hora de cita")
            appendLine("--------------------------------")
            for (appointment in listAppointWithoutAssisntace) {
                val time = Tools.getMillisToHourMinutes(appointment.date_hour_appointment)
                val desc = appointment.description.padEnd(23) // ajustá este valor según el largo máximo esperado
                appendLine("$desc $time")
            }
        }
        return message


    }

    suspend  fun checkRememberAppointmentInsideInterval(prev: TimeWindow, curr: TimeWindow) {
        //obtengo el intervalo de tiempo en que se va a recordar las citas
        val timePreviousRemeber=repositoryConfigAppSPref.getTimeRememberAppointment()

        //obtengo de la bd el listado de citas a las que se debe recordar
        var listAreasWithPreviousRemember:List<EntityScheduledAssistance>?=null

        listAreasWithPreviousRemember=repositoryScheduleAssistance.getAreasWithAppointmentRemember(timePreviousRemeber,curr.start,curr.end)

        if(listAreasWithPreviousRemember.isEmpty()){
            Log.d(Definition.TAG_DEBUG,"No hay citas a las que se debe recordar")
            return
        }

        for(date in listAreasWithPreviousRemember){
            notifyRemeberUser(date)
        }
    }

    private fun notifyRemeberUser(dateRemember: EntityScheduledAssistance)
    {
        //creo el mensaje del Recordatorio
        val msg = SharedData.MsgNotification().apply {
            typeNotification = SharedData.TypeNotification.Reminder
            title = "Recordatorio de Cita"
            message = dateRemember.description
            hour = Tools.getMillisToHourMinutes(dateRemember.date_hour_appointment)
            date = Tools.getMillisToDate(dateRemember.date_hour_appointment)

        }

        Log.d(Definition.TAG_DEBUG,msg.toString())

        val idMsg=showReminderInSmarthpone(msg)
        showReminderInWearable(idMsg,msg)
    }

    private fun showReminderInWearable(idMsg: Int?, msg: SharedData.MsgNotification) {
        if (idMsg != null) {
            msg.idMsgMobile = idMsg
            RepositoryDispatcherWearable.sendDataToWearable(
                context,
                SharedData.PATH_ADD_NOTIFICATION_GENERAL,
                msg
            )
        }

    }

    private fun showReminderInSmarthpone(msg: SharedData.MsgNotification): Int? {
        val notificationHelper = NotificationHelper.getInstance(context)
        val id = notificationHelper?.showNotificationGeneral(msg)

        return id

    }

}