package com.example.comunicationwearmobile.ui.model.repository

import android.content.Context
import com.example.abumonitor.constants.Definition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class RepositoryScheduleAlarmSPref (context: Context){
    private val TIME_BETWEEN_CHECKS = "TIME_BETWEEN_CHECKS"
    private val TIME_NEXT_ALARM = "TIME_NEXT_ALARM"
    private val TIME_REMEMBER_APPOINTMET = "TIME_REMEMBER_APPOINTMET"

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
    //Versión asincronica para leerlo mas tarde
    suspend fun getTimeBetweenChecks(): Long {
        return withContext(Dispatchers.IO) {
            fileMutex.withLock {

               prefs.getLong(TIME_BETWEEN_CHECKS, Definition.NO_STORED_VALUE)

            }
        }
    }


    suspend fun saveTimeRememberAppointment(hour:Long){
        withContext(Dispatchers.IO) {
            fileMutex.withLock {
                val editor = prefs.edit()
                editor.putLong(TIME_REMEMBER_APPOINTMET, hour)
                editor.apply()
            }
        }
    }
    //Versión asincronica para leerlo mas tarde
    suspend fun getTimeRememberAppointment(): Long {
        return withContext(Dispatchers.IO) {
            fileMutex.withLock {

                prefs.getLong(TIME_REMEMBER_APPOINTMET, Definition.NO_STORED_VALUE)

            }
        }
    }

    suspend fun saveTimeNextAlarm(hour:Long){
        withContext(Dispatchers.IO) {
            fileMutex.withLock {
                val editor = prefs.edit()
                editor.putLong(TIME_NEXT_ALARM, hour)
                editor.apply()
            }
        }
    }

    suspend fun getTimeNextAlarm(): Long {
        return withContext(Dispatchers.IO) {
            fileMutex.withLock {

                prefs.getLong(TIME_NEXT_ALARM, Definition.NO_STORED_VALUE)

            }
        }
    }

    //Versión síncrona para inicialización temprana
    fun getTimeBetweenChecksSync(): Long {
        return prefs.getLong(TIME_BETWEEN_CHECKS, Definition.NO_STORED_VALUE)
    }

    //Versión síncrona para inicialización temprana
    fun getTimeRememberAppointmentSync(): Long {
        return prefs.getLong(TIME_REMEMBER_APPOINTMET, Definition.NO_STORED_VALUE)
    }

    fun saveTimeBetweenChecksSync(hour:Long){
        val editor = prefs.edit()
        editor.putLong(TIME_BETWEEN_CHECKS, hour)
        editor.apply()
    }

    fun saveTimeNextAlarmSync(hour:Long){
        val editor = prefs.edit()
        editor.putLong(TIME_NEXT_ALARM, hour)
        editor.apply()
    }

    fun saveTimeRememberAppointmentSync(hour:Long){
        val editor = prefs.edit()
        editor.putLong(TIME_REMEMBER_APPOINTMET, hour)
        editor.apply()
    }




}