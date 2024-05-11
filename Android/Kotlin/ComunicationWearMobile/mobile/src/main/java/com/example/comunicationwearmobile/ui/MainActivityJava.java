package com.example.comunicationwearmobile.ui;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.comunicationwearmobile.R;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class MainActivityJava {
    private Activity activity;
    private Button cmdSendWear;
    private TextView txtMsgWear;
    public MainActivityJava(Activity activity){

        this.activity = activity;


        cmdSendWear= (Button) this.activity.findViewById(R.id.cmdSendWear);
        txtMsgWear = (TextView) this.activity.findViewById(R.id.txtMsgWear);

        cmdSendWear.setOnClickListener(listenerButton);
        }

    private View.OnClickListener listenerButton = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity.getApplicationContext(), "Hola", Toast.LENGTH_SHORT).show();
                enviarDatosAlReloj();
            }
    };

    public void enviarDatosAlReloj() {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/data_path");
        putDataMapReq.getDataMap().putString("key", "valor");
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        Wearable.getDataClient(activity.getApplicationContext()).putDataItem(putDataReq);
    }
 }


