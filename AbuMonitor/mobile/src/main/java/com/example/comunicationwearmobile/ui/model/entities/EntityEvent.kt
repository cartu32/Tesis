package com.example.abumonitor.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "Event")
data class EntityEvent(
    @PrimaryKey var id_event: Int =0,
    var description: String
):Parcelable
