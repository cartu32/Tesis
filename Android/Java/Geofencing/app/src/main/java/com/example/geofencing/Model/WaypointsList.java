package com.example.geofencing.Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WaypointsList {

    @SerializedName("hint")
    private String hint;

    @SerializedName("distance")
    private float distance;

    @SerializedName("location")
    private List location;

    @SerializedName("name")
    private String name;

    public void setHint(String hint){this.hint=hint;}
    public String getHint(){return this.hint;}

    public void setDistance(float distance){this.distance=distance;}
    public float getDistance(){return this.distance;}

    public void setLocation(List location){this.location=location;}
    public List getLocation(){return this.location;}

    public void setName(String name){this.name=name;}
    public String getName(){return this.name;}
}
