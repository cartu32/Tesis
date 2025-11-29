package com.example.comunicationwearmobile.ui.model.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.datasource.local.AbuMonitorDatabase
import com.example.abumonitor.data.model.EntityScheduledAssistance
import com.example.comunicationwearmobile.ui.model.pojo.AreaGeofenceWithEvents
import com.example.comunicationwearmobile.ui.utils.Tools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RepositoryScheduleAssistance(context: Context) {

    private val database = AbuMonitorDatabase.getDatabase(context)
    private val daoAssistance = database.entityScheduledAssistanceDao()

    companion object {
        @Volatile
        private var INSTANCE: RepositoryScheduleAssistance? = null

        fun getInstance(context: Context): RepositoryScheduleAssistance {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RepositoryScheduleAssistance(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    suspend fun insertScheduledAssistance(assistance: EntityScheduledAssistance): Long {
        return withContext(Dispatchers.IO) {
            try {
                val idAssistance = daoAssistance.insertScheduledAssistance(assistance)

                if (idAssistance != -1L)
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
        return withContext(Dispatchers.IO) {
            daoAssistance.getAssistanceWithId(idAssistance)
        }
    }

    //Metodo que retorna las citas de la fecha pasada por parametro
    // desde el comienzo del dia (a las 0:00)
    // hasta el final de dia (inicio del proximo dia a las 0:00)
    fun getEventsByDate(date: Long): LiveData<List<EntityScheduledAssistance>> {
        val startDayDate = Tools.extractDayOfDateInMillis(date)
        val endDayDate = Tools.getStartNextDay(startDayDate)

        return daoAssistance.getAppointmetByDate(startDayDate, endDayDate)
    }


    suspend fun getAssistanceWithAreaId(idArea: Long): EntityScheduledAssistance {
        return withContext(Dispatchers.IO) {
            daoAssistance.getAppointmetByIdArea(idArea)
        }
    }

    suspend fun updateScheduleAssistance(assistance: EntityScheduledAssistance): Int {
        return withContext(Dispatchers.IO) {
            daoAssistance.updateScheduledAssistance(assistance)
        }
    }


    suspend fun getAreasInsideDateInterval(dateTimeAlarmInitial: Long, dateTimeAlarmNext: Long): List<AreaGeofenceWithEvents> {
        return withContext(Dispatchers.IO) {
            daoAssistance.getAreasInsideDateInterval(dateTimeAlarmInitial, dateTimeAlarmNext)
        }
    }

    suspend fun updateIsActivatedGeofence(idArea: Long, valueIsActivatedGeof: Boolean): Int {
        return withContext(Dispatchers.IO) {
            daoAssistance.updateIsActivatedGeofence(idArea, valueIsActivatedGeof)
        }
    }

    suspend fun getAreasWithAppointmentActivated(dateTimeAlarmInitial: Long, dateTimeAlarmNext: Long): List<EntityScheduledAssistance> {
        return withContext(Dispatchers.IO) {
            daoAssistance.getAreasWithAppointmentActivated(dateTimeAlarmInitial, dateTimeAlarmNext)
        }

    }

    suspend fun getAreasWithAppointmentRemember(timePreviousRemember: Long, dateTimeAlarmInitial: Long, dateTimeAlarmNext: Long): List<EntityScheduledAssistance> {
        return withContext(Dispatchers.IO){
            daoAssistance.getAreasWithAppointmentRemember(timePreviousRemember,dateTimeAlarmInitial, dateTimeAlarmNext)
        }

    }
}
