package com.example.comunicationwearmobile.ui.model.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class RepositoryScheduleAlarmSPref (context: Context){
    private val TIME_BETWEEN_CHECKS = "TIME_BETWEEN_CHECKS"

    private val prefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)

    private var fileMutex= Mutex()

    companion object {
        private const val PREF_FILE_NAME = "SPREF_SCHEDULE_ALARM"
        @Volatile
        private var INSTANCE: RepositoryScheduleAlarmSPref? = null

        fun getInstance(context: Context): RepositoryScheduleAlarmSPref {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RepositoryScheduleAlarmSPref(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    suspend fun saveTimeBetweenChecks(hour:Long){
        withContext(Dispatchers.IO) {
            fileMutex.withLock {
                val editor = prefs.edit()
                editor.putLong(TIME_BETWEEN_CHECKS, hour)
                editor.apply()
            }
        }
    }

    suspend fun getTimeBetweenChecks(): Long {
        return withContext(Dispatchers.IO) {
            fileMutex.withLock {

               prefs.getLong(TIME_BETWEEN_CHECKS, -1L)

            }
        }
    }
}