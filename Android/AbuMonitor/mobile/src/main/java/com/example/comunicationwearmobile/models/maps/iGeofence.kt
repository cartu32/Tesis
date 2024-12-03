package com.example.comunicationwearmobile.models.maps

import java.io.Serializable

class iGeofence : Serializable {
    @JvmField
    var idArea: String? = null
    var latitud: Double? = null
    var longitud: Double? = null
    @JvmField
    var radius: Float = 0f
}
