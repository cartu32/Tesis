package com.example.abumonitor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Type_Area")
data class EntityTypeArea(
    @PrimaryKey var id_type_area: Int =0 ,
    var description: String
)
