package com.example.comunicationwearmobile.ui.model.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Parcelize
@Entity(
    tableName = "DwellTimeZone",
    foreignKeys = [ForeignKey(
        entity = EntityDwellTimeZone::class,
        parentColumns = ["id_area"],
        childColumns = ["id_area"],
        onDelete = ForeignKey.CASCADE //si se borra el area se borra el time range
    )],
    indices = [Index(value = ["id_area"], unique = true)] //con unique aseguro que la reacion sea 1 a 1
)
data class EntityDwellTimeZone(
    @PrimaryKey(autoGenerate = true)
    var id_area: Long = 0,
    var dwell_time: Long = 0
) : Parcelable
