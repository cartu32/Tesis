package com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.abumonitor.data.model.EntityScheduledAssistance
import com.example.comunicationwearmobile.ui.model.pojo.AreaGeofenceWithEvents

@Dao
interface DaoScheduledAssistance {
    @Insert
    suspend fun insertScheduledAssistance(assistance: EntityScheduledAssistance):Long

    @Query("SELECT * FROM Scheduled_Assistance")
    fun getAllScheduleAssitance(): LiveData<List<EntityScheduledAssistance>>

    @Transaction
    @Query("SELECT * FROM Scheduled_Assistance WHERE id_assistance = :idAssistance")
    fun getAssistanceWithId(idAssistance: Int): EntityScheduledAssistance

    @Delete
    fun deleteAssistance(scheduledAssistance: EntityScheduledAssistance)

    @Query("SELECT * FROM scheduled_assistance" +
           " WHERE date_hour_appointment >= :dayDateStart AND " +
           "date_hour_appointment <= :dayDateEnd")
    fun getAppointmetByDate(dayDateStart: Long, dayDateEnd: Long): LiveData<List<EntityScheduledAssistance>>

    @Query("""
            SELECT sch.*
            FROM scheduled_assistance sch
            INNER JOIN area_runtime_state ar ON sch.id_area = ar.id_area
            WHERE sch.id_area = :idArea
              AND ar.is_activated_geof = 1
            LIMIT 1 
        """)
    fun getAppointmentActivatedByIdArea(idArea: Long): EntityScheduledAssistance?

    @Update
    suspend fun updateScheduledAssistance(assistance: EntityScheduledAssistance):Int

    @Query("""
        SELECT 
           ag.id_area,
            ag.latitude,
            ag.longitude,
            ag.meters,
            ag.id_priority,
            ag.id_type_area,
            ag.description,
            ar.is_activated_geof,
            ar.prev_state_machine,
            ar.last_update_time,
            COALESCE(dt.dwell_time, 0) AS dwell_time,
            GROUP_CONCAT(ae.id_event) AS list_id_event
        FROM Area_Geofence ag
        INNER JOIN Scheduled_Assistance sa ON ag.id_area = sa.id_area
        INNER JOIN Area_Event ae ON ag.id_area = ae.id_area
        INNER JOIN Type_Area ta ON ag.id_type_area = ta.id_type_area
        INNER JOIN Priority p ON ag.id_priority = p.id_priority
        INNER JOIN Area_Runtime_State ar ON ag.id_area = ar.id_area
        LEFT JOIN DwellTimeZone dt ON ag.id_area = dt.id_area
        WHERE sa.date_hour_appointment >= :dateTimeAlarmInitial AND
              sa.date_hour_appointment< :dateTimeAlarmNext              
        GROUP BY ag.id_area,ag.latitude,ag.longitude,ag.meters,ag.id_priority,ag.id_type_area,ag.description,ar.is_activated_geof,ar.prev_state_machine,ar.last_update_time
    """)
    fun getAreasInsideDateInterval(
        dateTimeAlarmInitial: Long,
        dateTimeAlarmNext: Long
    ):List<AreaGeofenceWithEvents>



    @Query("""
        SELECT *
        FROM scheduled_assistance sa
        WHERE sa.date_hour_appointment - :timePreviousRemember >= :dateTimeAlarmInitial AND
              sa.date_hour_appointment - :timePreviousRemember < :dateTimeAlarmNext
    """)
    fun getAreasWithAppointmentRemember(timePreviousRemember: Long,dateTimeAlarmInitial: Long, dateTimeAlarmNext: Long): List<EntityScheduledAssistance>

    @Query("""
        SELECT MIN(sa2.date_hour_appointment)
        FROM   scheduled_assistance sa2
        WHERE  sa2.date_hour_appointment > :initIntervalAlarm
    """)
    fun getNextAppointmentTime(initIntervalAlarm: Long): Long?


    @Query("""
        UPDATE Area_Runtime_State
        SET is_activated_geof = :activated
        WHERE id_area IN (
              SELECT sa.id_area
              FROM scheduled_assistance sa
              WHERE sa.date_hour_appointment = :timeCurrentAlarm
            )
    """)
    suspend fun updateUpcomingAreasActivation(
        activated: Boolean,
        timeCurrentAlarm: Long
    ): Int

    @Query("""
        UPDATE Area_Runtime_State
        SET is_activated_geof = :activated
        WHERE id_area = :idArea
    """)
    suspend fun updateActivationAreaWithId(
        activated: Boolean,
        idArea: Long
    ): Int

}

