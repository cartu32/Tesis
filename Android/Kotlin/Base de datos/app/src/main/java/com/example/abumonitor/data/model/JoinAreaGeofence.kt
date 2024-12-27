package com.example.abumonitor.data.model

import java.sql.Time

data class JoinAreaGeofence (
    var latitude:Double=0.0,
    var longitude:Double=0.0,
    var meters:Int=0,
    var security_zone:Boolean=false,
    var dwell_time: Time,
    var name_color: String
)