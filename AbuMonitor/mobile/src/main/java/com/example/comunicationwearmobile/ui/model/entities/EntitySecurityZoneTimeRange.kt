package com.example.comunicationwearmobile.ui.model.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.abumonitor.data.model.EntityAreaGeofence
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "SecurityZoneTimeRange",
    foreignKeys = [ForeignKey(
        entity = EntityAreaGeofence::class,
        parentColumns = ["id_area"],
        childColumns = ["id_area"],
        onDelete = ForeignKey.CASCADE //si se borra el area se borra el time range
    )],
    indices = [Index(value = ["id_area"], unique = true)] //con unique aseguro que la reacion sea 1 a 1
)
data class EntitySecurityZoneTimeRange(
    @PrimaryKey(autoGenerate = true)  var id_time_range: Int = 0,
    var id_area: Long = 0,
    var min_hour: String = "00:00",
    var max_hour: String = "00:00"
) : Parcelable



