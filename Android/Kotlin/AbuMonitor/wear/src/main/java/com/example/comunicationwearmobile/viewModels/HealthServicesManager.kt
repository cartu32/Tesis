package com.example.comunicationwearmobile.viewModels

import android.content.Context
import android.util.Log
import androidx.concurrent.futures.await
import androidx.health.services.client.HealthServices
import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.PassiveMonitoringClient
import androidx.health.services.client.data.HealthEvent
import androidx.health.services.client.data.PassiveListenerConfig
import androidx.health.services.client.getCapabilities
import com.example.comunicationwearmobile.models.FallDetectorDataStore
import com.example.comunicationwearmobile.common.showNotification
import com.example.comunicationwearmobile.models.FallEventData
import com.example.comunicationwearmobile.models.PassiveHealthEventService
import com.example.comunicationwearmobile.models.SingletonHolder
import com.example.comunicationwearmobile.ui.screen.main.TAG
import com.example.shared_library.SharedData
import com.example.shared_library.toByteArray
import kotlinx.coroutines.flow.first
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*


class HealthServicesManager private constructor(val context: Context) {

    private var healthServicesClient: HealthServicesClient
    private var passiveMonitoringClient: PassiveMonitoringClient
    private val healthEventTypes = setOf(HealthEvent.Type.FALL_DETECTED)
    private var registered: Boolean = false

    companion object : SingletonHolder<HealthServicesManager , Context>(::HealthServicesManager)

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

    suspend fun registerFallDetectorEventsData(){
        val stateDetector:Boolean= FallDetectorDataStore.getDetectorActivateState(context).first()

        if (!stateDetector) {
            Log.d(TAG,"El detector de caidas no estaba registrado")
            registerForHealthEventsData()
            FallDetectorDataStore.saveDetectorActivateState(context,true)
            Log.d(TAG,"Detecto de caidas registrado")
        }
        else{
            Log.d(TAG,"El detector de caidas ya estaba registrado")
        }

    }
    //este metodo inicia un servicio para detectar los eventos de la caida en segundo plano.
    suspend fun registerForHealthEventsData() {
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


    fun recordHealthEvent(healthEvent: HealthEvent) {
        var msgFallDetection: SharedData.MsgFallDetection? =null

        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.ENGLISH)
            .withZone(ZoneId.systemDefault())
        val eventData = FallEventData(healthEvent.type.name, formatter.format(healthEvent.eventTime))

        msgFallDetection =SharedData.MsgFallDetection(
            title = "AbuMonitor",
            message = "Abumonitor ha detectado una caida",
            fechaHora = eventData.eventTime
        )

        showNotification(context, msgFallDetection)
        Log.d(TAG, "Caida Detectada")
    }
}