package com.example.comunicationwearmobile.ui.model.pojo

//data class que se usa para dibujar las areas de geofence en el mapa
data class AreaGeofenceBasic(
    val id_area: Long,
    val latitude: String,
    val longitude: String,
    val meters: Int,
    val id_type_area:Int,
    val type_area: String,
    val color: Int
)
