package com.example.abumonitor.data.datasource.local

import androidx.room.Dao
import androidx.room.Query
import com.example.abumonitor.data.model.JoinAreaGeofence

@Dao
interface DaoJoinAreaGeofence {
    @Query("""
    SELECT
            Area.longitude AS longitude,
            Area.latitude AS latitude,
            Area.meters AS meters,
            Area.security_zone AS security_zone,
            Area.dwell_time AS dwell_time,
            Color.description AS name_color
    FROM Area_Geofence AS Area
    INNER JOIN Color ON Area.id_color = Color.id_color
    """)
    fun getJoinAreaGeofence():List<JoinAreaGeofence>
}