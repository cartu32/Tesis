package com.example.comunicationwearmobile.ui.model.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/*
Clase que contiene el shared preferences para guardar el id de la notificación
 */
class RepositoryIDNotificationSPref private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_FILE_NAME , Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREF_FILE_NAME = "PREF_FILE_NAME"
        @Volatile
        private var INSTANCE: RepositoryIDNotificationSPref? = null

        fun getInstance(context: Context): RepositoryIDNotificationSPref {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RepositoryIDNotificationSPref(context.applicationContext).also { INSTANCE = it }
            }
        }
    }


    fun saveArrayList(list: ArrayList<Int>, key: String) {
        val editor = prefs.edit()
        val json = gson.toJson(list)
        editor.putString(key, json)
        editor.apply()
    }

    fun getArrayList(key: String): ArrayList<Int> {
        val json = prefs.getString(key, null)
        val type = object : TypeToken<ArrayList<Int>>() {}.type
        return if (json != null) {
            gson.fromJson(json, type)
        } else {
            ArrayList()
        }
    }

    fun clearSharedPreferences() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}