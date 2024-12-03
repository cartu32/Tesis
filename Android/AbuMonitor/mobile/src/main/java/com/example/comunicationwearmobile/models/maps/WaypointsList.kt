package com.example.comunicationwearmobile.models.maps

import com.google.gson.annotations.SerializedName

class WaypointsList {
    @SerializedName("hint")
    var hint: String? = null

    @SerializedName("distance")
    var distance: Float = 0f

    @SerializedName("location")
    var location: List<*>? = null

    @SerializedName("name")
    var name: String? = null
}
