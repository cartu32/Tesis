package com.example.comunicationwearmobile.ui.model.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.datasource.local.AbuMonitorDatabase
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.data.model.EntityScheduledAssistance
import com.example.comunicationwearmobile.ui.model.extra.InsertResultAssistance
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


    suspend fun insertScheduledAssistance(assistance: EntityScheduledAssistance, latitude: String, longitude: String, meters: Int): InsertResultAssistance? {
        return withContext(Dispatchers.IO) {
            try {
                val areaGeof = EntityAreaGeofence().apply {
                    description = "Area de Asistencia"
                    this.latitude = latitude
                    this.longitude = longitude
                    this.meters = meters
                    id_priority = Definition.PRIORITY_ID_LOW
                    id_type_area = Definition.TYPE_AREA_ID_ASSISTANCE
                }

                val idArea = daoAreaGeofence.insertAreaGeofence(areaGeof)
                if (idArea == -1L) return@withContext null

                assistance.id_area = idArea
                val idAssistance = daoAssistance.insertScheduledAssistance(assistance)

                if (idAssistance == -1L) {
                    daoAreaGeofence.deleteArea(areaGeof)
                    return@withContext null
                }
                //se retorna el data class con el id de la asistencia y el id de la area de geofence
                InsertResultAssistance(idArea, idAssistance)

            } catch (e: Exception) {
                e.printStackTrace()
                null
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

    suspend fun deleteScheduledAssistance(scheduledAssistance: EntityScheduledAssistance) {
        withContext(Dispatchers.IO){
            daoAssistance.deleteAssistance(scheduledAssistance)
        }
    }

    fun getEventsByDate(date: Long): LiveData<List<EntityScheduledAssistance>> {
        return daoAssistance.getEventsByDate(date)

    }

}