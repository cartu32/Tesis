package com.example.comunicationwearmobile.ui.model.pojo

import com.example.abumonitor.data.model.EntityEvent

//data class que se usa para dibujar las areas de geofence en el mapa
//esta se difernecia de AreaGeofenceBasic porque tiene una lista de eventos

data class AreaGeofenceForMap(
    val id_area: Long,
    val latitude: String,
    val longitude: String,
    val meters: Int,
    val id_type_area:Int,
    val type_area:String,
    val color: Int,
    val events: List<EntityEvent>
)
