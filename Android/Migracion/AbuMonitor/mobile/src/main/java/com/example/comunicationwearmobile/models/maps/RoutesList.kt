package com.example.comunicationwearmobile.models.maps

import com.google.gson.annotations.SerializedName

class RoutesList {
    @SerializedName("legs")
    var legs: List<*>? = null

    @SerializedName("weight_name")
    var weightName: String? = null

    @JvmField
    @SerializedName("geometry")
    var geometry: String? = null

    @SerializedName("weight")
    var weight: Float = 0f

    @SerializedName("distance")
    var distance: Float = 0f

    @SerializedName("duration")
    var duration: Float = 0f
}
