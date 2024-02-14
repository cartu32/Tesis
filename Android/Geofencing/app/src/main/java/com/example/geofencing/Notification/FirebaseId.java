package com.example.geofencing.Notification;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class FirebaseId {

    static final String TAG ="Firebase Token";

    public  static void logToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        Log.d(TAG, "Token: "+token);
                        //showFirebaseToken(token);
                    }
                });
    }

    /*public void showFirebaseToken(String token){
        Log.d(TAG, token);
        Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();

    }*/

}
