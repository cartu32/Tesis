package com.example.comunicationwearmobile.Interface

import com.example.comunicationwearmobile.models.maps.RouteApiOSRMJson
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface RouteMaker {
    @GET
    fun getAllWayPoints(@Url url: String?): Call<RouteApiOSRMJson?>?
}
