package com.example.comunicationwearmobile.ui.model.dto

import android.os.Parcelable
import com.example.abumonitor.data.model.EntityAreaGeofence
import kotlinx.parcelize.Parcelize

//data class intermedia temporal que se usa para pasar los datos que se ingresan
// desde la actitivty properties al viewmodel cuando se ingresa un area nueva.
@Parcelize
data class DataAreaGeofAux(
    val entityAreaGeofence: EntityAreaGeofence=EntityAreaGeofence(),
    val listIdEventSelected: MutableList<Int> = mutableListOf()
):Parcelable
