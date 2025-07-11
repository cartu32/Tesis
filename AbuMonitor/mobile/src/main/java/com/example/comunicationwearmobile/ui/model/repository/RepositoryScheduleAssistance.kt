package com.example.comunicationwearmobile.ui.model.repository

import android.content.Context
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.datasource.local.AbuMonitorDatabase
import com.example.abumonitor.data.model.EntityScheduledAssistance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RepositoryScheduleAssistance(context: Context, scope: CoroutineScope) {
    private val database = AbuMonitorDatabase.getDatabase(context, scope)
    private val daoAssistance = database.entityScheduledAssistanceDao()

    suspend fun insertScheduledAssistance(assistance: EntityScheduledAssistance): Long {
        return withContext(Dispatchers.IO) {
            try {
                daoAssistance.insertScheduledAssistance(assistance)
            } catch (e: Exception) {
                e.printStackTrace()
                Definition.ERROR_INSERT_CONTACT
            }
        }
    }

    suspend fun getAllScheduleAssitance(): List<EntityScheduledAssistance> {
        return withContext(Dispatchers.IO){
            daoAssistance.getAllScheduleAssitance()
        }
    }

    suspend fun getAssistanceWithId(idAssistance: Int): EntityScheduledAssistance {
        return withContext(Dispatchers.IO){
            daoAssistance.getAssistanceWithId(idAssistance)
        }
    }

    suspend fun deleteScheduledAssistance(scheduledAssistance: EntityScheduledAssistance) {
        withContext(Dispatchers.IO){
            daoAssistance.deleteAssistance(scheduledAssistance)
        }
    }

}