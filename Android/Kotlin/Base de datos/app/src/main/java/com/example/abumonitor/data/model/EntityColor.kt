package com.example.abumonitor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Color")
data class EntityColor(
    @PrimaryKey var id_color: Int =0 ,
    var description: String
)
