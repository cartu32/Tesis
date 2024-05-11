package com.example.geofencing.Presenter;

import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import com.example.geofencing.Util.Tools;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;

import java.util.ArrayList;

public class GeofenceHelper extends ContextWrapper {

    private static final String TAG = "GeofenceHelper";
    private PendingIntent pendingIntent;
    private ArrayList<Geofence> geofenceList = new ArrayList<>();

    public GeofenceHelper(Context base) {
        super(base);
    }

    public GeofencingRequest getGeofencingRequest() {

        return new GeofencingRequest.Builder()
                .addGeofences(geofenceList)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();
    }

    public void addGeofenceList(String ID, double latitude, double longitude, float radius, int transitionTypes) {
        geofenceList.add(new Geofence.Builder()
                 .setCircularRegion(latitude,longitude, radius)
                .setRequestId(ID)
                .setTransitionTypes(transitionTypes)
                .setLoiteringDelay(5000)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build()
        );
    }

    public ArrayList getGeofenceList(){
        return this.geofenceList;
    }

    public PendingIntent getPendingIntent() {
        if (pendingIntent != null) {
            return pendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionService.class);
        intent.putExtra("Operation", Tools.GEOFENCE_TRANSITION);
        pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_MUTABLE);


        return pendingIntent;
    }

    public void clearGeofenceList(){
        this.geofenceList.clear();
        pendingIntent=null;
    }
    public String getErrorString(Exception e) {
        if (e instanceof ApiException) {
            ApiException apiException = (ApiException) e;
            switch (apiException.getStatusCode()) {
                case GeofenceStatusCodes
                        .GEOFENCE_NOT_AVAILABLE:
                    return "GEOFENCE_NOT_AVAILABLE";
                case GeofenceStatusCodes
                        .GEOFENCE_TOO_MANY_GEOFENCES:
                    return "GEOFENCE_TOO_MANY_GEOFENCES";
                case GeofenceStatusCodes
                        .GEOFENCE_TOO_MANY_PENDING_INTENTS:
                    return "GEOFENCE_TOO_MANY_PENDING_INTENTS";
            }
        }
        return e.getLocalizedMessage();
    }
}
