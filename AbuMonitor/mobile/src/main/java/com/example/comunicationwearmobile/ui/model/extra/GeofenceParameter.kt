package com.example.comunicationwearmobile.ui.model.extra

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GeofenceEventParameter(
    var triggeringIds: MutableList<Long> = mutableListOf(),
    var transition: Int = 0
) : Parcelable

