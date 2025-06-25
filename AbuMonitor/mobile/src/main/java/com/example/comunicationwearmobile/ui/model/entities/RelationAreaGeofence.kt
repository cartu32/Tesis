package com.example.abumonitor.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.comunicationwearmobile.ui.model.entities.EntityAreaEventCrossRef

//Esta es la definicion de las relaciones que tiene la tabla EntityAreaGeofence con
//las demàs tablas. Aca indicamos si es de 1 a 1, de 1 a N.

data class RelationAreaGeofence(
    @Embedded var areaGeofence: EntityAreaGeofence ,

    @Relation(
        parentColumn = "id_color",
        entityColumn = "id_color"
    )
    var color: EntityColor ,
    @Relation(
        parentColumn = "id_priority",
        entityColumn = "id_priority"
    )
    var priority: EntityPriority ,

    @Relation(
        parentColumn = "id_area",
        entityColumn = "id_area",
        associateBy = Junction(EntityAreaEventCrossRef::class)
    )
    var events: List<EntityEvent>
)
