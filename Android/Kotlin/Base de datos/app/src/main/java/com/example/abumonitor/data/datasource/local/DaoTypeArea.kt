package com.example.abumonitor.data.datasource.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.abumonitor.data.model.EntityTypeArea

@Dao
interface DaoTypeArea {
    @Insert
    suspend fun insertTypeArea(typeArea: EntityTypeArea)

    @Query("SELECT * FROM Type_Area")
    suspend fun getAllTypeArea(): List<EntityTypeArea>

    @Transaction
    @Query("SELECT * FROM type_area WHERE id_type_area = :idTypeArea")
    suspend fun getTypeAreaWithId(idTypeArea: Int): EntityTypeArea

    @Delete
    suspend fun deleteTypeArea(typeArea: EntityTypeArea)

    @Update
    suspend fun updateTypeArea(typeArea: EntityTypeArea)
}