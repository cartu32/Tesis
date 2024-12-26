package com.example.abumonitor.data.datasource.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.abumonitor.data.model.EntityColor

@Dao
interface DaoColor {
    @Insert
    suspend fun insertColor(color: EntityColor)

    @Query("SELECT * FROM Color")
    suspend fun getAllColor(): List<EntityColor>

    @Transaction
    @Query("SELECT * FROM color WHERE id_color = :idColor")
    suspend fun getColorWithId(idColor: Int): EntityColor

    @Delete
    suspend fun deleteColor(color: EntityColor)

    @Update
    suspend fun updateColor(typeArea: EntityColor)
}