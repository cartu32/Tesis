package com.example.comunicationwearmobile.ui.ui.view.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.ui.viewmodel.ViewModelFactory
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.ui.viewmodel.ViewmodelMainActivity

class MainActivity : AppCompatActivity() {
    //atributos asociados a las componentes graficos(Frontend)
    private lateinit var cmdDefineAreas: Button
    private lateinit var cmdDefineReminders: Button
    private lateinit var cmdDefineRoutes: Button
    private lateinit var cmdDefineContacts: Button

    //atributos asociados al viewmodel
    private lateinit var viewmodelMainActivity: ViewmodelMainActivity
    private lateinit var factory: ViewModelFactory

    //atributos de los permisos de la app
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configView()
        configObserverLivedata()
        configPermisson()

     }

    override fun onStop() {
        super.onStop()
       // viewmodelMainActivity.onDestroyed()
       // Toast.makeText(this,"base de datos cerrada",Toast.LENGTH_SHORT).show()
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


    private fun configObserverLivedata() {
        //asoscio el viewmodelAreaGeofence usando el factory a la view del MainActivity
        factory=Definition.factory
        viewmodelMainActivity=ViewModelProvider(this,factory)[ViewmodelMainActivity::class.java]

        configObserverRequestPermisson()

    }


    private fun configPermisson() {
        // Inicializar el ActivityResultLauncher
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            handlePermissionResult(result)
        }

        //se verifican que la app tenga otorgados los permisos necesarios
        viewmodelMainActivity.verifyPermission(this,Definition.permissonNecesary)
    }

    private fun configObserverRequestPermisson() {
        //si algun permiso no se otorgo se
        viewmodelMainActivity.permissionRequest.observe(this){permissoNotGranted ->
            requestPermissionLauncher.launch(permissoNotGranted.toTypedArray())
        }
    }


    private fun createRequestPermissionLauncher(): ActivityResultLauncher<Array<String>> {
        return registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            handlePermissionResult(result)
        }
    }

    private fun handlePermissionResult(result: Map<String, Boolean>) {
        val deniedPermissions = result.filterValues { !it }.keys
        if (deniedPermissions.isEmpty()) {
            // Todos los permisos fueron concedidos
            Log.d(Definition.TAG_DEBUG, "Todos los permisos fueron concedidos")
        } else {
            // Permisos denegados
            deniedPermissions.forEach { permission ->
                Log.d(Definition.TAG_DEBUG, "Permiso NO concedido: $permission")
            }
            Toast.makeText(this, "Debe conceder todos los permisos para que la app funcione correctamente", Toast.LENGTH_SHORT).show()
        }
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