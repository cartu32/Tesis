package com.example.comunicationwearmobile.ui.model.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.datasource.local.AbuMonitorDatabase
import com.example.abumonitor.data.model.EntityScheduledAssistance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RepositoryScheduleAssistance(context: Context, scope: CoroutineScope) {
    private val database = AbuMonitorDatabase.getDatabase(context, scope)
    private val daoAssistance = database.entityScheduledAssistanceDao()
    private val daoAreaGeofence = database.entityAreaGeofenceDao()

    companion object {
        @Volatile private var INSTANCE: RepositoryScheduleAssistance? = null

        fun getInstance(context: Context,scope: CoroutineScope): RepositoryScheduleAssistance {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RepositoryScheduleAssistance(context,scope).also { INSTANCE = it }
            }
        }
    }

    suspend fun insertScheduledAssistance(assistance: EntityScheduledAssistance): Long {
        return withContext(Dispatchers.IO) {
            try {
                val idAssistance = daoAssistance.insertScheduledAssistance(assistance)

                if(idAssistance!=-1L)
                    idAssistance
                else
                    Definition.ERROR_INSERT_BD_GEOF
            } catch (e: Exception) {
                e.printStackTrace()
                Definition.ERROR_INSERT_BD_GEOF
            }
        }
    }


    fun getAllScheduleAssitance(): LiveData<List<EntityScheduledAssistance>> {
        return daoAssistance.getAllScheduleAssitance()
    }

    suspend fun getAssistanceWithId(idAssistance: Int): EntityScheduledAssistance {
        return withContext(Dispatchers.IO){
            daoAssistance.getAssistanceWithId(idAssistance)
        }
    }

    fun getEventsByDate(date: Long): LiveData<List<EntityScheduledAssistance>> {
        return daoAssistance.getEventsByDate(date)

    }

}