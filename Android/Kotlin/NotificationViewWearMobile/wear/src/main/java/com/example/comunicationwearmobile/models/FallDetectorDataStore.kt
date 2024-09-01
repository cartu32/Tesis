package com.example.comunicationwearmobile.models

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object FallDetectorDataStore {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "FallDetectorPreferences")
    private val DETECTOR_ACTIVATE_KEY = booleanPreferencesKey("detector_key")


    // Guardar datos en DataStore
    suspend fun saveDetectorActivateState(context: Context, state:Boolean) {
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(DETECTOR_ACTIVATE_KEY.toString())] = state
        }
    }

    // Leer datos desde DataStore
    fun getDetectorActivateState(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[DETECTOR_ACTIVATE_KEY] == true
        }
    }
}
