package com.example.comunicationwearmobile.ui.utils.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.pojo.JoinAreaGeofence
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDispatcherWearable
import com.example.comunicationwearmobile.ui.model.repository.RepositorySecurityZoneSPref
import com.example.comunicationwearmobile.ui.utils.Helpers.NotificationHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.SmsHelper
import com.example.comunicationwearmobile.ui.utils.Tools
import com.example.shared_library.SharedData
import com.google.android.gms.location.Geofence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.Date
import java.util.Locale


//worker que trabaja la logica de cuando se detectan(activan) areas de geofence
//esta clase se llama desde GeofenceBrodacst
class GeofenceWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private var geofLatitude: String=""
    private var geofLongitude: String=""

    override suspend fun doWork(): Result = withTimeoutOrNull(60_000) {
        val transition = inputData.getInt("transition", -1)
        val triggeringIds = inputData.getStringArray("triggering_ids")?.mapNotNull { it.toLongOrNull() } ?: return@withTimeoutOrNull Result.failure()

        val repository = RepositoryAreaDB(context, CoroutineScope(Dispatchers.IO))

        for (idAreaGeofence in triggeringIds) {
            val areaGeof = repository.getJoinAreaGeofence(idAreaGeofence)

            //alamaceno la longitud y latitud del area de geofence detectada

            geofLongitude= areaGeof?.areaGeofence?.longitude.toString()
            geofLatitude=areaGeof?.areaGeofence?.latitude.toString()

            if (areaGeof?.areaGeofence?.id_type_area == Definition.TYPE_AREA_ID_SECURITY_ZONE) {
                analizeSecurityZone(context, areaGeof, transition)
            } else {
                analizeNormalZone(context, areaGeof, transition)
            }
        }

        Result.success()
    } ?: Result.failure()

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

    private fun processExitSecurityZone(context: Context, description: String, minHour: String?, maxHour: String?) {
        val prefs = RepositorySecurityZoneSPref.getInstance(context)
        val entryHour = prefs.getEnteredHour()
        val exitHour = System.currentTimeMillis()
        var msgSMS:String=""

        if (entryHour == -1L) return

        val durationMin = Duration.ofMillis(exitHour - entryHour).toMinutes()
        Log.d(Definition.TAG_DEBUG,"entra en processExitSecurityZone")

        if (durationMin > Definition.TIME_MIN_CIRCUMSTANTIAL_DURATION_SECURITY_ZONE) {
            val msg = if (durationMin < Definition.TIME_MAX_CIRCUMSTANTIAL_DURATION_SECURITY_ZONE) {

                msgSMS="El abuelo ha salido inesperadamente de la zona segura $description"
                createMsgSecurityZone(msgSMS)

            } else {
                val exitTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(exitHour))
                val isOutOfRange = Tools.isOutsideTimeRange(exitTime, minHour.toString(), maxHour.toString())

                if (isOutOfRange) {
                    msgSMS="El abuelo ha salido de la zona segura $description fuera del rango horario normal"
                    Log.d(Definition.TAG_DEBUG,msgSMS)

                    createMsgSecurityZone(msgSMS)

                } else {
                    msgSMS="El abuelo ha salido de la zona segura $description dentro del rango horario normal"
                    Log.d(Definition.TAG_DEBUG,msgSMS)

                    createMsgSecurityZone(msgSMS)
                }
            }

            notifyUserPriorityBaja(context, msg)
            prefs.clearSharedPreferences()
        }
        Log.d(Definition.TAG_DEBUG,"No cumplio el quantum.El abuelo ha salido de la zona segura $description dentro del rango horario norma")
    }

    private fun analizeNormalZone(context: Context, areaGeof: JoinAreaGeofence?, transition: Int) {
        val msg = areaGeof?.areaGeofence?.let {
            createMsg(transition, it.description, it.dwell_time)
        } ?: return
        determineRecipientByPriority(context, areaGeof.areaGeofence.id_priority, msg)
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
                Geofence.GEOFENCE_TRANSITION_ENTER -> "El abuelo ha entrado en la zona $description "
                Geofence.GEOFENCE_TRANSITION_EXIT -> "El abuelo ha salido de la zona $description "
                Geofence.GEOFENCE_TRANSITION_DWELL -> "El abuelo pasó más de $dwellTime min. en la zona $description "
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
