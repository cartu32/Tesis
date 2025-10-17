package com.example.comunicationwearmobile.ui.utils.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.model.pojo.JoinAreaGeofence
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDispatcherWearable
import com.example.comunicationwearmobile.ui.model.repository.RepositoryGeofActivate
import com.example.comunicationwearmobile.ui.model.repository.RepositoryScheduleAssistance
import com.example.comunicationwearmobile.ui.model.repository.RepositorySecurityZoneSPref
import com.example.comunicationwearmobile.ui.utils.Helpers.NotificationHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.SmsHelper
import com.example.comunicationwearmobile.ui.utils.Tools
import com.example.shared_library.SharedData
import com.google.android.gms.location.Geofence
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.Date
import java.util.Locale


//worker que trabaja la logica de cuando se detectan(activan) areas de geofence
//esta clase se llama desde GeofenceBrodacst
class GeofenceWorker(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private var geofLatitude: String=""
    private var geofLongitude: String=""

    override suspend fun doWork(): Result = withTimeoutOrNull(60_000) {
        val transition = inputData.getInt("transition", -1)
        val triggeringIds = inputData.getStringArray("triggering_ids")?.mapNotNull { it.toLongOrNull() } ?: return@withTimeoutOrNull Result.failure()

        val repository = RepositoryAreaDB(context)

        for (idAreaGeofence in triggeringIds) {
            val areaGeof = repository.getJoinAreaGeofence(idAreaGeofence)

            //alamaceno la longitud y latitud del area de geofence detectada

            geofLongitude= areaGeof?.areaGeofence?.longitude.toString()
            geofLatitude=areaGeof?.areaGeofence?.latitude.toString()

            when(areaGeof?.areaGeofence?.id_type_area){
                Definition.TYPE_AREA_ID_NORMAL ->analizeNormalZone(context, areaGeof, transition)
                Definition.TYPE_AREA_ID_SECURITY_ZONE ->analizeSecurityZone(context, areaGeof, transition)
                Definition.TYPE_AREA_ID_ASSISTANCE ->analizeAssistanceZone(context, areaGeof.areaGeofence.id_area, transition)
            }
        }

        Result.success()
    } ?: Result.failure()


    private fun analizeNormalZone(context: Context, areaGeof: JoinAreaGeofence?, transition: Int) {
        val msg = areaGeof?.areaGeofence?.let {
            createMsg(transition, it.description, it.dwell_time)
        } ?: return
        determineRecipientByPriority(context, areaGeof.areaGeofence.id_priority, msg)
    }

    private fun analizeSecurityZone(context: Context, areaGeof: JoinAreaGeofence, transition: Int) {

        when (transition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                processEnterSecurityZone(context, areaGeof.areaGeofence.description)
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                processExitSecurityZone(
                    context,
                    areaGeof.areaGeofence.description,
                    areaGeof.securityZoneTimeRange?.min_hour,
                    areaGeof.securityZoneTimeRange?.max_hour
                )
            }
        }
    }

    private suspend fun analizeAssistanceZone(context: Context, idArea: Long, transition: Int) {


        when (transition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                processEnterAssistenceZone(context,idArea)
            }

            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                proccessExitAssistanceZone(context,idArea)
            }
        }
    }

    private suspend fun processEnterAssistenceZone(context: Context, idArea: Long) {
        val repositoryScheduleAssistance=RepositoryScheduleAssistance(context)
        val entityAssistance=repositoryScheduleAssistance.getAssistanceWithAreaId(idArea)

        with(entityAssistance){
            //pregunto si la persona ya asistio a la cita
            if(went_appointment){
                Log.d(Definition.TAG_DEBUG,"Ya asistio a la cita")
                return
            }

            //pregunto si la fecha de la cita es para el dia de hoy
            if(!Tools.isToday(date_hour_appointment)){
                Log.e(Definition.TAG_DEBUG,"Error en la fecha de la cita")
                return
            }

            //si es para el dia hoy, pregunto si esta la persona dentro del horario de la cita
            if(!Tools.isTimeEnterAssistanceCorrect(date_hour_appointment)){
                Log.d(Definition.TAG_DEBUG,"Se descarta la entrada porque no esta dentro del horario de la cita")
                return
            }

            date_hour_enter_assistance=System.currentTimeMillis()

            val respUpdate=repositoryScheduleAssistance.updateScheduleAssistance(entityAssistance)

            if(respUpdate==1){
                Log.d(Definition.TAG_DEBUG,"Hora de entrada de la cita actualizada")
            }else{
                Log.e(Definition.TAG_DEBUG,"Error no se pudo actualizar la cita")
            }
        }
    }

    private suspend fun proccessExitAssistanceZone(context: Context, idArea: Long) {
        val repositoryGeofActivate= RepositoryGeofActivate()

        val repositoryScheduleAssistance=RepositoryScheduleAssistance(context)
        val entityAssistance=repositoryScheduleAssistance.getAssistanceWithAreaId(idArea)
        val minuteInMillis=60000L

        with(entityAssistance) {
            //pregunto si la persona ya asistio a la cita
            if (went_appointment) {
                Log.d(Definition.TAG_DEBUG, "Ya asistio a la cita")
                return
            }
            //si la persona todavia no ingreso en el horario que debia ingresar se descarta el evento
            if(date_hour_enter_assistance==0L){
                Log.d(Definition.TAG_DEBUG,"La persona todavia no ingreso a la zona de asistencia en el horario agendado")
                return
            }

            val hourExit=System.currentTimeMillis()
            //conveirto el tiempo que estuvo en la zona de asistencia a minutos
            val timeInAssitanceZone = (hourExit - date_hour_enter_assistance)/minuteInMillis

            //si la persona menos de un minuto en la zona de asistencia descartamos el evento
            if(timeInAssitanceZone<Definition.TIME_MIN_IN_ASSISTANCE_ZONE){
                Log.d(Definition.TAG_DEBUG,"Se descarta la salida porque estuvo menos de ${Definition.TIME_MIN_IN_ASSISTANCE_ZONE} minutos")
                return
            }

            //si la persona estuvo mas de un minuto en la zona de asistencia se lo considera como que asistio a la cita
            Log.d(Definition.TAG_DEBUG,"La persona asistio a la cita, estuvo mas de ${Definition.TIME_MIN_IN_ASSISTANCE_ZONE} minutos en la zona de asistencia")

            //guardo en la base de datos la hora de salida de la cita e indico que asistio a la cita
            date_hour_exit_assistance=hourExit
            went_appointment=true
            is_activated_geof =false
            val respUpdate=repositoryScheduleAssistance.updateScheduleAssistance(entityAssistance)

            if(respUpdate==1){
                //notifico al contacto de que el abuelo asistio a la cita
                val msg = SharedData.MsgNotification().apply {
                    typeNotification = SharedData.TypeNotification.Alert
                    title = "¡Notificacion de Asistencia!"
                    message = "El abuelo ha asistido a la cita de $description"
                    hour = Tools.getHour(LocalTime.now())
                    date = Tools.getDate(LocalDate.now())
                }
                notifyUserPriorityBaja(context, msg)

                //como ya se asitio a la cita desactivo el area de geofence
                repositoryGeofActivate.desactivateGeofence(context, id_area.toString())
                Log.d(Definition.TAG_DEBUG,"Hora de salida de la cita actualizada")
            }else{
                Log.e(Definition.TAG_DEBUG,"Error no se pudo actualizar la cita")
            }

        }

    }

    private fun processEnterSecurityZone(context: Context, description: String) {
        val msg = SharedData.MsgNotification().apply {
            typeNotification = SharedData.TypeNotification.Alert
            title = "¡Alerta de Seguridad!"
            message = "El abuelo ha entrado en la zona segura $description"
            hour = Tools.getHour(LocalTime.now())
            date = Tools.getDate(LocalDate.now())
        }

        RepositorySecurityZoneSPref.getInstance(context).saveEnteredHour(System.currentTimeMillis())
        notifyUserPriorityBaja(context, msg)
    }

    /********************************************************************
    * Método que se ejecuta al salir de una zona segura:
    *
    * 1) Si estuvo menos de 1 minuto → se descarta el evento.
    * 2) Si estuvo entre X y Z minutos → se notifica salida inesperada.
    * 3) Si estuvo más de Z minutos:
    *   a) Si salió fuera del horario seguro → se notifica salida fuera de horario.
    *   b) Si salió dentro del horario seguro → se notifica salida dentro del horario.
    ********************************************************************/

    private fun processExitSecurityZone(context: Context, description: String, minHour: String?, maxHour: String?) {
        val prefs = RepositorySecurityZoneSPref.getInstance(context)
        val entryHour = prefs.getEnteredHour()
        val exitHour = System.currentTimeMillis()
        var msgSMS=""
        var msg:SharedData.MsgNotification

        if (entryHour == -1L) return

        val durationMin = Duration.ofMillis(exitHour - entryHour).toMinutes()
        Log.d(Definition.TAG_DEBUG,"entra en processExitSecurityZone")

        //si estuvo menos de 1 minuto descartamos el evento
        if (durationMin < Definition.TIME_MIN_CIRCUMSTANTIAL_DURATION_SECURITY_ZONE) {
            Log.d(Definition.TAG_DEBUG, "No cumplio el quantum.El abuelo ha salido de la zona segura $description dentro del rango horario norma")
            return
        }
        //si estuvo mas de 1 minuto y menor a 3 minutos notificamos la salida inesperada
        if (durationMin < Definition.TIME_MAX_CIRCUMSTANTIAL_DURATION_SECURITY_ZONE) {

            msgSMS="El abuelo ha salido inesperadamente de la zona segura $description"
            msg=createMsgSecurityZone(msgSMS)

        } else {
            val exitTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(exitHour))
            val isOutOfRange = Tools.isOutsideTimeRange(exitTime, minHour.toString(), maxHour.toString())

            //si estuvo mas de 3 minutos y esta fuera de horario notificamos la salida fuera de horario
            if (isOutOfRange) {
                msgSMS="El abuelo ha salido de la zona segura $description fuera del rango horario normal"
                Log.d(Definition.TAG_DEBUG,msgSMS)

                msg=createMsgSecurityZone(msgSMS)

            } else {
                //si estuvo mas de 3 minutos y esta dentro de horario notificamos la salida dentro de horario
                msgSMS="El abuelo ha salido de la zona segura $description dentro del rango horario normal"
                Log.d(Definition.TAG_DEBUG,msgSMS)

                msg=createMsgSecurityZone(msgSMS)
            }
        }

        //enviamos la notificacion por sms
        notifyUserPriorityBaja(context, msg)
        prefs.clearSharedPreferences()

        Log.d(Definition.TAG_DEBUG,msgSMS)
    }





    private fun createMsgSecurityZone(msg: String): SharedData.MsgNotification {

        return SharedData.MsgNotification().apply {
            typeNotification = SharedData.TypeNotification.Alert
            title = "¡Alerta de Seguridad!"
            message = msg
            hour = Tools.getHour(LocalTime.now())
            date = Tools.getDate(LocalDate.now())
        }
    }


    private fun createMsg(transition: Int?, description: String?, dwellTime: Int): SharedData.MsgNotification {

        val completeMsg = SharedData.MsgNotification().apply {
            hour = Tools.getHour(LocalTime.now())
            date = Tools.getDate(LocalDate.now())
            typeNotification = SharedData.TypeNotification.Alert
            title = "¡Alerta de Geofence!"
            message = when (transition) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> "Usted ha entrado en la zona $description "
                Geofence.GEOFENCE_TRANSITION_EXIT -> "Usted ha salido de la zona $description "
                Geofence.GEOFENCE_TRANSITION_DWELL -> "Usted estuvo más de $dwellTime min. en la zona $description "
                else -> "Evento desconocido en zona $description"
            }
        }
        return completeMsg
    }

    private fun notifyUserPriorityBaja(context: Context, msg: SharedData.MsgNotification) {
        val smsManagerCustom=SmsHelper()

        smsManagerCustom.sendSMSNotifyGeofence(context, msg,geofLatitude,geofLongitude)

    }

    private fun notifyUserPriorityMedia(context: Context, msg: SharedData.MsgNotification): Int? {
        val notificationHelper = NotificationHelper.getInstance(context)
        val id = notificationHelper?.showNotificationGeneral(msg)
        notifyUserPriorityBaja(context, msg)
        return id
    }

    private fun notifyUserPriorityAlta(context: Context, msg: SharedData.MsgNotification) {
        val idMsg = notifyUserPriorityMedia(context, msg)
        if (idMsg != null) {
            msg.idMsgMobile = idMsg
            RepositoryDispatcherWearable.sendDataToWearable(
                context,
                SharedData.PATH_ADD_NOTIFICATION_GENERAL,
                msg
            )
        }
    }

    private fun determineRecipientByPriority(context: Context, idPriority: Int?, msg: SharedData.MsgNotification) {
        when (idPriority) {
            Definition.PRIORITY_ID_LOW -> notifyUserPriorityBaja(context, msg)
            Definition.PRIORITY_ID_MEDIUM -> notifyUserPriorityMedia(context, msg)
            Definition.PRIORITY_ID_HIGH -> notifyUserPriorityAlta(context, msg)
            else -> Log.e(Definition.TAG_DEBUG, "No se encontró el id de prioridad")
        }
    }
}
