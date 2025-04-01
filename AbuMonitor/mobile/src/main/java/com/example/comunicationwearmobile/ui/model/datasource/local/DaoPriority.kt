package com.example.abumonitor.data.datasource.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.abumonitor.data.model.EntityPriority

@Dao
interface DaoPriority {
    @Insert
    suspend fun insertPriority(priority: EntityPriority)

    @Query("SELECT * FROM Priority")
    suspend fun getAllPriority(): List<EntityPriority>

    @Transaction
    @Query("SELECT * FROM Priority WHERE id_priority = :idPriority")
    suspend fun getPriorityWithId(idPriority: Int): EntityPriority

    @Delete
    suspend fun deletePriority(priority: EntityPriority)

    @Update
    suspend fun updatePriority(priority: EntityPriority)
}