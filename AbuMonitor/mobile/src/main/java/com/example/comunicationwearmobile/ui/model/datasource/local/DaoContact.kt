package com.example.abumonitor.data.datasource.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.abumonitor.data.model.EntityContact

@Dao
interface DaoContact {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EntityContact):Long

    @Query("SELECT * FROM Contact")
    fun getAllContactLiveData(): LiveData<List<EntityContact>>

    @Query("SELECT * FROM Contact")
    fun getAllContactList():List<EntityContact>
    @Delete
    suspend fun deleteContact(contact: EntityContact):Int

    @Update
    suspend fun updateContact(contact: EntityContact)
}