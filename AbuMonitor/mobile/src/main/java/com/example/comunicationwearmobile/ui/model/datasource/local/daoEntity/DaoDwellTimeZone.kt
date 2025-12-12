package com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.comunicationwearmobile.ui.model.entities.EntityDwellTimeZone

@Dao
interface DaoDwellTimeZone {
    @Insert
    fun insertDwellTimeZone(areaDwell:EntityDwellTimeZone):Long

    @Query("""
        SELECT dwell_time
        FROM dwelltimezone dw
        WHERE dw.id_area==:idArea
    """)
    fun getDwellTimeZoneWithId(idArea:Long):Int

}