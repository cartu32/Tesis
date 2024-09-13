package com.example.geofencing.Model;

import java.io.Serializable;
import java.sql.Time;

public class Route implements Serializable {
    private String idArea;
    private Double latitudeOrigin;
    private Double longitudeOrigin;
    private Double latitudeDestination;
    private Double longitudeDestination;
    private float radius;
    private Time schedule;
    private int tolerance;

    public void setIdArea(String id){ this.idArea=id;}

    public String getIdArea(){return this.idArea;}

    public void setLatitudeOrigin(Double latitud) {
        this.latitudeOrigin = latitud;
    }

    public void setLongitudeOrigin(Double longitud) { this.longitudeOrigin = longitud; }


    public Double getLatitudeOrigin() {
        return this.latitudeOrigin;
    }

    public Double getLongitudeOrigin() {
        return this.longitudeOrigin;
    }

    public Double getLongitudeDestination() {
        return this.longitudeDestination;
    }

    public void setLatitudeDestination(Double latitud) {
        this.latitudeDestination = latitud;
    }

    public void setLongitudeDestination(Double longitud) { this.longitudeDestination = longitud; }

    public Double getLatitudeDestination() {
        return this.latitudeDestination;
    }


    public void setRadius(float radius){ this.radius=radius;}

    public float getRadius(){return this.radius;}

    public int getTolerance() {
        return tolerance;
    }

    public void setTolerance(int tolerance) {
        this.tolerance = tolerance;
    }

    public Time getSchedule() {
        return schedule;
    }

    public void setSchedule(Time schedule) {
        this.schedule = schedule;
    }
}
