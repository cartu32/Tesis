package com.example.abumonitor.data.datasource.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.abumonitor.data.model.EntityScheduledAssistance

@Dao
interface DaoScheduledAssistance {
    @Insert
    suspend fun insertScheduledAssistance(reminder: EntityScheduledAssistance):Long

    @Query("SELECT * FROM Scheduled_Assistance")
    suspend fun getAllScheduleAssitance(): List<EntityScheduledAssistance>

    @Transaction
    @Query("SELECT * FROM Scheduled_Assistance WHERE id_assistance = :idAssistance")
    suspend fun getAssistanceWithId(idAssistance: Int): EntityScheduledAssistance

    @Delete
    suspend fun deleteAssistance(scheduledAssistance: EntityScheduledAssistance)

    @Update
    suspend fun updateAssistance(scheduledAssistance: EntityScheduledAssistance)
}