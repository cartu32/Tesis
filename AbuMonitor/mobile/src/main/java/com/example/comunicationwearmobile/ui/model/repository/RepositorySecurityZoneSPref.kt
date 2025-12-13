package com.example.comunicationwearmobile.ui.model.repository

import android.content.Context
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RepositorySecurityZoneSPref (context: Context){
    private val ENTERED_HOUR = "ENTERED_HOUR"

    private val prefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
    private val mutex= Mutex()
    companion object {
        private const val PREF_FILE_NAME = "SPREF_SECURITY_ZONE"
        @Volatile
        private var INSTANCE: RepositorySecurityZoneSPref? = null

        fun getInstance(context: Context): RepositorySecurityZoneSPref {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RepositorySecurityZoneSPref(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    suspend fun saveEnteredHour(hour:Long){
        mutex.withLock {
            val editor = prefs.edit()
            editor.putLong(ENTERED_HOUR, hour)
            editor.apply()
        }
    }

    suspend fun getEnteredHour(): Long {
        mutex.withLock {
            return prefs.getLong(ENTERED_HOUR, -1L)
        }
    }

    suspend fun clearSharedPreferences() {
        mutex.withLock {
            val editor = prefs.edit()
            editor.clear()
            editor.apply()
        }
    }
}