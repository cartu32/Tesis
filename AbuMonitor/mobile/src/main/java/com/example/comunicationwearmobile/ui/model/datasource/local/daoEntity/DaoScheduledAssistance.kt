package com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.data.model.EntityScheduledAssistance

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

    @Query("SELECT * FROM scheduled_assistance WHERE date_appointment = :date")
    fun getAppointmetByDate(date: Long): LiveData<List<EntityScheduledAssistance>>

    @Query("SELECT * FROM scheduled_assistance WHERE id_area = :idArea")
    fun getAppointmetByIdArea(idArea: Long): EntityScheduledAssistance

    @Update
    suspend fun updateScheduledAssistance(assistance: EntityScheduledAssistance):Int
}