package com.example.abumonitor.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "Area_Geofence",
    foreignKeys = [ForeignKey(
        entity = EntityColor::class,
        parentColumns = ["id_color"],
        childColumns = ["id_color"]
    ), ForeignKey(
        entity = EntityPriority::class,
        parentColumns = ["id_priority"],
        childColumns = ["id_priority"]
    )], indices = [
        Index(value = ["id_color"]) ,
        Index(value = ["id_priority"]),
    ]
)
data class EntityAreaGeofence(
    @PrimaryKey(autoGenerate = true) var id_area: Long =0 ,
    var latitude:String= null.toString() ,
    var longitude:String= null.toString() ,
    var meters:Int=0 ,
    var security_zone:Boolean=false ,
    var dwell_time: Int=0 ,
    var description:String= null.toString() ,

    var id_color: Int =0 ,
    var id_priority: Int =0 ,
) : Parcelable
