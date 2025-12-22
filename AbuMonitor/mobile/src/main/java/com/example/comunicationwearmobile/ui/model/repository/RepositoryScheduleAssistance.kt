package com.example.comunicationwearmobile.ui.model.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.withTransaction
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.datasource.local.AbuMonitorDatabase
import com.example.abumonitor.data.model.EntityScheduledAssistance
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity.DaoAreaRuntimeState
import com.example.comunicationwearmobile.ui.model.dto.AppointmentNextEnd
import com.example.comunicationwearmobile.ui.model.dto.AreaNextAppointmentRow
import com.example.comunicationwearmobile.ui.model.dto.DataAreaGeofAux
import com.example.comunicationwearmobile.ui.model.pojo.AreaGeofenceWithEvents
import com.example.comunicationwearmobile.ui.utils.Tools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RepositoryScheduleAssistance(context: Context) {

    private val database = AbuMonitorDatabase.getDatabase(context)
    private val daoAssistance = database.entityScheduledAssistanceDao()
    private val daoAreaRuntimeState = database.entityAreaRuntimeState()

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


    suspend fun activateNextAppointmentArea(timeCurrentAlarm: Long): Int {
        return withContext(Dispatchers.IO) {
            daoAssistance.activateNextAppointmentArea(timeCurrentAlarm)
        }
    }


    suspend fun desactivateNextAppointmentArea(timeCurrentAlarm: Long): Int {
        return withContext(Dispatchers.IO){
            daoAssistance.desactivateNextAppointmentArea(timeCurrentAlarm)
        }
    }

    suspend fun desactivateAreasWithoutAssisntance(listAppointWithoutAssisntace: List<EntityScheduledAssistance>):Int{
        return withContext(Dispatchers.IO){

            val listIdAreas= mutableListOf<Long>()

            for (area in listAppointWithoutAssisntace){
                listIdAreas.add(area.id_area)
            }
            daoAreaRuntimeState.updateAreaActivated(listIdAreas,false)
        }

    }

    suspend fun getReminderAppointmentOfCurrentAlarm(timeCurrentAlarm: Long,offsetReminder: Long): List<EntityScheduledAssistance> {
        return withContext(Dispatchers.IO){
            daoAssistance.getReminderAppointmentOfCurrentAlarm(timeCurrentAlarm,offsetReminder)
        }

    }

    //metodo que retorna la hora y fecha de inicio de la proxima cita de asistencia
    suspend fun getStartTimeOfNextAppointment(initIntervalAlarma:Long=0):Long? {
        return withContext(Dispatchers.IO){
            daoAssistance.getStartTimeOfNextAppointment(initIntervalAlarma)
        }
    }

    //metodo que retorna la hora y fecha mas chica de fin de la proxima cita de asistencia
    suspend fun getEndTimeOfNextAppointmentMin(initIntervalAlarma:Long=0):Long? {
        return withContext(Dispatchers.IO){
            daoAssistance.getEndTimeOfNextAppointmentMin(initIntervalAlarma)
        }
    }

    //metodo que retorna el listado de las areas con la hora y fecha de fin de la proxima cita de asistencia
    //teniendo en cuenta si la cita es nueva o no
    suspend fun getListEndTimeOfNextAppointment(initIntervalAlarma:Long=0):List<AppointmentNextEnd> {
        return withContext(Dispatchers.IO){
            daoAssistance.getListEndTimeOfNextAppointment(initIntervalAlarma)
        }
    }


    suspend fun getAndMarkExpiredInassistance(timeCurrentAlarm: Long): List<EntityScheduledAssistance> {
        return database.withTransaction {
            val list = daoAssistance.getExpiredUnassistedNotNotified(timeCurrentAlarm)
            if (list.isNotEmpty()) {
                val ids = list.map { it.id_area }
                daoAssistance.markInassistanceNotifiedByIds(ids)
            }
            list
        }
    }

    suspend fun updateNewAppointmentDate(listIdAreasNextEnd: List<Long>?) {
        return withContext(Dispatchers.IO){
            daoAssistance.updateNewAppointmentDate(listIdAreasNextEnd)
        }
    }

    suspend fun getTimeOfNextReminder(timeCurrentAlarm:Long, offsetReminder: Long): Long? {
        return withContext(Dispatchers.IO){
            daoAssistance.getTimeOfNextReminder(timeCurrentAlarm, offsetReminder)
        }
    }


}
