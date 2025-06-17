package com.example.comunicationwearmobile.models.repository

import android.content.Context
import com.example.shared_library.SharedData
import com.google.common.reflect.TypeToken
import com.google.gson.Gson

class RepositorySPStateListNotif(context: Context) {
    private val NAME_SHARED_PREFERENCES = "alerts_prefs"
    private val nameKey ="alerts_list"
    private val prefs = context.getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveAlertsToPrefs(alerts: List<SharedData.MsgNotification>) {
        val json = gson.toJson(alerts)
        prefs.edit().putString(nameKey, json).apply()
    }

    fun loadAlertsFromPrefs(): List<SharedData.MsgNotification> {
        val json = prefs.getString(nameKey, null)
        if (json != null) {
            val type = object : TypeToken<List<SharedData.MsgNotification>>() {}.type
            return gson.fromJson(json, type)
        }
        return emptyList()
    }
}