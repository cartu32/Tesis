package com.example.abumonitor.data.datasource.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.abumonitor.data.model.EntityReminder

@Dao
interface DaoReminder {
    @Insert
    suspend fun insertReminder(reminder: EntityReminder)

    @Query("SELECT * FROM Reminder")
    suspend fun getAllReminder(): List<EntityReminder>

    @Transaction
    @Query("SELECT * FROM reminder WHERE id_reminder = :idReminder")
    suspend fun getReminderWithId(idReminder: Int): EntityReminder

    @Delete
    suspend fun deleteReminder(reminder: EntityReminder)

    @Update
    suspend fun updateReminder(reminder: EntityReminder)
}