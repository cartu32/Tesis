/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.passivedatacompose.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "passive_data")

class PassiveDataRepository(private val context: Context) {
    //esto seria como el get del sharedpreferences y que retorna el valr de passiverdata_enalbed, que esta en el sharedpref
    //alamcenado. Pero aca no hace un return, lo que hace es asociarlo a un flow, que es como un observer que cuando se modifica
    // prefs[PASSIVE_DATA_ENABLED] en el pref
    val passiveDataEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PASSIVE_DATA_ENABLED] ?: false
    }

    //Esta metodo set PASSIVE_DATA_ENABLED en el sharedpref
    suspend fun setPassiveDataEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PASSIVE_DATA_ENABLED] = enabled
        }
    }

    //esto metdodo seria  como el get del sharedpreferences y que retorna el valr de LATEST_HEART_RATE, que esta en el sharedpref
    //alamcenado. Pero aca no hace un return, lo que hace es asociarlo a un flow, que es como un observer que cuando se modifica
    // prefs[LATEST_HEART_RATE] en el pref
    val latestHeartRate: Flow<Double> = context.dataStore.data.map { prefs ->
        prefs[LATEST_HEART_RATE] ?: 0.0
    }

    suspend fun storeLatestHeartRate(heartRate: Double) {
        context.dataStore.edit { prefs ->
            prefs[LATEST_HEART_RATE] = heartRate
        }
    }

    companion object {
        private val PASSIVE_DATA_ENABLED = booleanPreferencesKey("passive_data_enabled")
        private val LATEST_HEART_RATE = doublePreferencesKey("latest_heart_rate")
    }
}
