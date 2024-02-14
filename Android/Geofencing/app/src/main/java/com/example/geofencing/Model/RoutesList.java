package com.example.geofencing.Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RoutesList {
    @SerializedName("legs")
    private List legs;

     @SerializedName("weight_name")
    private String weightName;

    @SerializedName("geometry")
    private String geometry;

    @SerializedName("weight")
    private float weight;

    @SerializedName("distance")
    private float distance;

    @SerializedName("duration")
    private float duration;

    public void setLegs(List legs){this.legs=legs;}
    public List  getLegs(){return this.legs;}

    public void setWeightName(String weightName){this.weightName=weightName;}
    public String getWeightName(){return this.weightName;}

    public void setGeometry(String geometry){this.geometry=geometry;}
    public String getGeometry(){return this.geometry;}

    public void setWeight(float weight){this.weight=weight;}
    public float getWeight(){return this.weight;}

    public void setDistance(float distance){this.distance=distance;}
    public float getDistance(){return this.distance;}

    public void setDuration(float duration){this.duration=duration;}
    public float getDuration(){return this.duration;}

}
