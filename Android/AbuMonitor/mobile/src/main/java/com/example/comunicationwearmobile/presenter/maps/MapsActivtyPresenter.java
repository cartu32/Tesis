package com.example.comunicationwearmobile.presenter.maps;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.comunicationwearmobile.models.maps.Route;
import com.example.comunicationwearmobile.models.maps.SharedPreferencesRoutes;
import com.example.comunicationwearmobile.utils.maps.Tools;
import com.example.comunicationwearmobile.ui.Activities.MapsActivity;
import com.example.comunicationwearmobile.ui.Fragments.fragment_config_geofence;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivtyPresenter {
    //private final String NAME_SHARED_PREFERENCE="File_Routes";

    private final int MULTIPLE_PERMISSON_REQUEST_CODE = 10003;
    public static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 15; //metros
    public static final long MIN_TIME_BW_UPDATES = 1000 * 30;       //segundos
    private GeofenceHelper geofenceHelper;
    private GeofencingClient geofencingClient;

    private int cantGeofences=0;

    private static final String TAG = "MapsActivityPresenter";
    private HashMap<String, Integer> hashMapId = new HashMap<String, Integer>();
    /*Se declara una variable de tipo LocationManager encargada de proporcionar acceso al servicio de localización del sistema.*/
    private LocationManager locationManager;
    /*Se declara una variable de tipo Location que accederá a la última posición conocida proporcionada por el proveedor.*/
    private Location location;
    private Boolean isGPSEnabled=false,isNetworkEnabled=false;

    MapsActivity activity;

    private String idAreaActiva = Tools.DEFAULT_ACTIVE_AREA;
    private static final int SELECT_ORIGIN_POINT = 1;
    private static final int SELECT_DESTINATION_POINT = 2;
    private float GEOFENCE_RADIUS_DEFAULT = 100;
    private int selectPoint = SELECT_ORIGIN_POINT;
    private LatLng originPoint;
    private LatLng destinationPoint;

    private fragment_config_geofence frag = null;
    List<LatLng> waypointsActiveRoute = null;

    public IntentFilter filtro;
    public IntentFilter filtroExterno;
    private ReceptorOperation receiver = new ReceptorOperation();
    private ReceptorNotificacion receiverExtern = new ReceptorNotificacion();


    //Array que contiene las areas de Geofencing agregadas manualmente por el usuario
    private ArrayList<com.example.comunicationwearmobile.models.maps.Geofence> listAreaAddedManual = new ArrayList<com.example.comunicationwearmobile.models.maps.Geofence>();

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public MapsActivtyPresenter(MapsActivity activity) {

        this.activity = activity;

        geofenceHelper = new GeofenceHelper(activity);
        geofencingClient = LocationServices.getGeofencingClient(activity);


        configureBroadcastReciever();

    }


    public void checkPermisson()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                activity.enableMap();
            } else
            {
                activity.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION}, MULTIPLE_PERMISSON_REQUEST_CODE);
            }
        }
        else
        {
            activity.enableMap();
        }
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //We have the permission
            activity.showMessage("Ahora puedes agregar puntos de Georeferencia");
            activity.enableMap();
        } else {
            //We do not have the permission..
            activity.showMessage("El acceso a la ubicación en segundo plano es necesario para que se activen las geocercas...");
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void configureBroadcastReciever() {
        //se asocia(registra) la  accion RESPUESTA_OPERACION, para que cuando el Servicio de recepcion la ejecute
        //se invoque automaticamente el OnRecive del objeto receiver
        filtro = new IntentFilter("com.example.intentservice.intent.action.RESPUESTA_OPERACION");
        filtroExterno = new IntentFilter("com.example.intentservice.intent.action.NOTIFICACION_FIREBASE");

        filtro.addCategory(Intent.CATEGORY_DEFAULT);
        filtroExterno.addCategory(Intent.CATEGORY_ALTERNATIVE);

        activity.getApplicationContext().registerReceiver(receiver, filtro, Context.RECEIVER_NOT_EXPORTED);
        activity.getApplicationContext().registerReceiver(receiverExtern, filtroExterno, Context.RECEIVER_NOT_EXPORTED);

    }

    public Location getLocation() {
        try {

            if (checkconnection()) {
                // Si no hay proveedor habilitado
                //solicito que active el gps
                activity.alertNoGps();
            }

            // if GPS Enabled get lat/long using GPS Services
            if (isGPSEnabled) {
                setPositionGPS();
            }else if (isNetworkEnabled) {
                setPositionNetwork();
            }

        } catch (Exception e) {
            Log.e("getLocation", e.getMessage());
        }
        return location;
    }

    public void setPositionGPS() {
        if (location == null) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) activity);
            if (locationManager != null) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    activity.positionUpdate(location);
                }
            }
        }
    }

    void setPositionNetwork(){
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) activity);
        if (locationManager != null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                activity.positionUpdate(location);
            }
        }

    }

    public void checkGeofenceRoute(LatLng latLng){
        boolean resp;

        if(waypointsActiveRoute!=null) {
            resp = PolyUtil.isLocationOnPath(latLng, waypointsActiveRoute, true, 300);
            Log.d("Respuesta", Boolean.toString(resp));

            if(resp==false){

                Intent intent = new Intent(activity.getApplicationContext(), GeofenceTransitionService.class);
                intent.putExtra("Operation", Tools.GEOFENCE_ROUTE);
                activity.startService(intent);

            }
        }

    }
    public boolean generateGeofences(ArrayList<com.example.comunicationwearmobile.models.maps.Geofence> listLastGeofence){

        //Creo el identificador de cada zona de goefoence que empiece por una letra identifcadora.
        //String idGeofences="A";
        int idAux=0;

        if(listLastGeofence.size()==0)
            return false;

        if(checkconnection()){
            activity.showMessage("No hay conexion de GPS o Red");
            return false;
        }

        Log.d("Alerta", "Entrando en For");
        for (int i = 0; i < listLastGeofence.size(); i++) {
            //cantGeofences++;
            idAux=createHashMapId(listLastGeofence.get(i).getIdArea());
            geofenceHelper.addGeofenceList(listLastGeofence.get(i).getIdArea()+idAux,
                    listLastGeofence.get(i).getLatitud(),
                    listLastGeofence.get(i).getLongitud() ,
                    listLastGeofence.get(i).getRadius(),
                    Geofence.GEOFENCE_TRANSITION_ENTER);
            Log.d("Alerta", "Ejecutando For");
        }

        return activateGefenceRequest();


    }

    private int createHashMapId(String idArea){
        Integer cantId= null;
        cantId = hashMapId.get(idArea);


        if(cantId==null) {
            hashMapId.put(idArea,0);
            cantId=0;
        }else{
            cantId++;
            hashMapId.put(idArea,cantId);
        }
        return cantId;
    }

    private int getHashMapIdOrigin(String idArea){
        Integer idOrigin= null;
        int DESTINO=1;

        idOrigin= hashMapId.get(idArea);

        //como este metodo es invocado cuando se genera la ruta, entonces
        //entonces en el Hashmap hay grabados dos ID. El primero el origen y despues el destino
        //Con lo cual para obtener el idOrigen correcto, debo restarle 1 para poder obtenerlo. Porque
        //sino me devolveria el idDestino.

        if(idOrigin!=null){
            idOrigin=idOrigin-DESTINO;
        }

        return idOrigin;
    }
    public boolean activateGefenceRequest(){
        GeofencingRequest geofencingRequest=geofenceHelper.getGeofencingRequest();

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        geofencingClient.addGeofences(geofencingRequest, geofenceHelper.getPendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Geofence Added...");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = geofenceHelper.getErrorString(e);

                        Log.d(TAG, "onFailure: " + errorMessage);
                        activity.showMessage("on Failure"+errorMessage);
                    }
                });
        return true;
    }

    public void saveRouteInFile( List<LatLng> listRoute,String idArea){
         int numberId=getHashMapIdOrigin(idArea);

        SharedPreferencesRoutes.setStringArrayPref(this.activity, idArea+numberId, listRoute);
    }

    public void clearRouteInFile(){
        SharedPreferencesRoutes.clearSharedPreferences(this.activity);

    }
    public void clearGeofencesIntent(){

        cantGeofences=0;
        geofencingClient.removeGeofences(geofenceHelper.getPendingIntent())
                .addOnSuccessListener(activity, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences removed
                        // ...
                        Log.d(TAG,"se borraron todos los geofences");
                    }
                })
                .addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to remove geofences
                        Log.d(TAG,"Error al borrar todos los geofences");
                    }
                });

        hashMapId.clear();
        geofenceHelper.clearGeofenceList();
    }

    public com.example.comunicationwearmobile.models.maps.Geofence createRetrofitGeofence(String id, LatLng latLng, float radius){
        com.example.comunicationwearmobile.models.maps.Geofence obj = new com.example.comunicationwearmobile.models.maps.Geofence();

        obj.setIdArea(id);
        obj.setRadius(radius);
        obj.setLatitud(latLng.latitude);
        obj.setLongitud(latLng.longitude);

        return obj;
    }

    public int determineColor(){
        int color=0;

        switch(idAreaActiva){

            case "A":
                color=Color.RED;
                break;
            case "B":
                color= Color.GREEN;
                break;
            case "C":
                color=Color.MAGENTA;
                break;
            case"Ruta Z":
                color=Color.BLUE;
                break;
            default:
                Log.e("TAG", "Error al determinar el color");
        }
        return color;
    }

    private boolean checkconnection(){
        locationManager = (LocationManager) activity.getApplicationContext()
                .getSystemService(activity.getApplicationContext().LOCATION_SERVICE);

        // getting GPS status
        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        return (!isGPSEnabled && !isNetworkEnabled);
    }

    public void closeFragment() {
        Fragment prev = activity.getSupportFragmentManager().findFragmentById(frag.getId());

        if (prev != null)
            activity.getSupportFragmentManager().beginTransaction().remove(prev).commit();
    }

    public void addAreaGeofenceInList(LatLng latLng){
        listAreaAddedManual.add(createRetrofitGeofence(idAreaActiva, latLng, GEOFENCE_RADIUS_DEFAULT));

        activity.addMarkerGeofence(latLng,GEOFENCE_RADIUS_DEFAULT);

        frag = new fragment_config_geofence(this, GEOFENCE_RADIUS_DEFAULT, true);
        frag.show(activity.getSupportFragmentManager(), fragment_config_geofence.class.getSimpleName());

    }

    public void  generateGeofencesManual(){
        if(generateGeofences(listAreaAddedManual)==true){
            closeFragment();
            activity.showMessage("geofences agregadas");
        } else {
            activity.showMessage("Error al agragar geofences");
        }
        listAreaAddedManual.clear();
    }

    public void markRoutePoint(LatLng latLng){
        switch (selectPoint) {
            case SELECT_ORIGIN_POINT:
                originPoint = latLng;
                selectPoint = SELECT_DESTINATION_POINT;

                //dibujo en el mapa el area de origen
                activity.addMarkerGeofence(latLng,GEOFENCE_RADIUS_DEFAULT);
                listAreaAddedManual.add(createRetrofitGeofence(idAreaActiva, latLng, GEOFENCE_RADIUS_DEFAULT));

                break;

            case SELECT_DESTINATION_POINT:
                destinationPoint = latLng;
                selectPoint = SELECT_ORIGIN_POINT;

                //dibujo en el mapa el area de destino
                activity.addMarkerGeofence(latLng,GEOFENCE_RADIUS_DEFAULT);
                listAreaAddedManual.add(createRetrofitGeofence(idAreaActiva, latLng, GEOFENCE_RADIUS_DEFAULT));

                //abro el menu fragment
                frag = new fragment_config_geofence(this, GEOFENCE_RADIUS_DEFAULT,false);
                frag.show(activity.getSupportFragmentManager(), fragment_config_geofence.class.getSimpleName());

                break;
            default:
                activity.showMessage("Error en switch de ruta");
        }
    }

    public void generateRouteManual(){
        generateGeofencesManual();
        closeFragment();
    }

    public void updateCircleRadius(float geofenceRadius){
        activity.updateCircleRadiusGraphic(geofenceRadius);

        //obtengo el ultimo item agregado al listado
        com.example.comunicationwearmobile.models.maps.Geofence aux = listAreaAddedManual.get(listAreaAddedManual.size() - 1);
        //lo actualizo con el valor del id seleccionado
        aux.setRadius(geofenceRadius);
        //actualizo el item en el listado
        listAreaAddedManual.set(listAreaAddedManual.size() - 1, aux);

    }

    public void saveSelectedGeofencesArea(String idArea){
        this.idAreaActiva = idArea;
        activity.updateCircleColorGraphic(idAreaActiva);
        //obtengo el ultimo item agregado al listado
        com.example.comunicationwearmobile.models.maps.Geofence aux = listAreaAddedManual.get(listAreaAddedManual.size() - 1);
        //lo actualizo con el valor del id seleccionado
        aux.setIdArea(idAreaActiva);
        //actualizo el item en el listado
        listAreaAddedManual.set(listAreaAddedManual.size() - 1, aux);

        activity.showMessage("Item Seleccionado" + this.idAreaActiva);

    }

    public void clearGeofences() {
        clearGeofenceMaps();
        clearRouteInFile();
        closeFragment();
    }

    public void clearGeofenceMaps() {
        selectPoint = SELECT_ORIGIN_POINT;
        clearGeofencesIntent();
        listAreaAddedManual.clear();
        activity.clearMaps();
    }

    public void updateActiveRoute(Intent intent) {
        waypointsActiveRoute = (List<LatLng>) intent.getSerializableExtra("rutaActiva");

    }

    public void clearActiveRoute() {
        originPoint = null;
        destinationPoint = null;
        selectPoint = SELECT_DESTINATION_POINT;
        waypointsActiveRoute = null;
    }

    public class ReceptorOperation extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {

            int operacion = 0;

            operacion = intent.getExtras().getInt("Operacion");

            if (operacion == Tools.OPERATION_UPDATE_ACTIVE_ROUTE) {
                updateActiveRoute(intent);
            } else if (operacion == Tools.OPERATION_CLEAR_ACTIVE_ROUTE) {
                clearActiveRoute();
            }
        }
    }

    public class ReceptorNotificacion extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {

            int operacion = 0;

            operacion = intent.getExtras().getInt("Operacion");

        }
    }
}
