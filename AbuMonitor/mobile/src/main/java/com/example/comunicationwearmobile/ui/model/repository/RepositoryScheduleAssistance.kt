package com.example.comunicationwearmobile.ui.model.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.withTransaction
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.datasource.local.AbuMonitorDatabase
import com.example.abumonitor.data.model.EntityScheduledAssistance
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.model.dto.AreaNextAppointmentRow
import com.example.comunicationwearmobile.ui.model.dto.DataAreaGeofAux
import com.example.comunicationwearmobile.ui.model.pojo.AreaGeofenceWithEvents
import com.example.comunicationwearmobile.ui.utils.Tools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RepositoryScheduleAssistance(context: Context) {

    private val database = AbuMonitorDatabase.getDatabase(context)
    private val daoAssistance = database.entityScheduledAssistanceDao()
    private val repositoryAreaDB=RepositoryAreaDB.getInstance(context)

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

    suspend fun insertScheduledAssistance(assistance: EntityScheduledAssistance, newAreaGeof: DataAreaGeofAux):Long{
        return withContext(Dispatchers.IO){
            try{
                database.withTransaction {

                    val idArea=repositoryAreaDB.insertArea(newAreaGeof,false)

                    if (idArea==Definition.ERROR_INSERT_BD_GEOF)
                        throw IllegalStateException("Error insertando área")

                    assistance.id_area = idArea
                    val idAssistance = daoAssistance.insertScheduledAssistance(assistance)
                    if (idAssistance == -1L) {
                        throw IllegalStateException("Error insertando cita de asistencia")
                    }
                    idArea
                }
            }catch (e:Exception){
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


    suspend fun getAssistanceWithAreaId(idArea: Long): EntityScheduledAssistance? {
        return withContext(Dispatchers.IO) {
            daoAssistance.getAppointmentActivatedByIdArea(idArea)
        }
    }

    suspend fun updateScheduleAssistance(assistance: EntityScheduledAssistance): Int {
       return daoAssistance.updateScheduledAssistance(assistance)
    }

    suspend fun updateScheduledAssitanceAndDesactivateArea(assistance: EntityScheduledAssistance): Int {
        return database.withTransaction {
            updateScheduleAssistance(assistance)

            daoAssistance.updateActivationAreaWithId(false, assistance.id_area)

        }
    }


    suspend fun updateUpcomingAreasActivation(valueIsActivatedGeof: Boolean,timeCurrentAlarm: Long): Int {
        return withContext(Dispatchers.IO) {
            daoAssistance.updateUpcomingAreasActivation(valueIsActivatedGeof,timeCurrentAlarm)
        }
    }


    suspend fun getAreasWithAppointmentRemember(timePreviousRemember: Long, dateTimeAlarmInitial: Long, dateTimeAlarmNext: Long): List<EntityScheduledAssistance> {
        return withContext(Dispatchers.IO){
            daoAssistance.getAreasWithAppointmentRemember(timePreviousRemember,dateTimeAlarmInitial, dateTimeAlarmNext)
        }

    }

    //metodo que retorna la hora y fecha de la proxima cita de asistencia
    suspend fun getNextAppointmentTime(initIntervalAlarma:Long=0):Long? {
        return withContext(Dispatchers.IO){
            daoAssistance.getNextAppointmentTime(initIntervalAlarma)
        }
    }

}
