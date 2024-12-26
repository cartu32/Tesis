package com.example.abumonitor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Event")
data class EntityEvent(
    @PrimaryKey var id_event: Int =0,
    var description: String
)
