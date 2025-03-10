package com.example.abumonitor.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.time.LocalTime

@Parcelize
@Entity(
    tableName = "Area_Geofence",
    foreignKeys = [ForeignKey(
        entity = EntityColor::class,
        parentColumns = ["id_color"],
        childColumns = ["id_color"]
    ), ForeignKey(
        entity = EntityEvent::class,
        parentColumns = ["id_event"],
        childColumns = ["id_event"]
    ), ForeignKey(
        entity = EntityPriority::class,
        parentColumns = ["id_priority"],
        childColumns = ["id_priority"]
    ), ForeignKey(
        entity = EntityReminder::class,
        parentColumns = ["id_reminder"],
        childColumns = ["id_reminder"],
        onDelete = ForeignKey.SET_NULL // Opcional: establece null si se elimina el padre
    ), ForeignKey(
        entity = EntityContact::class,
        parentColumns = ["id_contact"],
        childColumns = ["id_contact"],
        onDelete = ForeignKey.SET_NULL // Opcional: establece null si se elimina el padre
    ), ForeignKey(
        entity = EntityTypeArea::class,
        parentColumns = ["id_type_area"],
        childColumns = ["id_type_area"]
    )], indices = [
        Index(value = ["id_color"]) ,
        Index(value = ["id_contact"]),
        Index(value = ["id_event"]) ,
        Index(value = ["id_priority"]),
        Index(value = ["id_reminder"]) ,
        Index(value = ["id_type_area"]),
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

    var id_contact: Int? = null ,
    var id_color: Int =0 ,
    var id_event: Int =0 ,
    var id_priority: Int =0 ,
    var id_reminder: Int ?=null ,
    var id_type_area: Int =0 ,
) : Parcelable
