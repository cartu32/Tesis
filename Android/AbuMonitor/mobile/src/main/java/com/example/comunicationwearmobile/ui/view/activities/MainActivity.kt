package com.example.comunicationwearmobile.ui.view.activities

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.ui.viewmodel.GenericViewModelFactory
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.utils.Mannager.LocationManagerHelper
import com.example.comunicationwearmobile.ui.utils.Tools
import com.example.comunicationwearmobile.ui.utils.services.GeofencesServices
import com.example.comunicationwearmobile.ui.viewmodel.ViewmodelMainActivity


class MainActivity : AppCompatActivity() {

    //atributos asociados al viewmodel
    private var viewmodelMainActivity: ViewmodelMainActivity?=null
    private lateinit var backPressedCallback: OnBackPressedCallback
    private var locationManagerHelper: LocationManagerHelper?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        configureInsets()
        configCallbackBackPressed()
        configLocatioManager()
        initComponent()



        Log.d(Definition.TAG_DEBUG,"OnCreate MainActivity")
    }

    private fun configLocatioManager() {
        locationManagerHelper=LocationManagerHelper(this)
        locationManagerHelper?.configCheckStatusGps()

    }

    private fun configCallbackBackPressed() {
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //Log.d("ABUMONITOR_DEBUG", "Back button pressed - Finishing Activity")
                finish() // Cierra la actividad
            }
        }

        onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    override fun onStart() {
        super.onStart()
        Log.d(Definition.TAG_DEBUG,"Onstart MainActivity")
    }
    private fun initComponent(){
        initStrictMode()
        initializeViewModel()
        initializeComponentsView()
        observeLiveData()
        checkPermissions()
        locationManagerHelper?.checkLocationSettings(this)
    }

    private fun initStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build()
        )
    }

    private fun startGeofenceService() {
        val serviceIntent = Intent(this , GeofencesServices::class.java)
        startForegroundService(serviceIntent)

        Toast.makeText(
            this ,
            "Geofenceservices esta ejecutandose en primer plano" ,
            Toast.LENGTH_LONG
        )
            .show()
    }

    private fun configureInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left , systemBars.top , systemBars.right , systemBars.bottom)
            insets
        }
    }

    private fun initializeViewModel() {
        // Obtén una instancia del ViewModel usando el GenericViewModelFactory
        val factory = GenericViewModelFactory {
            ViewmodelMainActivity(application)
        }
        Log.d(Definition.TAG_DEBUG,"Inicializa ViewModel")
        viewmodelMainActivity = ViewModelProvider(this, factory)[ViewmodelMainActivity::class.java]
    }



    private fun initializeComponentsView() {
        //Esta es otra forma de asociar los listeners de los botones,
        //sin necesidad de crear objetos botones
        val buttons = mapOf(
            R.id.cmdDefineAreas to ::openMapsActivity ,
            R.id.cmdDefineRoutes to ::openMapsElderlyTrackActivity ,
            R.id.cmdDefineReminder to ::showUnderConstruction ,
            R.id.cmdDefineContacts to ::showUnderConstruction
        )

        buttons.forEach { (id , action) ->
            findViewById<Button>(id).setOnClickListener { action() }
        }

        Log.d(Definition.TAG_DEBUG,"Inicializa Componentes View")
    }

    private fun observeLiveData() {
        viewmodelMainActivity?.permissionsToRequest?.observe(this) { permissions ->
            if (permissions?.isNotEmpty() == true) {
                requestPermissionsLauncher.launch(permissions.toTypedArray())
            }
        }

        viewmodelMainActivity?.backgroundPermissionRequired?.observe(this) { isRequired ->
            if (isRequired == true) askPermissionForBackgroundUsage()
        }

        viewmodelMainActivity?.allPermissionGranted?.observe(this) { isRequired ->
            //Si se otorgaron todos los permisos, entonces se inicia el service
            startGeofenceService()
        }
        Log.d(Definition.TAG_DEBUG,"Inicializa Obersever")
    }

    private fun checkPermissions() {
        viewmodelMainActivity?.checkPermissions()
        Log.d(Definition.TAG_DEBUG,"Inicializa checkPermissions")

    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            Log.d(Definition.TAG_DEBUG , "Todos los permisos generales fueron aceptados")
            askPermissionForBackgroundUsage()
        } else {
            Toast.makeText(
                this ,
                "Permisos denegados: ${permissions.filter { !it.value }.keys}" ,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val requestBackgroundPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            //Si se otorgaron todos los permisos, entonces se inicia el service
            startGeofenceService()
        } else {
            val message = "ACCESS_BACKGROUND_LOCATION denegado"
            Toast.makeText(this , message , Toast.LENGTH_SHORT).show()
        }
    }

    private fun askPermissionForBackgroundUsage() {
        AlertDialog.Builder(this)
            .setTitle("Permiso Necesario!")
            .setMessage("¡Se necesita permiso de ubicación en segundo plano!. Por favor toca \"Permitir todo el tiempo\" en la siguiente pantalla")
            .setPositiveButton("OK") { _ , _ -> requestBackgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION) }
            .setNegativeButton("CANCEL" , null)
            .create().show()
    }

    private fun openMapsActivity() {
        startActivity(Intent(this , MapsDefineAreasActivity::class.java))
    }

    private fun openMapsElderlyTrackActivity() {
        startActivity(Intent(this , MapsElderlyTrackActivity::class.java))
    }
    private fun showUnderConstruction() {
        Toast.makeText(this , "En construcción" , Toast.LENGTH_SHORT).show()
    }


    private fun freeListeners() {
        val buttons = listOf(
            R.id.cmdDefineAreas,
            R.id.cmdDefineRoutes,
            R.id.cmdDefineReminder,
            R.id.cmdDefineContacts
        )

        buttons.forEach { id ->
            findViewById<Button>(id).setOnClickListener(null)
        }
    }

    private fun freeViewmodel(){
        // Desvincular observadores de LiveData para evitar fugas de memoria
        viewmodelMainActivity?.permissionsToRequest?.removeObservers(this)
        viewmodelMainActivity?.backgroundPermissionRequired?.removeObservers(this)
        viewmodelMainActivity?.allPermissionGranted?.removeObservers(this)

        // Notificar al ViewModel que la actividad se destruyó
        viewmodelMainActivity?.onDestroyed()
        viewmodelMainActivity=null

    }

    private fun freeComponent(){
        //se libera los listeners de los botones
        freeListeners()
        freeViewmodel()
        locationManagerHelper=null
    }

    override fun onStop() {
        super.onStop()
        Log.d(Definition.TAG_DEBUG,"onStop MainActivity")

    }

    override fun onDestroy() {
        super.onDestroy()
        freeComponent()
        //remuevo el callback del boton back de Android
        if (::backPressedCallback.isInitialized) {
            backPressedCallback.remove() // Libera el callback
        }
        Log.d(Definition.TAG_DEBUG, "onDestroy MainActivity")
        //System.exit(0)
    }

}


