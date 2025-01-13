package com.example.comunicationwearmobile.ui.model.maps

import com.google.android.gms.maps.model.Circle
import java.io.Serializable

class AreaGeofence : Serializable {
    @JvmField
    var idArea: String = ""
    var latitud: Double = 0.0
    var longitud: Double = 0.0
    @JvmField
    var radius: Float = 0f
    lateinit var circle: Circle
}
