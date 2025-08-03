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

package com.example.comunicationwearmobile.utils.services

import android.util.Log
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.HealthEvent
import com.example.comunicationwearmobile.view.jetpackCompose.main.TAG
import com.example.comunicationwearmobile.models.repository.RepositoryHealthServices
import kotlinx.coroutines.runBlocking

//Este es el servicio que se lanza en el metodo registerForHealthEventsData de RepositoryHealthServices
class PassiveHealthEventService : PassiveListenerService() {

    override fun onHealthEventReceived(event: HealthEvent) {
        //ver si se puede eliminar runBlocking, porque parece que es innecesario, ya que no ejecuta
        // ninugna corutina
        runBlocking {
            try{
                super.onHealthEventReceived(event)
                Log.i("ABUMONITOR", "onHealthEventReceived received with type: ${event.type}")
                RepositoryHealthServices.getInstance(applicationContext).notifyFallDetectionInWatch()
            }catch (e : Exception){
                Log.i("ABUMONITOR", "Error al detectar caida${e.message}")
            }

        }
    }
}
