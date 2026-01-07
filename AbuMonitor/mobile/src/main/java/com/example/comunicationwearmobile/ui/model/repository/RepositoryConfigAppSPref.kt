package com.example.comunicationwearmobile.ui.model.repository

import android.content.Context
import com.example.abumonitor.constants.Definition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class RepositoryConfigAppSPref (context: Context){
    private val TIME_REMEMBER_APPOINTMET = "TIME_REMEMBER_APPOINTMET"
    private val NAME_USER = "NAME_USER"

    private val prefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)

    private var fileMutex= Mutex()

    companion object {
        private const val PREF_FILE_NAME = "SPREF_CONFIG_APP"
        @Volatile
        private var INSTANCE: RepositoryConfigAppSPref? = null

        fun getInstance(context: Context): RepositoryConfigAppSPref {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RepositoryConfigAppSPref(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    suspend fun saveNameUser(nameUser: String){
        withContext(Dispatchers.IO) {
            fileMutex.withLock {
                val editor = prefs.edit()
                editor.putString(NAME_USER,nameUser)
                editor.apply()
            }
        }
    }

    suspend fun getNameUser(): String {
        return withContext(Dispatchers.IO) {
            fileMutex.withLock {
                prefs.getString(NAME_USER, Definition.DEFAULT_NAME_USER)?:Definition.DEFAULT_NAME_USER
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





    fun getTimeRememberAppointmentSync(): Long {
        return prefs.getLong(TIME_REMEMBER_APPOINTMET, Definition.NO_STORED_VALUE)
    }

    fun getNameUserSync(): Long {
        return prefs.getLong(NAME_USER, Definition.NO_STORED_VALUE)
    }

    fun saveNameUserSync(nameUser:String){
        val editor = prefs.edit()
        editor.putString(NAME_USER, nameUser)
        editor.apply()
    }

    fun saveTimeRememberAppointmentSync(hour:Long){
        val editor = prefs.edit()
        editor.putLong(TIME_REMEMBER_APPOINTMET, hour)
        editor.apply()
    }




}
