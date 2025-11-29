package com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity

import androidx.room.Insert
import androidx.room.Query
import com.example.abumonitor.data.model.EntityEvent
import com.example.comunicationwearmobile.ui.model.entities.EntityAreaEventCrossRef

interface DaoAreaEventCrossRef {
    //insertar en la tabla intermedia de la relacion  area-evento (relacion N a N)
    @Insert
    suspend fun insertAreaEventCrossRef(areaEventCrossRef: EntityAreaEventCrossRef):Long

    @Query("""
        SELECT *
        FROM Event AS e
        INNER JOIN Area_Event AS ae ON ae.id_event = e.id_event
        WHERE ae.id_area = :areaId
    """)
    suspend fun getEventByArea(areaId: Long): List<EntityEvent>
}