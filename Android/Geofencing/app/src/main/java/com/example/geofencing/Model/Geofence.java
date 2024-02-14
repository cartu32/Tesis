package com.example.geofencing.Model;

import java.io.Serializable;

public class Geofence implements Serializable
{
    private String idArea;
    private Double latitude;
    private Double longitude;
    private float radius;

    public void setIdArea(String id){ this.idArea=id;}

    public String getIdArea(){return this.idArea;}

    public void setLatitud(Double latitud) {
        this.latitude = latitud;
    }

    public void setLongitud(Double longitud) { this.longitude = longitud; }

    public Double getLatitud() {
        return this.latitude;
    }

    public Double getLongitud() {
        return this.longitude;
    }

    public void setRadius(float radius){ this.radius=radius;}

    public float getRadius(){return this.radius;}
}
