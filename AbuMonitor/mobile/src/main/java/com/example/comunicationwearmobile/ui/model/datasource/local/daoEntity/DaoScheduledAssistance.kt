package com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.abumonitor.data.model.EntityScheduledAssistance
import com.example.comunicationwearmobile.ui.model.dto.AppointmentNextEnd
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
        SELECT *
        FROM scheduled_assistance sa
        WHERE (sa.date_hour_appointment-:offsetReminder)=:timeCurrentAlarm
    """)
    fun getReminderAppointmentOfCurrentAlarm(timeCurrentAlarm: Long,offsetReminder: Long): List<EntityScheduledAssistance>

    @Query("""
        SELECT MIN(sa2.date_hour_appointment)
        FROM   scheduled_assistance sa2
        INNER JOIN Area_Runtime_State ar ON sa2.id_area = ar.id_area
        WHERE ar.is_activated_geof == false AND
              :timeAlarm < sa2.date_hour_appointment 
    """)
    fun getStartTimeOfNextAppointment(timeAlarm: Long): Long?

    @Query("""
        SELECT MIN(sa2.date_hour_appointment+sa2.time_duration_activation_appointment)
        FROM   scheduled_assistance sa2
        INNER JOIN Area_Runtime_State ar ON sa2.id_area = ar.id_area
        WHERE ar.is_activated_geof == true AND
             (sa2.date_hour_appointment+sa2.time_duration_activation_appointment) > :timeAlarm
    """)
    fun getEndTimeOfNextAppointmentMin(timeAlarm: Long): Long?


    @Query("""
        SELECT sa.id_area,
               sa.date_hour_appointment + sa.time_duration_activation_appointment AS endTime
        FROM scheduled_assistance sa
        INNER JOIN Area_Runtime_State ar ON sa.id_area = ar.id_area
        WHERE ar.is_activated_geof = 1 AND  
              sa.is_new_appointment_assistance = 1 AND  
              :timeAlarm < (sa.date_hour_appointment + sa.time_duration_activation_appointment) AND
              (sa.date_hour_appointment + sa.time_duration_activation_appointment) = (
                    SELECT MIN(sa2.date_hour_appointment + sa2.time_duration_activation_appointment)
                    FROM scheduled_assistance sa2
                    INNER JOIN Area_Runtime_State ar2 ON sa2.id_area = ar2.id_area
                    WHERE ar2.is_activated_geof = 1
                    AND   sa2.is_new_appointment_assistance = 1
                    AND   :timeAlarm < (sa2.date_hour_appointment + sa2.time_duration_activation_appointment))
    """)
    fun getListEndTimeOfNextAppointment(timeAlarm: Long): List<AppointmentNextEnd>

    @Query("""
        UPDATE Area_Runtime_State
        SET is_activated_geof = true
        WHERE id_area IN (
              SELECT sa.id_area
              FROM scheduled_assistance sa
              WHERE sa.date_hour_appointment = :timeCurrentAlarm
            )
    """)
    suspend fun activateNextAppointmentArea(timeCurrentAlarm: Long ): Int


    @Query("""
        UPDATE Area_Runtime_State
        SET is_activated_geof = false
        WHERE id_area IN (
              SELECT sa.id_area
              FROM scheduled_assistance sa
              WHERE sa.date_hour_appointment +sa.time_duration_activation_appointment= :timeCurrentAlarm
            )
    """)
    suspend fun desactivateNextAppointmentArea(timeCurrentAlarm: Long): Int


    @Query("""
        UPDATE Area_Runtime_State
        SET is_activated_geof = :activated
        WHERE id_area = :idArea
    """)
    suspend fun updateActivationAreaWithId(activated: Boolean, idArea: Long): Int

    @Query("""
        SELECT * 
        FROM scheduled_assistance sa
        WHERE sa.went_appointment=false AND
              sa.date_hour_appointment + sa.time_duration_activation_appointment ==:timeCurrentAlarm 
    """)
    fun getAreasWithAppointmentWithoutAssistance(timeCurrentAlarm: Long):List<EntityScheduledAssistance>


    @Query("""
        UPDATE scheduled_assistance
        SET was_notified_inassistance = true
        WHERE id_area IN (:listIdAreas)
        """)
    suspend fun markInassistanceNotifiedByIds(listIdAreas: List<Long>): Int

    @Query("""
        SELECT *
        FROM scheduled_assistance sa
        WHERE sa.went_appointment = false
              AND sa.was_notified_inassistance = false
              AND (sa.date_hour_appointment + sa.time_duration_activation_appointment) <= :timeCurrentAlarm
    """)
    suspend fun getExpiredUnassistedNotNotified(timeCurrentAlarm: Long): List<EntityScheduledAssistance>

    @Query("""
        UPDATE scheduled_assistance
        SET is_new_appointment_assistance=false
        WHERE id_area IN (:listIdAreasNextEnd)
    """)
    suspend fun updateNewAppointmentDate(listIdAreasNextEnd: List<Long>?):Int

    @Query("""
        SELECT MIN(sa2.date_hour_appointment-:offsetReminder)
        FROM   scheduled_assistance sa2
        INNER JOIN Area_Runtime_State ar ON sa2.id_area = ar.id_area
        WHERE ar.is_activated_geof == false AND
              :timeCurrentAlarm < (sa2.date_hour_appointment - :offsetReminder)
    """)
    fun getTimeOfNextReminder(timeCurrentAlarm:Long, offsetReminder: Long):Long?

}

