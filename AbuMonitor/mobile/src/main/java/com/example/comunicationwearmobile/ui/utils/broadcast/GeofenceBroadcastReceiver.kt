package com.example.comunicationwearmobile.ui.utils.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.JoinAreaGeofence
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDispatcherWearable
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
import java.time.LocalDate
import java.time.LocalTime
import java.time.Duration
import java.time.Instant

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var enteredInSecurityZone = false
    private var timeThatEnteredSecurityZone: Instant? = null

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
        var timeThatExitSecurityZone: Instant? = null
        val msg=SharedData.MsgNotification()

        if(transition==Geofence.GEOFENCE_TRANSITION_ENTER){
            enteredInSecurityZone=true
            timeThatEnteredSecurityZone=Instant.parse(LocalTime.now().toString())

            msg.typeNotification = SharedData.TypeNotification.Alert
            msg.title="¡Alerta de Seguridad!"
            msg.message= "El abuelo ha entrado en la zona segura ${areaGeof.areaGeofence.description}"
            msg.hour = Tools.getHour(LocalTime.now())
            msg.date = Tools.getDate(LocalDate.now())

        }else if(transition==Geofence.GEOFENCE_TRANSITION_EXIT){
                if(enteredInSecurityZone) {
                    timeThatExitSecurityZone = Instant.parse(LocalTime.now().toString())

                    val duration=Duration.between(timeThatEnteredSecurityZone,timeThatExitSecurityZone)

                    val days=duration.toDays()
                    val hours=duration.minusDays(days).toHours()
                    val minutes=duration.minusMinutes(hours).toMinutes()

                   // if(minutes>=Definition)
                }

        }

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
