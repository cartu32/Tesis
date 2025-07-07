package com.example.comunicationwearmobile.ui.utils.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.JoinAreaGeofence
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDispatcherWearable
import com.example.comunicationwearmobile.ui.model.repository.RepositorySecurityZoneSPref
import com.example.comunicationwearmobile.ui.utils.Mannager.NotificationManagerHelper
import com.example.comunicationwearmobile.ui.utils.Tools
import com.example.comunicationwearmobile.ui.utils.services.GeofencesServices
import com.example.shared_library.SharedData
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.Duration
import java.util.Calendar
import java.util.Date
import java.util.Locale

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())


    override fun onReceive(mContext: Context, intent: Intent) {
        val context=mContext.applicationContext

        if (intent.action == "com.example.app.ACTION_GEOFENCE_EVENT") {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            if (geofencingEvent != null) {
                if (geofencingEvent.hasError()) {
                    Log.e(Definition.TAG_DEBUG, "Error en el Geofencing: ${geofencingEvent.errorCode}")
                    return
                 }
            }

            coroutineScope.launch {

                analizeDetectedGeofences(context, geofencingEvent,coroutineScope)
            }
        }
    }

    private suspend fun analizeDetectedGeofences(context: Context, geofencingEvent: GeofencingEvent?, scope: CoroutineScope) {
        val repositoryAreaDB= RepositoryAreaDB(context,scope)

        geofencingEvent?.triggeringGeofences?.forEach { geofence ->
            val transition = geofencingEvent.geofenceTransition
            val idAreaGeofence = geofence.requestId.toLong()
            val areaGeof=repositoryAreaDB.getJoinAreaGeofence(idAreaGeofence)

            if(areaGeof?.areaGeofence?.security_zone==true){
                analizeSecurityZone(context,areaGeof,transition)
            }else{
                analizeNormalZone(context,areaGeof,transition)
            }
        }

    }

    private fun analizeSecurityZone(context: Context, areaGeof: JoinAreaGeofence, transition: Int) {

        when(transition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                processEnterSecurityZone(context,areaGeof.areaGeofence.description)
            }

            Geofence.GEOFENCE_TRANSITION_EXIT ->
            {
                processExitSecurityZone(
                    context,
                    areaGeof.areaGeofence.description,
                    areaGeof.securityZoneTimeRange?.min_hour,
                    areaGeof.securityZoneTimeRange?.max_hour
                )
            }
        }

    }

    private fun processExitSecurityZone(
        context: Context,
        description: String,
        minHour: String?,
        maxHour: String?
    ) {
        val repositorySecurityZoneSPref= RepositorySecurityZoneSPref.getInstance(context)
        val entryHour=repositorySecurityZoneSPref.getEnteredHour()
        val exitHour=System.currentTimeMillis()
        val msg:SharedData.MsgNotification

        if(entryHour==-1L)
            return

        val durartionMs=exitHour-entryHour
        val durationMin= Duration.ofMillis(durartionMs).toMinutes()

        Log.d(Definition.TAG_DEBUG,"Salio zona segura duracionMin: $durationMin")

        //determino si la salida de la zona segura no fue esporadica. Si es asi
        //se envia un sms alertando de posible problema
        if(durationMin>Definition.TIME_MIN_CIRCUMSTANTIAL_DURATION_SECURITY_ZONE) {

            if (durationMin < Definition.TIME_MAX_CIRCUMSTANTIAL_DURATION_SECURITY_ZONE) {
                msg = createMsgCircumstantialExitSecurityZone(description)

                //envio el sms de alerta al contacto de emergencia
                notifyUserPriorityBaja(context, msg)

                repositorySecurityZoneSPref.clearSharedPreferences()
            }else {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                val exitHourString = sdf.format(Date(exitHour))

                //me fijo si la hora de salida esta dentro del rango de horas de la zona de seguridad
                if ((LocalTime.parse(exitHourString)<LocalTime.parse(maxHour.toString()))&&
                    (LocalTime.parse(exitHourString)>LocalTime.parse(minHour.toString())))    {

                    //si esta fuera del rango de horas normal
                    // para salir de la zona segura entonces se genera la notificacion
                    msg = createMsgExitSecurityZoneOutRange(description)

                    //envio el sms de alerta al contacto de emergencia
                    notifyUserPriorityBaja(context, msg)
                    repositorySecurityZoneSPref.clearSharedPreferences()

                }else{
                    //si sale de la zona de seguridad dentro del rango horario normal entonces
                    //no se hace nada
                    Log.d(Definition.TAG_DEBUG,"Fuera de rango horario de la zona de seguridad")
                }

            }
        }
   }

    private fun processEnterSecurityZone(context: Context, description: String) {
        val msg:SharedData.MsgNotification
        val repositorySecurityZoneSPref= RepositorySecurityZoneSPref.getInstance(context)

        //guardo la hora de entrada en la zona de seguridad
        repositorySecurityZoneSPref.saveEnteredHour(System.currentTimeMillis())

        msg = createMsgEnterdSecurityZone(description)

        //envio el sms de alerta al contacto de emergencia
        notifyUserPriorityBaja(context, msg)
    }

    private fun createMsgCircumstantialExitSecurityZone(description: String): SharedData.MsgNotification {
        val msg=SharedData.MsgNotification()

        msg.typeNotification = SharedData.TypeNotification.Alert
        msg.title="¡Alerta de Seguridad!"
        msg.message= "El abuelo ha salido inesperadamente de la zona segura $description"
        msg.hour = Tools.getHour(LocalTime.now())
        msg.date = Tools.getDate(LocalDate.now())

        return msg
    }

    private fun createMsgExitSecurityZoneOutRange(description: String): SharedData.MsgNotification {
        val msg=SharedData.MsgNotification()

        msg.typeNotification = SharedData.TypeNotification.Alert
        msg.title="¡Alerta de Seguridad!"
        msg.message= "El abuelo ha salido de la zona segura $description fuera del rango horario normal"
        msg.hour = Tools.getHour(LocalTime.now())
        msg.date = Tools.getDate(LocalDate.now())

        return msg
    }

    private fun createMsgEnterdSecurityZone(description: String): SharedData.MsgNotification {
        val msg=SharedData.MsgNotification()

        msg.typeNotification = SharedData.TypeNotification.Alert
        msg.title="¡Alerta de Seguridad!"
        msg.message= "El abuelo ha entrado en la zona segura $description"
        msg.hour = Tools.getHour(LocalTime.now())
        msg.date = Tools.getDate(LocalDate.now())

        return msg
    }
    private fun analizeNormalZone(context: Context, areaGeof: JoinAreaGeofence?, transition: Int) {
        val msg:SharedData.MsgNotification

        with(areaGeof?.areaGeofence){
            msg = createMsg(transition, this?.description, this?.dwell_time ?: 0)
        }

        //genera la notificacion segun l prioridad del geofence
        determineRecipientByPriority(context.applicationContext,areaGeof?.areaGeofence?.id_priority,msg)
    }

    private fun notifyUserPriorityBaja(context: Context, msg: SharedData.MsgNotification) {

        val intent = Intent(context, GeofencesServices::class.java).apply {
            putExtra(Definition.OPERATION_GOEFENCE_SEND_SMS,msg)
            putExtra(Definition.OPERATION_START_FOREGROUND_SERVICE,Definition.OPERATION_GOEFENCE_SEND_SMS)
        }

        context.startService(intent)
    }

    private fun notifyUserPriorityMedia(context: Context, msg: SharedData.MsgNotification): Int? {
        val notificationHelper = NotificationManagerHelper.getInstance(context)

        //muestro la notificacion al usuario en la bandeja de notificacion del telefono
        val idMsgMobile=notificationHelper?.showNotificationGeneral(msg)

        //le envio el SMS de alerta al contacto de emergencia
        notifyUserPriorityBaja(context,msg)

        return idMsgMobile
    }

    private fun determineRecipientByPriority(context: Context, idPriority: Int?, msg: SharedData.MsgNotification) {
        when(idPriority){
            Definition.PRIORITY_BAJA-> notifyUserPriorityBaja(context,msg)
            Definition.PRIORTY_MEDIA-> notifyUserPriorityMedia(context,msg)
            Definition.PRIORITY_ALTA-> notifyUserPriorityAlta(context,msg)
            else-> Log.e(Definition.TAG_DEBUG,"No se encontro el id de prioridad")
        }

    }

    private fun notifyUserPriorityAlta(context: Context, msg: SharedData.MsgNotification) {

        //envio el SMS al contacto de emergencia y muestro la notificacion al usuario
        //en la bandeja de notificaciones
        val idMsgMobile=notifyUserPriorityMedia(context, msg)

        //el id de la notificacion se la agrego al mesnaje que lo envio al smartwatch
        if (idMsgMobile != null) {
            msg.idMsgMobile = idMsgMobile

            //envio el mensaje al smartwatch
            RepositoryDispatcherWearable.sendDataToWearable(
                context,
                SharedData.PATH_ADD_NOTIFICATION_GENERAL,
                msg
            )
        }else{
            Log.e(Definition.TAG_DEBUG,"No se pudo enviar la notificacion al smartwatch")
        }
   }


    private fun createMsg(transition: Int?, description: String?, dwellTime: Int): SharedData.MsgNotification {
        val msg=SharedData.MsgNotification()

        msg.hour = Tools.getHour(LocalTime.now())
        msg.date = Tools.getDate(LocalDate.now())

        when (transition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                msg.typeNotification = SharedData.TypeNotification.Alert
                msg.title="¡Alerta de Geofence!"
                msg.message= "El abuelo ha entrado en la zona $description"

                Log.d(Definition.TAG_DEBUG, "Entraste en un geofence")

            }

            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                msg.typeNotification = SharedData.TypeNotification.Alert
                msg.title="¡Alerta de Geofence!"
                msg.message="El abuelo ha salido de la zona $description"

                Log.d(Definition.TAG_DEBUG, "Saliste de un geofence")

            }

            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                msg.typeNotification = SharedData.TypeNotification.Alert
                msg.title="¡Alerta de Geofence!"
                msg.message= "El abuelo paso más de $dwellTime min. en la zona $description"

                Log.d(Definition.TAG_DEBUG, "Tiempo de permanencia en un geofence")

            }

        }
        return msg
    }
}
