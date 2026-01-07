package com.example.comunicationwearmobile.ui.model.repository

import android.content.Context

class RepositoryDwellTimeZoneSPref(context: Context) {

    private val ENTERED_HOUR = "ENTERED_HOUR"

    private val prefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_FILE_NAME = "SPREF_DWELL_TIME_ZONE"
        @Volatile
        private var INSTANCE: RepositoryDwellTimeZoneSPref? = null

        fun getInstance(context: Context): RepositoryDwellTimeZoneSPref {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RepositoryDwellTimeZoneSPref(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    fun saveEnteredHour(hour:Long){
        val editor = prefs.edit()
        editor.putLong(ENTERED_HOUR, hour)
        editor.apply()
    }

    fun getEnteredHour(): Long {
        return prefs.getLong(ENTERED_HOUR, -1L)
    }

    fun clearSharedPreferences() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}