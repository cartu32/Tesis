package com.example.comunicationwearmobile.ui.model.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "Area_Runtime_State",
    foreignKeys = [ForeignKey(
        entity = EntityAreaGeofence::class,
        parentColumns = ["id_area"],
        childColumns = ["id_area"],
        onDelete = ForeignKey.CASCADE //si se borra el area se borra el time range
    )],
    indices = [
        Index(value = ["id_area"], unique = true),//con unique aseguro que la reacion sea 1 a 1
    ]
)
data class EntityAreaRuntimeState(
    @PrimaryKey var id_area: Long=0,
    val is_activated_geof:Boolean=false,                  //indica si el area esta activa
    var prev_state_machine:String=Definition.ST_INIT, //Indica el estado en que quedo la maquina de estado de esa area
    val last_update_time:Long=0,                        //indica el ultimo tiempo en que se actualizo el estado
):Parcelable
