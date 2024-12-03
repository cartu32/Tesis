package com.example.comunicationwearmobile.models.maps

import java.io.Serializable
import java.sql.Time

class Route : Serializable {
    var idArea: String? = null
    var latitudeOrigin: Double? = null
    var longitudeOrigin: Double? = null
    var latitudeDestination: Double? = null
    var longitudeDestination: Double? = null
    var radius: Float = 0f
    var schedule: Time? = null
    var tolerance: Int = 0
}
