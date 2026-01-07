package com.example.abumonitor.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.entities.EntityTypeArea
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "Area_Geofence",
    foreignKeys = [ForeignKey(
        entity = EntityTypeArea::class,
        parentColumns = ["id_type_area"],
        childColumns = ["id_type_area"]
    ), ForeignKey(
        entity = EntityPriority::class,
        parentColumns = ["id_priority"],
        childColumns = ["id_priority"]
    )], indices = [
        Index(value = ["id_type_area"]) ,
        Index(value = ["id_priority"]),
    ]
)
data class EntityAreaGeofence(
    @PrimaryKey(autoGenerate = true) var id_area: Long =0 ,
    var latitude:String= null.toString() ,
    var longitude:String= null.toString() ,
    var meters:Int=0 ,
    var description:String= null.toString() ,

    var id_type_area: Int =Definition.TYPE_AREA_ID_NORMAL ,
    var id_priority: Int =Definition.PRIORITY_ID_LOW,
) : Parcelable
