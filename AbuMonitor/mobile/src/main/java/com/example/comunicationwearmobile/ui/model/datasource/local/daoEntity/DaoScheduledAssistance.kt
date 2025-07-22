package com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
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
    fun getEventsByDate(date: Long): LiveData<List<EntityScheduledAssistance>>
}