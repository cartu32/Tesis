package com.example.abumonitor.data.datasource.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.abumonitor.data.model.EntityAreaGeofence

@Dao

interface DaoAreaGeofence {

    // Insertar una nueva área geofence
    @Insert
    suspend fun insertAreaGeofence(area: EntityAreaGeofence):Long

    // Obtener todas las áreas con sus relaciones (relación 1 a N)
    @Transaction
    @Query("SELECT * FROM Area_Geofence")
    fun getAllAreas(): LiveData<List<EntityAreaGeofence>>


    // Obtener una sola área con todas sus relaciones (relación 1 a N)
    @Transaction
    @Query("SELECT * FROM Area_Geofence WHERE id_area = :idArea")
    suspend fun getAreaWithId(idArea: Int): EntityAreaGeofence

    // Eliminar un área (y todas sus relaciones con colores, eventos, prioridades, recordatorios, etc.)
    @Transaction
    @Delete
    suspend fun deleteArea(areaGeofence: EntityAreaGeofence)


    @Transaction
    @Query("DELETE FROM Area_Geofence WHERE id_area = :idArea")
    suspend fun deleteAreaWithId(idArea: Long?):Int

    // Actualizar el área (solo actualiza la tabla principal)
    @Update
    suspend fun updateArea(areaGeofence: EntityAreaGeofence):Int
}
