package com.example.comunicationwearmobile.ui.model.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "Type_Area")
data class EntityTypeArea(
    @PrimaryKey var id_type_area: Int =0,
    var description: String,
    var color: Int
): Parcelable
