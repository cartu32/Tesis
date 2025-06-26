package com.example.abumonitor.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "Color")
data class EntityColor(
    @PrimaryKey var id_color: Int =0 ,
    var description: String
):Parcelable
