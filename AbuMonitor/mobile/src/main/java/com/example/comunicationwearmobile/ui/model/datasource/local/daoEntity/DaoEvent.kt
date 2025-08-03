package com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.abumonitor.data.model.EntityEvent

@Dao
interface DaoEvent {
    @Insert
    suspend fun insertEven(event: EntityEvent)

    @Query("SELECT * FROM Event")
    suspend fun getAllEvent(): List<EntityEvent>

    @Transaction
    @Query("SELECT * FROM event WHERE id_event = :idEvent")
    suspend fun getEventWithId(idEvent: Int): EntityEvent

    @Delete
    suspend fun deleteEvent(event: EntityEvent)

    @Update
    suspend fun updateEvent(event: EntityEvent)
}