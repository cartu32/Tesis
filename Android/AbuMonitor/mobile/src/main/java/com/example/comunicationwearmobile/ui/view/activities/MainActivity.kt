package com.example.comunicationwearmobile.ui.view.activities

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.ui.viewmodel.ViewModelFactory
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.utils.services.GeofencesServices
import com.example.comunicationwearmobile.ui.viewmodel.ViewmodelMainActivity


class MainActivity : AppCompatActivity() {

    //atributos asociados al viewmodel
    private lateinit var viewmodelMainActivity: ViewmodelMainActivity
    private lateinit var factory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        configureInsets()
        initializeViewModel()
        initializeComponentsView()
        observeLiveData()
        checkPermissions()

        // Verificar los permisos al iniciar
        viewmodelMainActivity.checkPermissions()

    }

    private fun startGeofenceService() {
        val serviceIntent = Intent(this, GeofencesServices::class.java)
        startService(serviceIntent)

        Toast.makeText(this,"Geofenceservices esta ejecutandose en primer plano",Toast.LENGTH_LONG)
            .show()
    }

    private fun configureInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initializeViewModel() {
        factory = Definition.factory
        viewmodelMainActivity = ViewModelProvider(this, factory)[ViewmodelMainActivity::class.java]
    }
    private fun initializeComponentsView() {
        //Esta es otra forma de asociar los listeners de los botones,
        //sin necesidad de crear objetos botones
        val buttons = mapOf(
            R.id.cmdDefineAreas to ::openMapsActivity,
            R.id.cmdDefineRoutes to ::showUnderConstruction,
            R.id.cmdDefineReminder to ::showUnderConstruction,
            R.id.cmdDefineContacts to ::showUnderConstruction
        )

        buttons.forEach { (id, action) ->
            findViewById<Button>(id).setOnClickListener { action() }
        }
    }
    private fun observeLiveData() {
        viewmodelMainActivity.permissionsToRequest.observe(this) { permissions ->
            if (permissions.isNotEmpty()) {
                requestPermissionsLauncher.launch(permissions.toTypedArray())
            }
        }

        viewmodelMainActivity.backgroundPermissionRequired.observe(this) { isRequired ->
            if (isRequired) askPermissionForBackgroundUsage()
        }

        viewmodelMainActivity.allPermissionGranted.observe(this) { isRequired ->
            //Si se otorgaron todos los permisos, entonces se inicia el service
            startGeofenceService()
        }
    }

    private fun checkPermissions() {
        viewmodelMainActivity.checkPermissions()
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            Log.d(Definition.TAG_DEBUG, "Todos los permisos generales fueron aceptados")
            askPermissionForBackgroundUsage()
        } else {
            Toast.makeText(this, "Permisos denegados: ${permissions.filter { !it.value }.keys}", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestBackgroundPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            //Si se otorgaron todos los permisos, entonces se inicia el service
            startGeofenceService()
        }else {
            val message = "ACCESS_BACKGROUND_LOCATION denegado"
            Toast.makeText(this , message , Toast.LENGTH_SHORT).show()
        }
    }

    private fun askPermissionForBackgroundUsage() {
        AlertDialog.Builder(this)
            .setTitle("Permiso Necesario!")
            .setMessage("¡Se necesita permiso de ubicación en segundo plano!. Por favor toca \"Permitir todo el tiempo\" en la siguiente pantalla")
            .setPositiveButton("OK") { _, _ -> requestBackgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION) }
            .setNegativeButton("CANCEL", null)
            .create().show()
    }

    private fun openMapsActivity() {
        startActivity(Intent(this, MapsActivity::class.java))
    }

    private fun showUnderConstruction() {
        Toast.makeText(this, "En construcción", Toast.LENGTH_SHORT).show()
    }
}

