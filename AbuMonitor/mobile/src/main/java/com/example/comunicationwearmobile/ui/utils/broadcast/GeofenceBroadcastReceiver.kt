package com.example.comunicationwearmobile.ui.utils.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
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
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.Date
import java.util.Locale

// Reemplazo de la corutina por Worker en el BroadcastReceiver
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext

        if (intent.action == "com.example.app.ACTION_GEOFENCE_EVENT") {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            if (geofencingEvent == null || geofencingEvent.hasError()) {
                Log.e(Definition.TAG_DEBUG, "Error en el Geofencing: ${geofencingEvent?.errorCode}")
                return
            }

            // Serializamos el evento (guardamos los IDs y el tipo de transición)
            val triggeringIds = geofencingEvent.triggeringGeofences?.map { it.requestId }?.toTypedArray()
            val transition = geofencingEvent.geofenceTransition

            val inputData = workDataOf(
                "triggering_ids" to triggeringIds,
                "transition" to transition
            )

            val workRequest = OneTimeWorkRequestBuilder<GeofenceWorker>()
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(appContext).enqueueUniqueWork(
                "trabajo_geofence",
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            )
        }
    }
}

// Nuevo Worker que reemplaza la lógica original en segundo plano
class GeofenceWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withTimeoutOrNull(60_000) {
        val transition = inputData.getInt("transition", -1)
        val triggeringIds = inputData.getStringArray("triggering_ids")?.mapNotNull { it.toLongOrNull() } ?: return@withTimeoutOrNull Result.failure()

        val repository = RepositoryAreaDB(context, CoroutineScope(Dispatchers.IO))

        for (idAreaGeofence in triggeringIds) {
            val areaGeof = repository.getJoinAreaGeofence(idAreaGeofence)

            if (areaGeof?.areaGeofence?.security_zone == true) {
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
        if (entryHour == -1L) return

        val durationMin = Duration.ofMillis(exitHour - entryHour).toMinutes()
        Log.d(Definition.TAG_DEBUG,"entra en processExitSecurityZone")
        
        if (durationMin > Definition.TIME_MIN_CIRCUMSTANTIAL_DURATION_SECURITY_ZONE) {
            val msg = if (durationMin < Definition.TIME_MAX_CIRCUMSTANTIAL_DURATION_SECURITY_ZONE) {
                createMsgCircumstantialExitSecurityZone(description)
            } else {
                val exitTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(exitHour))
                val isOutOfRange =Tools.isOutsideTimeRange(exitTime, minHour.toString(), maxHour.toString())

                if (isOutOfRange) {
                    Log.d(Definition.TAG_DEBUG,"El abuelo ha salido de la zona segura $description fuera del rango horario normal")
                    createMsgExitSecurityZoneOutRange(description)
                } else {
                    Log.d(Definition.TAG_DEBUG,"El abuelo ha salido de la zona segura $description dentro del rango horario normal")
                    return
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

    private fun createMsgCircumstantialExitSecurityZone(description: String): SharedData.MsgNotification =
        SharedData.MsgNotification().apply {
            typeNotification = SharedData.TypeNotification.Alert
            title = "¡Alerta de Seguridad!"
            message = "El abuelo ha salido inesperadamente de la zona segura $description"
            hour = Tools.getHour(LocalTime.now())
            date = Tools.getDate(LocalDate.now())
        }

    private fun createMsgExitSecurityZoneOutRange(description: String): SharedData.MsgNotification =
        SharedData.MsgNotification().apply {
            typeNotification = SharedData.TypeNotification.Alert
            title = "¡Alerta de Seguridad!"
            message = "El abuelo ha salido de la zona segura $description fuera del rango horario normal"
            hour = Tools.getHour(LocalTime.now())
            date = Tools.getDate(LocalDate.now())
        }

    private fun createMsg(transition: Int?, description: String?, dwellTime: Int): SharedData.MsgNotification =
        SharedData.MsgNotification().apply {
            hour = Tools.getHour(LocalTime.now())
            date = Tools.getDate(LocalDate.now())
            typeNotification = SharedData.TypeNotification.Alert
            title = "¡Alerta de Geofence!"
            message = when (transition) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> "El abuelo ha entrado en la zona $description"
                Geofence.GEOFENCE_TRANSITION_EXIT -> "El abuelo ha salido de la zona $description"
                Geofence.GEOFENCE_TRANSITION_DWELL -> "El abuelo pasó más de $dwellTime min. en la zona $description"
                else -> "Evento desconocido en zona $description"
            }
        }

    private fun notifyUserPriorityBaja(context: Context, msg: SharedData.MsgNotification) {
        val intent = Intent(context, GeofencesServices::class.java).apply {
            putExtra(Definition.OPERATION_GOEFENCE_SEND_SMS, msg)
            putExtra(Definition.OPERATION_START_FOREGROUND_SERVICE, Definition.OPERATION_GOEFENCE_SEND_SMS)
        }
        context.startService(intent)
    }

    private fun notifyUserPriorityMedia(context: Context, msg: SharedData.MsgNotification): Int? {
        val notificationHelper = NotificationManagerHelper.getInstance(context)
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
            Definition.PRIORITY_BAJA -> notifyUserPriorityBaja(context, msg)
            Definition.PRIORTY_MEDIA -> notifyUserPriorityMedia(context, msg)
            Definition.PRIORITY_ALTA -> notifyUserPriorityAlta(context, msg)
            else -> Log.e(Definition.TAG_DEBUG, "No se encontró el id de prioridad")
        }
    }
}
