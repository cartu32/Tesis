package com.example.abumonitor.data.datasource.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.abumonitor.data.model.JoinAreaGeofence

@Dao
interface DaoJoinAreaGeofence {
    @Transaction
    @Query("SELECT * FROM Area_Geofence WHERE id_area = :idArea")
    suspend fun getJoinAreaGeofence(idArea:Long):JoinAreaGeofence
}