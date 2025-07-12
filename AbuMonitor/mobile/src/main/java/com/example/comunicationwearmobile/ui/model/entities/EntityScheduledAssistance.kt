package com.example.abumonitor.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "Scheduled_Assistance",
    foreignKeys = [ForeignKey(
        entity = EntityAreaGeofence::class,
        parentColumns = ["id_area"],
        childColumns = ["id_area"],
        onDelete = ForeignKey.CASCADE //si se borra el area se borra el time range
    )],
    indices = [Index(value = ["id_area"], unique = true)] //con unique aseguro que la reacion sea 1 a 1
)data class EntityScheduledAssistance(
    @PrimaryKey(autoGenerate = true)
    var id_assistance: Int = 0,
    var title: String = "",
    var date_appointment:Long=0, //dia en que tiene la cita
    var hour_appointment:Long=0, //hora en que tiene la cita
    var hour_attendance:Long=0,  //hora en que realmente asisto a la cita
    var description:String="",
    var status:Boolean=false,
    var id_area: Long = 0,
    ): Parcelable
