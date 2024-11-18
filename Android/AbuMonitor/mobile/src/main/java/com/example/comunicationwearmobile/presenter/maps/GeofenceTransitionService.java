package com.example.comunicationwearmobile.presenter.maps;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.comunicationwearmobile.models.maps.SharedPreferencesRoutes;
import com.example.comunicationwearmobile.utils.maps.NotificationHelper;
import com.example.comunicationwearmobile.utils.maps.Tools;
import com.example.comunicationwearmobile.ui.Activities.MapsActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class GeofenceTransitionService extends IntentService {
    private static final String TAG = GeofenceTransitionService.class.getSimpleName();
    private static final int OFFSET_ARRAY = 1;
    private static Boolean controlingRoute=false;
    private static String  idActiveOrigin=null;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public GeofenceTransitionService() {
        super(TAG);

    }

    @Override
    public void onCreate(){
        super.onCreate();

    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        int operation= 0;
        if (intent != null) {
            operation = intent.getIntExtra("Operation",-1);
        }

        //determino si llamo al intent services desde el servicio de google maps, usando un pending intent
        //(GEOFENCE_TRANSITION) o si se llamo directamente a traves de start service(GEOFENCE_ROUTE).
        //
        //O sea si se llamo a intent service por haber entrado o salido de una area de geofencig, o si
        // en otro caso se llamo intent service porque la persona se salio de la ruta calculada.
        switch(operation){
            case(Tools.GEOFENCE_TRANSITION):
                operationTransition(intent);
                break;
            case(Tools.GEOFENCE_ROUTE):
                operationRoute();
                break;
            default:
                Log.e(TAG,"Error en on HandleIntent");
        }




    }

    private void operationRoute(){
        NotificationHelper notificationHelper;
        String msg;

        msg="Persona fuera de ruta";

        // Retrieve GeofenceTrasition
         notificationHelper = NotificationHelper.getInstance(getApplicationContext());
        // Send notification details as a String
        notificationHelper.sendHighPriorityNotification("Evento detectado", msg, MapsActivity.class);



    }

    private void operationTransition(Intent intent){
        int geoFenceTransition;
        NotificationHelper notificationHelper;
        GeofencingEvent geofencingEvent;
        String msg;

        geofencingEvent = GeofencingEvent.fromIntent(intent);

        // Handling errors
        if (Objects.requireNonNull(geofencingEvent).hasError()) {
            msg = getErrorString(geofencingEvent.getErrorCode());
            Log.e(TAG, msg);
            return;
        }


        geoFenceTransition = geofencingEvent.getGeofenceTransition();

        //se obtienen las areas que de geofecnes que se detectaron en ese momento
        List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

        //Si bien varios areas de geofences pueden detectarse al mismo tiempo
        //solamente se va a utilizar una sola para determinar el camino activo.
        msg = getMessageEvent(geoFenceTransition, triggeringGeofences.get(0).getRequestId());

        //establezco la ruta activa. Leyendo los waypoints del sharedpreference
        determineRouteActive(geoFenceTransition, triggeringGeofences.get(0).getRequestId());

        // Retrieve GeofenceTrasition
        notificationHelper = NotificationHelper.getInstance(getApplicationContext());
        // Send notification details as a String
        notificationHelper.sendHighPriorityNotification("Evento detectado", msg, MapsActivity.class);



    }

    private void determineRouteActive(int geoFenceTransition, String idArea){

        if(!idArea.contains(Tools.WORD_INITIAL))
            return;

        if (geoFenceTransition!=Geofence.GEOFENCE_TRANSITION_ENTER){
            return;
        }

        determineStatusRoute(idArea);
    }

    private void determineStatusRoute(String idActual){
        List <LatLng> listRoute;

        //me fijo si es un geofence origen que esta guardado en el shared preference
        listRoute=SharedPreferencesRoutes.getStringArrayPref(getApplicationContext(),idActual);

        //Si me la encontro dentro del archivo entonces quiere decir que es la nueva ruta activa.
        if(!listRoute.isEmpty()) {
            setActiveRoute(idActual,listRoute);
        }else{
            clearActiveRoute(idActual);
        }

    }

    private void setActiveRoute(String idActual, List<LatLng> listRoute){
        Log.d(TAG,"Ruta activa");

        Intent i = new Intent("com.example.intentservice.intent.action.RESPUESTA_OPERACION" );
        i.putExtra("Operacion",Tools.OPERATION_UPDATE_ACTIVE_ROUTE);
        i.putExtra("rutaActiva", (Serializable) listRoute);

        //almaceno el id de la ruta Activa
        idActiveOrigin=idActual;
        controlingRoute=true;

        //Se envian la ruta nueva ruta activa al bradcast reciever de la activity principal
        sendBroadcast(i);



    }
    private int getPosNumberId(String id){
        int index;
        char auxChar;
        boolean flag=false;

        index=id.length()-OFFSET_ARRAY;
        do{
            auxChar=id.charAt(index) ;
            if(Character.isDigit(auxChar)) {
                index--;
            }else{
                flag=true;
            }

        }while (flag!=true);

        return index+OFFSET_ARRAY;

    }

    private void  clearActiveRoute(String idActual){
        String idActiveDestination;

        Intent i;


        if(controlingRoute==false)
            return;


        //calculo cual deberia ser el id del gefecence Destino.
        //Este deberia ser Origen+1
        idActiveDestination= prepareNextId();
        //idActiveDestination= prepareNextId(idActiveArea);

        if(idActiveDestination==null)
            return;

        //Si el id del Geofence actual es igual al que se estimo,
        //entonces estoy en el final de la ruta.
        if(idActual.equals(idActiveDestination)) {
            controlingRoute=false;

            //Se envian la ruta nueva ruta activa al bradcast reciever de la activity principal
            i = new Intent("com.example.intentservice.intent.action.RESPUESTA_OPERACION");
            i.putExtra("Operacion", Tools.OPERATION_CLEAR_ACTIVE_ROUTE);
            sendBroadcast(i);
        }
    }

    private String prepareNextId(){
        int nextId,posNumberId;
        String idActiveDestination;


        posNumberId=getPosNumberId(idActiveOrigin);

        if(posNumberId==-1){
            Log.e(TAG,"Error en getPosNumberID()");
            return null;
        }


        nextId= Integer.parseInt(idActiveOrigin.substring(posNumberId));
        nextId++;


        idActiveDestination=idActiveOrigin.substring(0,posNumberId)+nextId;

        return idActiveDestination;
    }

    private String getMessageEvent(int geoFenceTransition, String idArea){
        String msg=null;

        switch(geoFenceTransition){
            case (Geofence.GEOFENCE_TRANSITION_ENTER):
                msg="Entrando en"+ idArea;
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                msg="Saliendo de"+ idArea;
                break;
            default:
                Log.e(TAG,"error en getMessageEvent");
        }

        return msg;
    }

    // Handle errors
    private static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "GeoFence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many GeoFences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error.";
        }
    }


}


