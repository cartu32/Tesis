package com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.abumonitor.data.model.EntityFirstTimeState

@Dao
interface DaoFirstTimeState {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(firstTimeState: EntityFirstTimeState)

    @Query("SELECT * FROM first_time_state WHERE id = 1")
    suspend fun getFirstTimeState(): EntityFirstTimeState?
}
