package com.example.comunicationwearmobile.ui.utils.Helpers.Notification

import android.content.Context
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.repository.RepositoryConfigAppSPref
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDispatcherWearable
import com.example.comunicationwearmobile.ui.utils.Tools
import com.example.shared_library.SharedData
import com.google.android.gms.location.Geofence
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.LocalTime

object NotificationManager {

    // Mejor nombre + inmutable
    private val smsMutex = Mutex()

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    //Guard para evitar crash si se olvidan de init()
    private fun isReady(): Boolean {
        if (!::appContext.isInitialized) {
            Log.e(Definition.TAG_DEBUG, "NotificationManager no inicializado. Llamá init(context) antes de usarlo.")
            return false
        }
        return true
    }

    fun createMsgSecurityZone(msg: String): SharedData.MsgNotification {
        return SharedData.MsgNotification().apply {
            typeNotification = SharedData.TypeNotification.Alert
            title = "¡Alerta de Seguridad!"
            message = msg
            hour = Tools.getHour(LocalTime.now())
            date = Tools.getDate(LocalDate.now())
        }
    }

    fun createMsg(transition: Int?, description: String?, dwellTime: Long): SharedData.MsgNotification {
        return SharedData.MsgNotification().apply {
            hour = Tools.getHour(LocalTime.now())
            date = Tools.getDate(LocalDate.now())
            typeNotification = SharedData.TypeNotification.Alert
            title = "¡Alerta de Geofence!"
            message = when (transition) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> "ha entrado en la zona $description "
                Geofence.GEOFENCE_TRANSITION_EXIT -> "ha salido de la zona $description "
                Geofence.GEOFENCE_TRANSITION_DWELL -> {
                    val dwellMin = (dwellTime / 60_000L).coerceAtLeast(1)
                    "estuvo mas de $dwellMin min. en la zona $description "
                }
                else -> "Evento desconocido en zona $description"
            }
        }
    }

    // PRIORIDAD BAJA: solo SMS al familiar
    suspend fun notifyUserPriorityBaja(
        originalMsg: SharedData.MsgNotification,
        geofLatitude: String,
        geofLongitude: String
    ) {
        if (!isReady()) return

        val msgForCustomUser = originalMsg.copy(
            message = getMessageForCustomName(originalMsg.message)
        )

        // Serializa envíos de SMS (recurso externo)
        smsMutex.withLock {
            SmsHelper.sendSMSNotifyGeofence(
                appContext,
                msgForCustomUser,
                geofLatitude,
                geofLongitude
            )
        }
    }

    // PRIORIDAD MEDIA: notificación al abuelo + SMS al familiar
    private suspend fun notifyUserPriorityMedia(
        originalMsg: SharedData.MsgNotification,
        latitude: String,
        longitude: String
    ): Int? {
        if (!isReady()) return null
        val notificationHelper = NotificationHelper.getInstance(appContext) ?: return null

        val msgForElderly = originalMsg.copy(
            message = getMessageForElderly(originalMsg.message)
        )

        val id = notificationHelper.showNotificationGeneral(msgForElderly)

        // Además, aviso al familiar por SMS
        notifyUserPriorityBaja(originalMsg, latitude, longitude)

        return id
    }

    // PRIORIDAD ALTA: media + envío al reloj
    private suspend fun notifyUserPriorityAlta(
        originalMsg: SharedData.MsgNotification,
        latitude: String,
        longitude: String
    ) {
        if (!isReady()) return
        val notificationHelper = NotificationHelper.getInstance(appContext) ?: return

        val msgForElderly = originalMsg.copy(
            message = getMessageForElderly(originalMsg.message)
        )

        val id = notificationHelper.showNotificationGeneral(msgForElderly)

        notifyUserPriorityBaja(originalMsg, latitude, longitude)

        val msgForWear = msgForElderly.copy(
            idMsgMobile = id
        )

        RepositoryDispatcherWearable.sendDataToWearable(
            appContext,
            SharedData.PATH_ADD_NOTIFICATION_GENERAL,
            msgForWear
        )
    }

    suspend fun determineRecipientByPriority(
        idPriority: Int?,
        msg: SharedData.MsgNotification,
        latitude: String,
        longitude: String
    ) {
        if (!isReady()) return

        when (idPriority) {
            Definition.PRIORITY_ID_LOW -> notifyUserPriorityBaja(msg, latitude, longitude)
            Definition.PRIORITY_ID_MEDIUM -> notifyUserPriorityMedia(msg, latitude, longitude)
            Definition.PRIORITY_ID_HIGH -> notifyUserPriorityAlta(msg, latitude, longitude)
            else -> Log.e(Definition.TAG_DEBUG, "No se encontró el id de prioridad")
        }
    }

    // Mensaje para familiar (con nombre)
    suspend fun getMessageForCustomName(message: String): String {
        if (!isReady()) return message
        val nameUser = RepositoryConfigAppSPref.getInstance(appContext).getNameUser()
        return "$nameUser $message"
    }

    // Mensaje para abuelo
    fun getMessageForElderly(message: String): String = "Usted $message"
}
