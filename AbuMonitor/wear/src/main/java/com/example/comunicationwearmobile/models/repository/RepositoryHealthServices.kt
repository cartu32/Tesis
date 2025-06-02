package com.example.comunicationwearmobile.models.repository

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.concurrent.futures.await
import androidx.health.services.client.HealthServices
import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.PassiveMonitoringClient
import androidx.health.services.client.data.HealthEvent
import androidx.health.services.client.data.PassiveListenerConfig
import androidx.health.services.client.getCapabilities
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.comunicationwearmobile.utils.services.PassiveHealthEventService
import com.example.comunicationwearmobile.utils.services.SingletonHolder
import com.example.comunicationwearmobile.view.jetpackCompose.main.TAG
import com.example.shared_library.SharedData
import com.example.shared_library.toByteArray
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.first


class RepositoryHealthServices private constructor(appContext: Context) {

    val context: Context = appContext.applicationContext
    private var healthServicesClient: HealthServicesClient
    private var passiveMonitoringClient: PassiveMonitoringClient
    private val healthEventTypes = setOf(HealthEvent.Type.FALL_DETECTED)

    private var registered: Boolean = false

    companion object :
        SingletonHolder<RepositoryHealthServices, Context>(::RepositoryHealthServices)

    init {
        healthServicesClient = HealthServices.getClient(context)

        passiveMonitoringClient = healthServicesClient.passiveMonitoringClient
    }

    //esta metodo se fija en una corutina aparte si el smartwatch tiene el hardware necesario
    //para detectar una ciadas. Si el reloj no posee es hw retorna false. Pero si lo posee retorna true
    suspend fun hasHealthEventsCapability(): Boolean {
        val capabilities = passiveMonitoringClient.getCapabilitiesAsync().await()
        for (healthEventType in healthEventTypes) {
            if (healthEventType !in capabilities.supportedHealthEventTypes) {
                return false
            }
        }
        passiveMonitoringClient.getCapabilities()
        return true
    }

    suspend fun registerFallDetectorEventsData() {
        val stateDetector: Boolean =
            RepositoryFallDetectorDS.getDetectorActivateState(context).first()

        if (!stateDetector) {
            Log.d(TAG, "El detector de caidas no estaba registrado")
            registerForHealthEventsData()
            RepositoryFallDetectorDS.saveDetectorActivateState(context, true)
            Log.d(TAG, "Detecto de caidas registrado")
        } else {
            Log.d(TAG, "El detector de caidas ya estaba registrado")
        }

    }

    //este metodo inicia un servicio para detectar los eventos de la caida en segundo plano.
    private suspend fun registerForHealthEventsData() {
        Log.d(TAG, "Registering listener")
        val passiveListenerConfig = PassiveListenerConfig.builder()
            .setHealthEventTypes(healthEventTypes)
            .build()

        //el inicio del servicio lo hace dentro de  un corutina
        passiveMonitoringClient.setPassiveListenerServiceAsync(
            PassiveHealthEventService::class.java,
            passiveListenerConfig
        ).await()
        registered = true
    }

    suspend fun unregisterHealthEventsData() {
        Log.d(TAG, "Unregistering listeners")
        passiveMonitoringClient.clearPassiveListenerServiceAsync().await()
        registered = false
    }

    fun isRegistered(): Boolean {
        return registered
    }

/*
    fun showNoitifyPush(healthEvent: HealthEvent) {
        val msgFallDetection: SharedData.MsgFallDetection?

        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.ENGLISH)
            .withZone(ZoneId.systemDefault())
        val eventData =
            DataClass_FallEventData(healthEvent.type.name, formatter.format(healthEvent.eventTime))

        msgFallDetection = SharedData.MsgFallDetection(
            title = "AbuMonitor",
            message = "Abumonitor ha detectado una caida",
            fechaHora = eventData.eventTime
        )

        notificationMannager?.showNotification(context, msgFallDetection)
        Log.d(TAG, "Caida Detectada")

    }

    fun notifyFallDetectionInWatch(healthEvent: HealthEvent) {
        val intent = Intent(SharedData.Broadcast.alertFallDetect.name)
        var msgFallDetection=SharedData.MsgNotification(
            "¡Alerta Caida Detectada!",
            "¿Necesita ayuda?",
             SharedData.TypeNotification.FallDetection)

        intent.putExtra(SharedData.ParamIntent.MESSAGE_PATH.name, SharedData.PATH_ADD_NOTIFICATION_FALL)
        intent.putExtra(SharedData.ParamIntent.MESSAGE_BODY.name,toByteArray(msgFallDetection))

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        Log.d("ABUMONITOR", "Caida Detectada "+healthEvent.type.name)

    }
*/
    fun notifyFallDetectionInWatch() {
        //como el unico que puede abrir activity con startactivity estando la app en segundo plano es la clase wearablelistener.
        //lo que hago el autoenviar el mensaje de deteccion de caidas al mismo reloj enviandoselo a wearablelistener. Para
        //qu desde ahi pueda abrirse la activty que muestra el alerta de deteccion de caida.

    var msgFallDetection=SharedData.MsgNotification(
        "¡Alerta Caida Detectada!",
        "¿Necesita ayuda?",
        SharedData.TypeNotification.FallDetection)

    Wearable.getNodeClient(context).localNode
            .addOnSuccessListener { node ->
                val nodeId = node.id
                Wearable.getMessageClient(context)
                    .sendMessage(nodeId,SharedData.PATH_ADD_NOTIFICATION_FALL, toByteArray(msgFallDetection))
                    .addOnSuccessListener {
                        Log.d("WearOS", "Mensaje enviado con éxito")
                    }
                    .addOnFailureListener {
                        Log.e("WearOS", "Error al enviar mensaje", it)
                    }
            }
            .addOnFailureListener {
                Log.e("WearOS", "Error al obtener nodo local", it)
            }


    }



}

