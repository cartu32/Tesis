package com.example.abumonitor.data.model

import java.sql.Time

data class JoinAreaGeofence (
    var id_area:Long=0,
    var latitude:Double=0.0,
    var longitude:Double=0.0,
    var description_area:String="",
    var meters:Int=0,
    var security_zone:Boolean=false,
    var dwell_time: Time,
    var id_priority:Long=0,
    var description_priority:String="",
    var id_event:Long=0,
    var description_event:String=""
)
