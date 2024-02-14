package com.example.geofencing.Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class JsonRoutePojo {
    @SerializedName("code")
    private String code;

    @SerializedName("waypoints")
    private List<WaypointsList> waypoints;

    @SerializedName("routes")
    private List<RoutesList> routes;

    public void setCode(String code){this.code=code;}
    public String getCode(){return this.code;}

    public void setWaipoints(List<WaypointsList> waypoints){
        this.waypoints=waypoints;
    }
    public List<WaypointsList> getWaipoints(){return this.waypoints;}

    public void setRoutes(List<RoutesList> routes){
        this.routes=routes;
    }
    public List<RoutesList> getRoutes(){return this.routes;}
}

