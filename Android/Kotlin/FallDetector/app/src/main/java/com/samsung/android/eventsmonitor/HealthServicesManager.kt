/*
 * Copyright 2022 Samsung Electronics Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.samsung.android.eventsmonitor

import android.content.Context
import android.util.Log
import androidx.concurrent.futures.await
import androidx.health.services.client.HealthServices
import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.PassiveMonitoringClient
import androidx.health.services.client.data.HealthEvent
import androidx.health.services.client.data.PassiveListenerConfig
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class HealthServicesManager private constructor(val context: Context) {

    private var healthServicesClient: HealthServicesClient
    private var passiveMonitoringClient: PassiveMonitoringClient
    private val healthEventTypes = setOf(HealthEvent.Type.FALL_DETECTED)
    private var registered: Boolean = false
    private var healthEventsObserver: HealthEventsObserver? = null

    companion object : SingletonHolder<HealthServicesManager, Context>(::HealthServicesManager)

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
        return true
    }

    //este metodo inicia un servicio para detectar los eventos de la caida en segundo plano.
    suspend fun registerForHealthEventsData() {
        Log.i(TAG, "Registering listener")
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
        Log.i(TAG, "Unregistering listeners")
        passiveMonitoringClient.clearPassiveListenerServiceAsync().await()
        registered = false
    }

    fun isRegistered(): Boolean {
        return registered
    }

    fun setHealthEventsObserver(healthEventsObs: HealthEventsObserver) {
        healthEventsObserver = healthEventsObs
    }

    fun recordHealthEvent(healthEvent: HealthEvent) {
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.ENGLISH)
            .withZone(ZoneId.systemDefault())
        val eventData = EventData(healthEvent.type.name, formatter.format(healthEvent.eventTime))
        showNotification(context)
        healthEventsObserver!!.onEventDataChanged(eventData)

    }
}