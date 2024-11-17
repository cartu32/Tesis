package com.example.geofencing.Interface;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public interface InterfaceConfigGeofence {
    void clearMaps();
    void updateCircleRadiusGraphic(float geofenceRadius);
    void updateCircleColorGraphic(String id);
    void addMarkerGeofence(LatLng latLng, float radius);
    void showMessage(String message);
    void graphRoute(List<LatLng> latLngList, String idArea);
}
