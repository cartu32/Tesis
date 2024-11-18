package com.example.comunicationwearmobile.presenter.maps;

import android.util.Log;

import com.example.comunicationwearmobile.Interface.InterfaceConfigGeofence;
import com.example.comunicationwearmobile.Interface.RouteMaker;
import com.example.comunicationwearmobile.models.maps.JsonRoutePojo;
import com.example.comunicationwearmobile.models.maps.RoutesList;
import com.example.comunicationwearmobile.utils.maps.PolylineDecoder;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsRoute {

    private static final String TAG = "MapsRoute";

    private static final String BASE_URL = "https://router.project-osrm.org/route/v1/foot/";
    private InterfaceConfigGeofence caller=null;

    public MapsRoute(InterfaceConfigGeofence activity) {
        this.caller=(InterfaceConfigGeofence) activity;
    }

    public void getWaypoints( LatLng origin, LatLng destination,String idArea) {

        String coordenateOrigin,coordenateDestination,url;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        coordenateOrigin=Double.toString(origin.longitude)+","+Double.toString(origin.latitude);
        coordenateDestination=Double.toString(destination.longitude)+","+Double.toString(destination.latitude);

        url = BASE_URL + coordenateOrigin +";"+coordenateDestination;

        RouteMaker service = retrofit.create(RouteMaker.class);

        Call<JsonRoutePojo> response = service.getAllWayPoints(url);
        response.enqueue(new Callback<JsonRoutePojo>() {

            @Override
            public void onResponse(Call<JsonRoutePojo> call, Response<JsonRoutePojo> response) {
                List <RoutesList> route=null;
                List <LatLng> latLngList=null;
                String geometry;

                if (!response.body().getCode().isEmpty()) {
                    route= response.body().getRoutes();
                    geometry = route.get(0).getGeometry();

                    Log.d(TAG, response.body().getCode());
                    latLngList=PolylineDecoder.decode(geometry);

                    Log.d(TAG, latLngList.toString());

                    caller.graphRoute(latLngList,idArea);
                }

            }

            @Override
            public void onFailure(Call<JsonRoutePojo> call, Throwable t) {
                Log.e(TAG, "Error " + t.getMessage());
                caller.showMessage("Error " + t.getMessage());
            }
        });
    }
}

