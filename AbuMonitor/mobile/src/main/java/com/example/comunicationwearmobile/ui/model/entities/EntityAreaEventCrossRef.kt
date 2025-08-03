package com.example.comunicationwearmobile.ui.model.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.data.model.EntityEvent

//Esta seria la tabla de relacion N a N entre AreaGeofence y Event
@Entity(
    tableName = "Area_Event",
    primaryKeys = ["id_area", "id_event"],
    foreignKeys = [
        ForeignKey(
            entity = EntityAreaGeofence::class,
            parentColumns = ["id_area"],
            childColumns = ["id_area"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = EntityEvent::class,
            parentColumns = ["id_event"],
            childColumns = ["id_event"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        androidx.room.Index(value = ["id_area"]),
        androidx.room.Index(value = ["id_event"])
    ]
)
data class EntityAreaEventCrossRef(
    var id_area: Long=0,
    val id_event: Int=0
)
