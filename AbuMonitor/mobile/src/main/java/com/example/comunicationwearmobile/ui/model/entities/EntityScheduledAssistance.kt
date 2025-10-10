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
    var description:String="",
    var went_appointment:Boolean=false, //indica si fue a la cita
    var is_activated_geof:Boolean=false, //indica si esta activada la geofence de la cita
    var date_hour_appointment:Long=0, //dia y hora en que tiene la cita
    var date_hour_enter_assistance:Long=0, //dia y hora en que realmente asisto a la cita
    var date_hour_exit_assistance:Long=0,  //dia y hora en que realmente salio de la cita
    var time_duration_activation_appointment:Long=0, //tiempo que se mantiene activa el area de geof de la cita
    var id_area: Long = 0,
    ): Parcelable
