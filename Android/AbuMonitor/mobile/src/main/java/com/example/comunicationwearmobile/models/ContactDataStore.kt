package com.example.comunicationwearmobile.models

import android.content.Context


import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object ContactDataStore {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "MobileDataStore")
    private val CONTACT_TEL_NUMBER_KEY = stringPreferencesKey("contactTelNUmber")

    // Guardar datos en DataStore
    suspend fun saveTelephoneNumber(context: Context, number: String) {
        context.dataStore.edit { preferences ->
            preferences[CONTACT_TEL_NUMBER_KEY] = number
        }
    }

    // Leer datos desde DataStore
    fun getTelephoneNumber(context: Context): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[CONTACT_TEL_NUMBER_KEY]
        }
    }
}
