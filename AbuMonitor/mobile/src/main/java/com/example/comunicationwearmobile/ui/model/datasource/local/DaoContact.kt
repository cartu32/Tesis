package com.example.abumonitor.data.datasource.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.abumonitor.data.model.EntityContact

@Dao
interface DaoContact {
    @Insert
    suspend fun insertContact(contact: EntityContact)

    @Query("SELECT * FROM Contact")
    suspend fun getAllContact(): List<EntityContact>

    @Transaction
    @Query("SELECT * FROM contact WHERE id_contact = :idContact")
    suspend fun getColorWithId(idContact: Int): EntityContact

    @Delete
    suspend fun deleteContact(contact: EntityContact)

    @Update
    suspend fun updateContact(contact: EntityContact)
}