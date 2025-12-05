package com.example.comunicationwearmobile.ui.model.dto

import android.os.Parcelable
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.comunicationwearmobile.ui.model.entities.EntityAreaRuntimeState
import com.example.comunicationwearmobile.ui.model.entities.EntitySecurityZoneTimeRange
import kotlinx.parcelize.Parcelize

//data class intermedia temporal que se usa para pasar los datos que se ingresan
// desde la actitivty properties al viewmodel cuando se ingresa un area nueva.
@Parcelize
data class DataAreaGeofAux(
    var entityAreaGeofence: EntityAreaGeofence=EntityAreaGeofence(),
    var listIdEventSelected: MutableList<Int> = mutableListOf(),
    var secZoneTimeRange: EntitySecurityZoneTimeRange?=EntitySecurityZoneTimeRange(),
    var entityAreaRuntimeState: EntityAreaRuntimeState=EntityAreaRuntimeState()
):Parcelable
