package com.example.geofencing.Interface;

import com.example.geofencing.Model.JsonRoutePojo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface RouteMaker {
    @GET
    Call<JsonRoutePojo> getAllWayPoints(@Url String url);
}
