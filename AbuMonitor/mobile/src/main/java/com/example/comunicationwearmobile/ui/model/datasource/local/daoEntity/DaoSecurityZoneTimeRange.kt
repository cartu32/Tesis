package com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity

import androidx.room.Dao
import androidx.room.Insert
import com.example.comunicationwearmobile.ui.model.entities.EntitySecurityZoneTimeRange

@Dao
interface DaoSecurityZoneTimeRange {
    @Insert
    fun insertSecurityZoneTimeRange(securityZoneTimeRange: EntitySecurityZoneTimeRange)

}