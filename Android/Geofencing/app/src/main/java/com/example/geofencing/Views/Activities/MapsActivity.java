package com.example.geofencing.Views.Activities;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.FragmentActivity;

import com.example.geofencing.Interface.InterfaceConfigGeofence;
import com.example.geofencing.Presenter.MapsActivtyPresenter;
import com.example.geofencing.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMapClickListener, LocationListener, InterfaceConfigGeofence {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private AlertDialog alert = null;
    private MapsActivtyPresenter mapsActivtyPresenter;
    // intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;
    private Circle circle;
    private MarkerOptions geoFenceMarker = null;

    private Marker previousAncientMarker=null;
    private float zoomLevel = 19.0f; //This goes up to 21

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mapsActivtyPresenter = new MapsActivtyPresenter(this);


    }


    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            Dialog dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, resultCode, RC_HANDLE_GMS);
            dlg.show();
        }

        mMap.setOnMapLongClickListener(this);
        mMap.setOnMapClickListener(this);

        mapsActivtyPresenter.checkPermisson();

        //elimino todos los geofences que pudieron haber quedado cargados, si la aplicacion se cerro
        // anteriormente por la ocurrencia de algun error.
        mapsActivtyPresenter.clearGeofenceMaps();

        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);

        mapsActivtyPresenter.connectMqtt();


    }

    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mapsActivtyPresenter.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }


    public Location getLocation() {
        return mapsActivtyPresenter.getLocation();
    }


    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng;

        latLng = new LatLng(location.getLatitude(), location.getLongitude());

        positionUpdate(latLng);
        mapsActivtyPresenter.checkGeofenceRoute(latLng);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public boolean positionUpdate(LatLng latLng) {



        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));

        return true;
    }


    public void enableMap() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);

        getLocation();
    }

    private Marker addMarker(LatLng latLng) {
        Marker marker;

        geoFenceMarker = new MarkerOptions().position(latLng);
        marker = mMap.addMarker(geoFenceMarker);

        return marker;
    }

    public void updateLocationSenior(LatLng latLng)
    {
        if(previousAncientMarker!=null)
            previousAncientMarker.remove();

        previousAncientMarker=addMarker(latLng);
        positionUpdate(latLng);

    }

    public void addMarkerGeofence(LatLng latLng, float radius) {
        addMarker(latLng);
        addCircle(latLng, radius);
    }


    private void addCircle(LatLng latLng, float radius) {

        int colorCircle;
        int alpha = 64;

        colorCircle = mapsActivtyPresenter.determineColor();

        this.circle = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .strokeColor(colorCircle)
                .fillColor(ColorUtils.setAlphaComponent(colorCircle, alpha))
                .radius(radius)
                .strokeWidth(4));

    }


    @Override
    public void onMapLongClick(LatLng latLng) {
        handleMapLongClick(latLng);

    }


    @Override

    protected void onDestroy() {
        super.onDestroy();

        mapsActivtyPresenter.closeActivity();

        if (alert != null) {
            alert.dismiss();
        }
    }

    public void clearMaps() {
        mMap.clear();
        circle = null;
    }


    @Override
    public void updateCircleRadiusGraphic(float geofenceRadius) {
        circle.setRadius(geofenceRadius);
    }

    public void updateCircleColorGraphic(String id) {
        int colorCircle;
        int alpha = 64;

        colorCircle = mapsActivtyPresenter.determineColor();

        circle.setStrokeColor(colorCircle);
        circle.setFillColor(ColorUtils.setAlphaComponent(colorCircle, alpha));
    }


    @Override
    public void graphRoute(List<LatLng> coordenateList,String idArea) {

        mMap.addPolyline(new PolylineOptions().addAll(coordenateList));

        mapsActivtyPresenter.saveRouteInFile(coordenateList,idArea);
    }

    private void handleMapLongClick(LatLng latLng) {
        mapsActivtyPresenter.addAreaGeofenceInList(latLng);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mapsActivtyPresenter.markRoutePoint(latLng);
    }
}
    
