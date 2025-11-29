package com.example.comunicationwearmobile.ui.model.pojo

//data class que se usa para activar o desactivar las areas de geofence
//correspondientes a una cita de asistencia en la fecha indicada
data class AreaGeofenceWithEvents(
    val id_area: Long,
    val latitude: String,
    val longitude: String,
    val meters: Int,
    val id_type_area: Int,
    val id_priority: Int,
    val list_id_event: String,
    val dwell_time: Int=0,
    val description:String=""
)
