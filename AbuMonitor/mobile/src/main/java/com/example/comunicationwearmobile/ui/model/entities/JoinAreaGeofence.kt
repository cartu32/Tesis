package com.example.abumonitor.data.model

import android.os.Parcelable
import java.sql.Time

@kotlinx.parcelize.Parcelize
data class JoinAreaGeofence (
    var id_area:Long=0,
    var latitude:Double=0.0,
    var longitude:Double=0.0,
    var description_area:String="",
    var meters:Int=0,
    var security_zone:Boolean=false,
    var dwell_time: Int,
    var id_priority:Long=0,
    var description_priority:String="",
): Parcelable
