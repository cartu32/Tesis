package com.example.comunicationwearmobile.ui.model.maps

import com.example.abumonitor.data.model.EntityAreaGeofence
import com.google.android.gms.maps.model.Circle
import java.io.Serializable

class AreaGeofence : Serializable {
    lateinit var entityArea: EntityAreaGeofence
    lateinit var circle: Circle
}
