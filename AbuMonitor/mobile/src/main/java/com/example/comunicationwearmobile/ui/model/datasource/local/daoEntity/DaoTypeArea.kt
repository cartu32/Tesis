package com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.comunicationwearmobile.ui.model.entities.EntityTypeArea

@Dao
interface DaoTypeArea {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTypeArea(contact: EntityTypeArea):Long

    @Query("SELECT * FROM Type_Area")
    fun getAllType(): LiveData<List<EntityTypeArea>>

    @Transaction
    @Query("SELECT * FROM Type_Area WHERE id_type_area = :idTypeArea")
    suspend fun getTypeAreaWithId(idTypeArea: Int): EntityTypeArea
}