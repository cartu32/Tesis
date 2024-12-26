package com.example.abumonitor.data.model

import androidx.room.Embedded
import androidx.room.Relation

//Esta es la definicion de las relaciones que tiene la tabla EntityAreaGeofence con
//las demàs tablas. Aca indicamos si es de 1 a 1, de 1 a N.

data class RelationAreaGeofence(
    @Embedded var areaGeofence: EntityAreaGeofence ,

    @Relation(
        parentColumn = "id_color",
        entityColumn = "id_color"
    )
    var color: List<EntityColor> , //Como es de 1 a N delvuele una lista

    @Relation(
        parentColumn = "id_event",
        entityColumn = "id_event"
    )
    var event: List<EntityEvent> ,

    @Relation(
        parentColumn = "id_priority",
        entityColumn = "id_priority"
    )
    var priority: List<EntityPriority> ,

    @Relation(
        parentColumn = "id_reminder",
        entityColumn = "id_reminder"
    )
    var reminder: EntityReminder? , //Como es de 1 a 1 delvuele una variable

    @Relation(
        parentColumn = "id_priority",
        entityColumn = "id_priority"
    )
    var typePriority: List<EntityPriority> ,

    @Relation(
        parentColumn = "id_type_area",
        entityColumn = "id_type_area"
    )
    var typeArea: List<EntityTypeArea> ,

    @Relation(
        parentColumn = "id_contact",
        entityColumn = "id_contact"
    )
    var typeContact: List<EntityContact>
)
