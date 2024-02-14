package com.example.geofencing.Model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.geofencing.Util.Tools;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class SharedPreferencesRoutes {

    public static synchronized void setStringArrayPref(Context context, String key, List<LatLng> values) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray a = new JSONArray();


        for (int i = 0; i < values.size(); i++) {
            a.put(values.get(i));
        }
         if (!values.isEmpty()) {
            editor.putString(key, a.toString());
        } else {
            editor.putString(key, null);
        }
        editor.apply();
    }

    public static synchronized List<LatLng> getStringArrayPref(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(key, null);
        LatLng latLng;
        List<LatLng> listRoute=new ArrayList<LatLng>();

        if (json != null) {
            try {

                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++) {
                    String  url =  a.optString(i);
                    latLng= Tools.convertStringToLatLng(url);
                    listRoute.add(latLng);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return listRoute;
    }

    public static synchronized void clearSharedPreferences(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        editor.clear();
        editor.apply();

    }
}


