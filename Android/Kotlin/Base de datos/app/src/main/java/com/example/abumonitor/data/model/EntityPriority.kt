package com.example.abumonitor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Priority")
data class EntityPriority(
    @PrimaryKey var id_priority: Int =0 ,
    var description: String
)

