package com.example.comunicationwearmobile.ui.model.datasource.local.daoPojo

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.comunicationwearmobile.ui.model.pojo.AreaGeofenceWithAppointment
import com.example.comunicationwearmobile.ui.model.pojo.JoinAreaGeofence

@Dao
interface DaoJoinAreaGeofence {
    @Transaction
    @Query("SELECT * FROM Area_Geofence WHERE id_area = :idArea")
    suspend fun getJoinAreaGeofence(idArea:Long): JoinAreaGeofence

    @Transaction
    @Query("""
        SELECT 
            ag.id_area,
            ag.latitude,
            ag.longitude,
            ag.meters,
            p.id_priority,
            ta.id_type_area,
            GROUP_CONCAT(ae.id_event) AS list_id_event
        FROM Area_Geofence ag
        INNER JOIN Scheduled_Assistance sa ON ag.id_area = sa.id_area
        INNER JOIN Area_Event ae ON ag.id_area = ae.id_area
        INNER JOIN Type_Area ta ON ag.id_type_area = ta.id_type_area
        INNER JOIN Priority p ON ag.id_priority = p.id_priority
        WHERE sa.date_appointment = :tomorrowDate
        GROUP BY ag.id_area,ag.latitude,ag.longitude,ag.meters,p.id_priority,ta.id_type_area
    """)
    suspend fun getAreasWithAppointmentsTomorrow(tomorrowDate: Long): List<AreaGeofenceWithAppointment>
}