package com.example.abumonitor.data.datasource.local

import androidx.room.Dao
import androidx.room.Query
import com.example.abumonitor.data.model.JoinAreaGeofence

@Dao
interface DaoJoinAreaGeofence {
    @Query("""
    SELECT
            Area.id_area AS id_area,
            Area.longitude AS longitude,
            Area.latitude AS latitude,
            Area.description AS description_area,
            Area.meters AS meters,
            Area.security_zone AS security_zone,
            Area.dwell_time AS dwell_time,
            Area.id_priority AS id_priority,
            priority.description AS description_priority,
            Area.id_event AS id_event,
            event.description AS description_event
    FROM Area_Geofence AS Area
    INNER JOIN priority ON Area.id_priority = priority.id_priority
    INNER JOIN event ON Area.id_event = event.id_event
    WHERE Area.id_area = :idArea
 """)
    suspend fun getJoinAreaGeofence(idArea:Long):JoinAreaGeofence
}