package com.example.comunicationwearmobile.ui.ui.view.activities

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.ui.viewmodel.ViewModelFactory
import com.example.abumonitor.ui.viewmodel.ViewmodelAreaGeofence
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.ui.viewmodel.ViewmodelMainActivity
import java.sql.Time

class MainActivity : AppCompatActivity() {
    //atributos asociados a las componentes graficos(Frontend)
    private lateinit var cmdDefineAreas: Button
    private lateinit var cmdDefineReminders: Button
    private lateinit var cmdDefineRoutes: Button
    private lateinit var cmdDefineContacts: Button

    //atributos asociados al viewmodel
    private lateinit var viewmodelAreaGeofence: ViewmodelAreaGeofence
    private lateinit var viewmodelMainActivity: ViewmodelMainActivity
    private lateinit var factory: ViewModelFactory

    val permissonNecesary = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_PHONE_STATE
    )

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        // Maneja los resultados de los permisos aquí
        result.forEach { (permisson, granted) ->
            if (granted) {
                //Permiso concedido
                Toast.makeText(this,"Permiso concedido${permisson}",Toast.LENGTH_SHORT).show()
            } else {
                // Permiso denegado
                Toast.makeText(this,"Permiso no concedido${permisson}",Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configView()
        configObserverLivedata()

        viewmodelMainActivity.verifyPermisson(this,permissonNecesary)
     }

    private fun configObserverLivedata() {
        //asoscio el viewmodelAreaGeofence usando el factory a la view del MainActivity
        factory=Definition.factory
        //viewmodelAreaGeofence=ViewModelProvider(this,factory)[ViewmodelAreaGeofence::class.java]
        viewmodelMainActivity=ViewModelProvider(this,factory)[ViewmodelMainActivity::class.java]

        configObserverRequestPermisson()

    }

    private fun configObserverRequestPermisson() {
        viewmodelMainActivity.permissionRequest.observe(this){permissoNotGranted ->
            requestPermissionLauncher.launch(permissoNotGranted.toTypedArray())
        }
    }


    private fun configView(){
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left , systemBars.top , systemBars.right , systemBars.bottom)
            insets
        }
        cmdDefineAreas     = findViewById(R.id.cmdDefineAreas)
        cmdDefineRoutes    = findViewById(R.id.cmdDefineRoutes)
        cmdDefineReminders = findViewById(R.id.cmdDefineReminder)
        cmdDefineContacts  = findViewById(R.id.cmdDefineContacts)

        cmdDefineAreas.setOnClickListener(listenerButton)
        cmdDefineReminders.setOnClickListener(listenerButton)
        cmdDefineContacts.setOnClickListener(listenerButton)
        cmdDefineRoutes.setOnClickListener(listenerButton)

    }

    // Crear un listener compartido
    val listenerButton = View.OnClickListener { view ->
        //val idAreaDelete = 1
        when (view.id) {
            R.id.cmdDefineAreas -> {
                // Acción para el botón cmdSendAlert
                val intent = Intent(this , MapsActivity::class.java)
                startActivity(intent)
            }

            R.id.cmdDefineRoutes -> {
                // Acción para el botón cmdSendAlert
                /* val intent = Intent(this , MapsActivity::class.java)
                 startActivity(
                     intent
                 )*/
            }


            R.id.cmdDefineReminder -> {
                // Acción para el botón cmdSmSConfig
                /*      val intent = Intent(this , SmsConfigActivity::class.java)
                      startActivity(
                          intent
                      )*/
            }

        }

    }
}