package com.example.comunicationwearmobile.models.maps

import com.google.gson.annotations.SerializedName

class JsonRoutePojo {
    @JvmField
    @SerializedName("code")
    var code: String? = null

    @SerializedName("waypoints")
    var waipoints: List<WaypointsList>? = null

    @JvmField
    @SerializedName("routes")
    var routes: List<RoutesList>? = null
}

