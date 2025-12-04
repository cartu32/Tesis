package com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.data.model.EntityEvent
import com.example.comunicationwearmobile.ui.model.entities.EntityAreaEventCrossRef
import com.example.comunicationwearmobile.ui.model.pojo.AreaGeofenceBasic
import com.example.comunicationwearmobile.ui.model.pojo.AreaGeofenceWithEvents
import com.example.comunicationwearmobile.ui.model.pojo.JoinAreaGeofence

@Dao

interface DaoAreaGeofence {

    // Insertar una nueva área geofence
    @Insert
    suspend fun insertAreaGeofence(area: EntityAreaGeofence):Long

    //insertar en la tabla intermedia de la relacion  area-evento (relacion N a N)
    @Insert
    suspend fun insertAreaEventCrossRef(areaEventCrossRef: EntityAreaEventCrossRef):Long

    // Obtener todas las áreas con sus relaciones (relación 1 a N)
    @Transaction
    @Query("SELECT * FROM Area_Geofence")
    fun getAllAreas(): List<EntityAreaGeofence>


    // Obtener una sola área con todas sus relaciones (relación 1 a N)
    @Transaction
    @Query("SELECT * FROM Area_Geofence WHERE id_area = :idArea")
    suspend fun getAreaWithId(idArea: Long): EntityAreaGeofence

    // Eliminar un área (y todas sus relaciones con colores, eventos, prioridades, recordatorios, etc.)
    @Transaction
    @Delete
    suspend fun deleteArea(areaGeofence: EntityAreaGeofence)


    @Transaction
    @Query("DELETE FROM Area_Geofence WHERE id_area = :idArea")
    suspend fun deleteAreaWithId(idArea: Long?):Int

    // Actualizar el área (solo actualiza la tabla principal)
    @Transaction
    @Update
    suspend fun updateArea(areaGeofence: EntityAreaGeofence):Int

    @Query("""
        SELECT ag.id_area, ag.latitude, ag.longitude, ag.meters, ta.id_type_area,ta.description AS type_area, ta.color
        FROM Area_Geofence AS ag
        INNER JOIN Type_Area AS ta ON ag.id_type_area = ta.id_type_area
    """)
    suspend fun getBasicAreas(): List<AreaGeofenceBasic>

    @Query("""
        SELECT *
        FROM Event AS e
        INNER JOIN Area_Event AS ae ON ae.id_event = e.id_event
        WHERE ae.id_area = :areaId
    """)
    suspend fun getEventByArea(areaId: Long): List<EntityEvent>

    @Transaction
    @Query("SELECT * FROM Area_Geofence WHERE id_area = :idArea")
    suspend fun getJoinAreaGeofence(idArea:Long): JoinAreaGeofence

    @Query("""
        SELECT 
            ag.id_area,
            ag.latitude,
            ag.longitude,
            ag.meters,
            ag.id_priority,
            ag.id_type_area,
            ag.description,
            ag.dwell_time,
            GROUP_CONCAT(ae.id_event) AS list_id_event
        FROM Area_Geofence ag
        INNER JOIN Area_Event ae ON ag.id_area = ae.id_area
        GROUP BY ag.id_area,ag.latitude,ag.longitude,ag.meters,ag.id_priority,ag.id_type_area,ag.dwell_time,ag.description
    """)
    fun getAreasActivated():List<AreaGeofenceWithEvents>
}
