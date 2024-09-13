package com.example.geofencing.Notification;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.geofencing.Model.Geofence;
import com.example.geofencing.Model.Route;
import com.example.geofencing.R;
import com.example.geofencing.Util.Tools;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PushNotificationService extends FirebaseMessagingService {
    private static final String TAG = "Firebase";
    private static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    private final static String default_notification_channel_id = "default" ;
    private String GROUP_KEY_GEOFENCING = "com.example.geofencing";
    private Boolean notificationReceived=false;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        JSONObject json;
        List<Geofence> listNewAreas=null;
        List<Route> listNewRoutes=null;

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());


        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            showToast(remoteMessage.getData().toString());

            json =getJson(remoteMessage.getData().toString());


            listNewAreas=convertJsontoArrayAreas(json);
            listNewRoutes=convertJsontoArrayRoutes(json);

            if((listNewAreas.isEmpty()==false)&&(listNewRoutes.isEmpty()==false)){
                notifyMapresenteNewAreas(listNewAreas,listNewRoutes);
            }else{
                Log.e(TAG,"Error en send notification");
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            //Log.d(TAG, "Message Notification Title: " + remoteMessage.getNotification().getTitle());
            //Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }



    private JSONObject getJson(String msg){
        JSONObject json = null;
        try {
            json = new JSONObject(msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
    private void showToast(String msg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showNotification(){
        Uri sound = Uri. parse (ContentResolver. SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/raw/quite_impressed.mp3" ) ;

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), default_notification_channel_id )
                .setSmallIcon(R.drawable. ic_launcher_foreground )
                .setContentTitle( "Actualización de Geofencing" )
                .setSound(sound)
                .setAutoCancel(true)
                .setGroup(GROUP_KEY_GEOFENCING)
                .setGroupSummary(true)
                .setContentText( "Se ha recibido una actualización para el mapa de Geofencing" );

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context. NOTIFICATION_SERVICE ) ;

        if (android.os.Build.VERSION. SDK_INT >= android.os.Build.VERSION_CODES. O ) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes. CONTENT_TYPE_SONIFICATION )
                    .setUsage(AudioAttributes. USAGE_ALARM )
                    .build() ;

            int importance = NotificationManager. IMPORTANCE_HIGH ;
            NotificationChannel notificationChannel = new NotificationChannel( NOTIFICATION_CHANNEL_ID , "NOTIFICATION_CHANNEL_NAME" , importance) ;
            notificationChannel.enableLights( true ) ;
            notificationChannel.setLightColor(Color. RED ) ;

            notificationChannel.enableVibration( true ) ;
            notificationChannel.setVibrationPattern( new long []{ 100 , 200 , 300 , 400 , 500 , 400 , 300 , 200 , 400 }) ;
            notificationChannel.setSound(sound , audioAttributes) ;
            mBuilder.setChannelId( NOTIFICATION_CHANNEL_ID ) ;
            assert mNotificationManager != null;
            mNotificationManager.createNotificationChannel(notificationChannel) ;
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(( int ) System. currentTimeMillis (),  mBuilder.build()) ;
    }
    /**
     * There are two scenarios when onNewToken is called:
     * 1) When a new token is generated on initial app startup
     * 2) Whenever an existing token is changed
     * Under #2, there are three scenarios when the existing token is changed:
     * A) App is restored to a new device
     * B) User uninstalls/reinstalls the app
     * C) User clears app data
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        showToast("token nuevo"+token);

        //sendRegistrationToServer(token);
    }


    public List<Geofence> convertJsontoArrayAreas(JSONObject json){
        JSONArray array;
        List<Geofence> listNewAreas =new ArrayList<Geofence>();
        Geofence area;

        try {
            array = json.getJSONArray("geoareas");

            for (int i=0;i< array.length();i++){
                area = new Geofence();
                area.setIdArea(array.getJSONObject(i).getString("identificador"));
                area.setLatitud(Double.valueOf(array.getJSONObject(i).getString("latitud")));
                area.setLongitud(Double.valueOf(array.getJSONObject(i).getString("longitud")));
                area.setRadius(Float.parseFloat((array.getJSONObject(i).getString("radio"))));
                listNewAreas.add(area);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  listNewAreas;
    }

    public List<Route> convertJsontoArrayRoutes(JSONObject json){
        JSONArray array;
        List<Route> listNewRoutes =new ArrayList<Route>();
        Route route;

        try {
            array = json.getJSONArray("rutas");

            for (int i=0;i< array.length();i++){
                route = new Route();
                route.setIdArea(array.getJSONObject(i).getString("identificador"));
                route.setLatitudeOrigin(Double.valueOf(array.getJSONObject(i).getString("latitud_origen")));
                route.setLongitudeOrigin(Double.valueOf(array.getJSONObject(i).getString("longitud_origen")));
                route.setLatitudeDestination(Double.valueOf(array.getJSONObject(i).getString("latitud_destino")));
                route.setLongitudeDestination(Double.valueOf(array.getJSONObject(i).getString("longitud_destino")));
                route.setTolerance(Integer.parseInt(((array.getJSONObject(i).getString("tolerancia")))));
                listNewRoutes.add(route);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  listNewRoutes;
    }

    public void notifyMapresenteNewAreas(List<Geofence> listAreas,List<Route> listRoute){
        Intent i = new Intent("com.example.intentservice.intent.action.NOTIFICACION_FIREBASE" );
        i.putExtra("Operacion", Tools.OPERATION_NEW_AREAS_FIREBASE);
        i.putExtra("NuevasAreas", (Serializable) listAreas);
        i.putExtra("NuevasRutas", (Serializable) listRoute);

        //Se envian la ruta nueva ruta activa al bradcast reciever de la activity principal
        sendBroadcast(i);
    }


}
