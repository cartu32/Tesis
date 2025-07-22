package com.example.comunicationwearmobile.ui.model.datasource.local.daoPojo

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.comunicationwearmobile.ui.model.pojo.JoinAreaGeofence

@Dao
interface DaoJoinAreaGeofence {
    @Transaction
    @Query("SELECT * FROM Area_Geofence WHERE id_area = :idArea")
    suspend fun getJoinAreaGeofence(idArea:Long): JoinAreaGeofence
}