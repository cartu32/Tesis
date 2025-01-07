package com.example.comunicationwearmobile.ui.view.activities

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.ui.viewmodel.ViewModelFactory
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.viewmodel.ViewmodelMainActivity


class MainActivity : AppCompatActivity() {

    //atributos asociados a las componentes graficos(Frontend)
    private lateinit var cmdDefineAreas: Button
    private lateinit var cmdDefineReminders: Button
    private lateinit var cmdDefineRoutes: Button
    private lateinit var cmdDefineContacts: Button

    //atributos asociados al viewmodel
    private lateinit var viewmodelMainActivity: ViewmodelMainActivity
    private lateinit var factory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configView()
        configObserverLivedata()

        // Verificar los permisos al iniciar
        viewmodelMainActivity.checkPermissions()
    }

    private fun configView() {
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left , systemBars.top , systemBars.right , systemBars.bottom)
            insets
        }
        cmdDefineAreas = findViewById(R.id.cmdDefineAreas)
        cmdDefineRoutes = findViewById(R.id.cmdDefineRoutes)
        cmdDefineReminders = findViewById(R.id.cmdDefineReminder)
        cmdDefineContacts = findViewById(R.id.cmdDefineContacts)

        cmdDefineAreas.setOnClickListener(listenerButton)
        cmdDefineReminders.setOnClickListener(listenerButton)
        cmdDefineContacts.setOnClickListener(listenerButton)
        cmdDefineRoutes.setOnClickListener(listenerButton)

    }


    private fun configObserverLivedata() {
        //asoscio el viewmodelAreaGeofence usando el factory a la view del MainActivity
        factory = Definition.factory
        viewmodelMainActivity = ViewModelProvider(this , factory)[ViewmodelMainActivity::class.java]

        configObserverPermisson()
    }

    private fun configObserverPermisson() {

        // Observar los permisos que deben solicitarse
        viewmodelMainActivity.permissionsToRequest.observe(this) { permissions ->
            if (permissions.isNotEmpty()) {
                requestPermissionsLauncher.launch(permissions.toTypedArray())
            }
        }

        // Observar si se requiere el permiso de fondo
        viewmodelMainActivity.backgroundPermissionRequired.observe(this) { isRequired ->
            if (isRequired) {
                askPermissionForBackgroundUsage()
            }
        }
    }

    private fun requestBackgroundPermisson(){
        requestBackgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        viewmodelMainActivity.onBackgroundPermissionHandled()
    }

    // Lanzador para solicitudes de permisos
    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val deniedPermissions = permissions.filter {
            !it.value
        }
        if (deniedPermissions.isEmpty()) {

            Log.d(Definition.TAG_DEBUG,"Todos los Permisos Generales fueron aceptados")
            //Si estan todos los permisos concecidos, entocnes quiere decir que esta aceptado
            //ACCESS_FINE_LOCATION que es necsario para ACCESS_Background_LOCATION
            askPermissionForBackgroundUsage()
        } else {
            Toast.makeText(this, "Permisos denegados: ${deniedPermissions.keys}", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestBackgroundPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "ACCESS_BACKGROUND_LOCATION concedido", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "ACCESS_BACKGROUND_LOCATION denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun askPermissionForBackgroundUsage() {
            AlertDialog.Builder(this)
                .setTitle("Permiso Necesario!")
                .setMessage("¡Se necesita permiso de ubicación en segundo plano!. Por favor toca  \"Permitir todo el tiempo\" en la siguiente pantalla")
                .setPositiveButton("OK") { dialog , which ->
                    requestBackgroundPermisson()
                }
                .setNegativeButton(
                    "CANCEL"
                ) { dialog , which ->
                    // User declined for Background Location Permission.
                }
                .create().show()
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

