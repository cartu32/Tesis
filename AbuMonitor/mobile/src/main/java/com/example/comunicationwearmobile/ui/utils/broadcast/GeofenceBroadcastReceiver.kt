package com.example.comunicationwearmobile.ui.utils.broadcast

import android.content.BroadcastReceiver
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDispatcherWearable
import com.example.comunicationwearmobile.ui.utils.Mannager.NotificationManagerHelper
import com.example.comunicationwearmobile.ui.utils.Tools
import com.example.shared_library.SharedData
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent) {
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
        val repositoryAreaDB:RepositoryAreaDB= RepositoryAreaDB(context,scope)

        geofencingEvent?.triggeringGeofences?.forEach { geofence ->
            val msg:SharedData.MsgNotification
            val transition = geofencingEvent.geofenceTransition
            val idAreaGeofence = geofence.requestId.toLong()

            val areaGeof=repositoryAreaDB.getJoinAreaGeofence(idAreaGeofence)

            msg = createMsg(transition,areaGeof?.areaGeofence?.description)

            //envia la notificaciones al usuario
            notifyUser(context.applicationContext, msg)
        }

    }

    private  fun notifyUser( context: Context, msg: SharedData.MsgNotification) {
        val notificationHelper = NotificationManagerHelper.getInstance(context)

        //muestro la notificacion al usuario en la bandeja de notificacion del telefono
        //y obtengo su id para poder enviarselo al samrtwatch
        val idMsgMobile=notificationHelper?.showNotificationGeneral(msg)

        //el id de la notificacion se la agrego al mesnaje que lo envio al smartwatch
        if (idMsgMobile != null) {
            msg.idMsgMobile=idMsgMobile
        }

        RepositoryDispatcherWearable.sendDataToWearable(context,SharedData.PATH_ADD_NOTIFICATION_GENERAL,msg)
    }

    private fun createMsg(transition: Int?, description: String?): SharedData.MsgNotification {
        val msg=SharedData.MsgNotification()

        msg.hour = Tools.getHour(LocalTime.now())
        msg.date = Tools.getDate(LocalDate.now())

        when (transition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                msg.typeNotification = SharedData.TypeNotification.Alert
                msg.title="¡Alerta de Geofence!"
                msg.message= "Has entrado en la zona $description"

                Log.d(Definition.TAG_DEBUG, "Entraste en un geofence")

            }

            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                msg.typeNotification = SharedData.TypeNotification.Alert
                msg.title="¡Alerta de Geofence!"
                msg.message="Has salido de la zona $description"

                Log.d(Definition.TAG_DEBUG, "Saliste de un geofence")

            }

            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                msg.typeNotification = SharedData.TypeNotification.Alert
                msg.title="¡Alerta de Geofence!"
                msg.message= "Tiempo de permanencia en la zona $description"

                Log.d(Definition.TAG_DEBUG, "Tiempo de permanencia en un geofence")

            }

        }
        return msg
    }
}
