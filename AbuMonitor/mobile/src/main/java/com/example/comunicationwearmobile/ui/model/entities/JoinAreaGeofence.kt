package com.example.abumonitor.data.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.comunicationwearmobile.ui.model.datasource.local.DaoSecurityZoneTimeRange
import com.example.comunicationwearmobile.ui.model.entities.EntityAreaEventCrossRef
import com.example.comunicationwearmobile.ui.model.entities.EntitySecurityZoneTimeRange
import java.sql.Time

@kotlinx.parcelize.Parcelize
data class JoinAreaGeofence (
    @Embedded var areaGeofence: EntityAreaGeofence,

 /*   @Relation(
        parentColumn = "id_color",
        entityColumn = "id_color"
    )
    var color: EntityColor,*/
    @Relation(
        parentColumn = "id_priority",
        entityColumn = "id_priority"
    )
    var priority: EntityPriority,

    @Relation(
        parentColumn = "id_area",                 // de EntityAreaGeofence
        entityColumn = "id_event",                // de EntityEvent
        associateBy = Junction(
            value = EntityAreaEventCrossRef::class,
            parentColumn = "id_area",             // campo en EntityAreaEventCrossRef
            entityColumn = "id_event"             // campo en EntityAreaEventCrossRef
        )
    )
    val events: List<EntityEvent>,

    @Relation(
        parentColumn = "id_area",
        entityColumn = "id_area"
    )
    val securityZoneTimeRange: EntitySecurityZoneTimeRange?,//ya que es de 1 a 1 puede ser que no sea zona segura entonces no tenga time range, por lo que seria nulo
): Parcelable
