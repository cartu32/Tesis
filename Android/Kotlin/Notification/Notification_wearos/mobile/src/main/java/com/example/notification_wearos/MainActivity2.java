package com.example.notification_wearos;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity2 extends AppCompatActivity {

    Button cmdBoton=null;
    Integer numberMsj=0;

    Notification notifica =null;
    NotificationCustom notificaCustom =null;
    private Integer notificationId=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);

        cmdBoton=(Button) findViewById(R.id.btnMostrarNotificacion);

        cmdBoton.setOnClickListener(botonesListeners);
        notifica = new Notification(this);
        notificaCustom = new NotificationCustom(this);
    }

    //Metodo que actua como Listener de los eventos que ocurren en los componentes graficos de la activty
    private View.OnClickListener botonesListeners = new View.OnClickListener()
    {


        public void onClick(View v)
        {
            numberMsj++;
            Toast.makeText(getApplicationContext(),"Error en Listener de botones",Toast.LENGTH_LONG).show();
            //showNotification(getApplicationContext(),"Alerta Notificacion", "mensaje notificacion");
            //notifica.showNotification("Notificacion","Cuerpo notificacion"+numberMsj.toString());

            notificaCustom.showNotification("Notificacion","Cuerpo notificacion"+numberMsj.toString());



        }
    };
}