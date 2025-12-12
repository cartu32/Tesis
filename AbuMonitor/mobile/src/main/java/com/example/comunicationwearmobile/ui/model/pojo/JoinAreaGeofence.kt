package com.example.comunicationwearmobile.ui.model.pojo

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.data.model.EntityEvent
import com.example.comunicationwearmobile.ui.model.entities.EntityAreaEventCrossRef
import com.example.comunicationwearmobile.ui.model.entities.EntityDwellTimeZone
import com.example.comunicationwearmobile.ui.model.entities.EntitySecurityZoneTimeRange

@kotlinx.parcelize.Parcelize
data class JoinAreaGeofence (
    @Embedded var areaGeofence: EntityAreaGeofence,

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

    @Relation(
        parentColumn = "id_area",
        entityColumn = "id_area"
    )
    val secDwellTimeZone: EntityDwellTimeZone?

): Parcelable
