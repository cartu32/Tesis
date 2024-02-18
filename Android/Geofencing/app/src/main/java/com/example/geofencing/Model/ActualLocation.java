package com.example.geofencing.Model;

public class ActualLocation
{
    private Double latitude;
    private Double longitude;

    private Double altitude;

    public void setLatitud(Double latitud) {
        this.latitude = latitud;
    }

    public void setLongitud(Double longitud) { this.longitude = longitud; }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public Double getLatitud() {
        return this.latitude;
    }

    public Double getLongitud() {
        return this.longitude;
    }

    public Double getAltitude() {
        return this.altitude;
    }
}
