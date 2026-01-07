package com.example.comunicationwearmobile.ui.view.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.ui.viewmodel.GenericViewModelFactory
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.common.SharedVariables
import com.example.comunicationwearmobile.ui.utils.Helpers.Notification.NotificationHelper
import com.example.comunicationwearmobile.ui.utils.services.GeofencesServices
import com.example.comunicationwearmobile.ui.view.activities.areas_geofence.MapsDefineAreasActivity
import com.example.comunicationwearmobile.ui.view.activities.calendar_assistance.AssistanceCalendarActivity
import com.example.comunicationwearmobile.ui.view.activities.contact.ContactsActivity
import com.example.comunicationwearmobile.ui.view.activities.elderly_track.MapsElderlyTrackActivity
import com.example.comunicationwearmobile.ui.view.activities.menu_option.ConfigActivity
import com.example.comunicationwearmobile.ui.view.fragment.LoginDialogFragmentDialogFragment
import com.example.comunicationwearmobile.ui.viewmodel.ViewmodelMainActivity


class MainMenuActivity : AppCompatActivity(),LoginDialogFragmentDialogFragment.LoginListener {


    private val cmdDefineAreas by lazy { findViewById<Button>(R.id.cmdDefineAreas) }
    private val cmdViewAreas by lazy { findViewById<Button>(R.id.cmdViewAreas) }
    private val cmdViewAppointment by lazy { findViewById<Button>(R.id.cmdViewAppoitnment) }
    private val cmdViewContacts by lazy { findViewById<Button>(R.id.cmdViewContacts) }
    private var itemAdmin:MenuItem?=null
    private var itemConfig:MenuItem?=null

    //atributos asociados al viewmodel
    private var viewmodelMainActivity: ViewmodelMainActivity?=null
    private var notificationManagerHelper: NotificationHelper?= null

    private lateinit var backPressedCallback: OnBackPressedCallback


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main_menu)
        configureInsets()
        configCallbackBackPressed()
        initComponent()

        Log.d(Definition.TAG_DEBUG,"OnCreate MainMenuActivity")
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
        Log.d(Definition.TAG_DEBUG,"Onstart MainMenuActivity")
    }
    private fun initComponent(){
        initStrictMode()
        initNotificationManager()
        initializeViewModel()
        initializeComponentsView()
        observeLiveData()
        checkPermissions()
        checkGooglePlayServices()

    }

    private fun checkGooglePlayServices() {
        val result=viewmodelMainActivity?.checkWearableApiAvailable(this)

        when(result) {
            Definition.ERROR_PLAY_SERVICES_MISSING_OR_OUTDATED -> {
                Toast.makeText(this, "Error Google Play Services no está disponible o actualizado.", Toast.LENGTH_LONG).show()
            }
            Definition.ERROR_API_UNAVAILABLE->{
                showInstallWearOsDialog(this)
            }
            else->{
                Log.d(Definition.TAG_DEBUG,"Google Play Services disponible")
            }
        }
    }
    private fun initNotificationManager() {
        //se inicializa el notification manager helper
        notificationManagerHelper= NotificationHelper.getInstance(this.applicationContext)
        notificationManagerHelper?.initConfiguration()
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
        Log.d(Definition.TAG_DEBUG, "Inicializa ViewModels")

        // Crea el factory para ViewModelMainActivity
        val factoryMainActivity = GenericViewModelFactory {
            ViewmodelMainActivity(application)
        }
        viewmodelMainActivity = ViewModelProvider(this, factoryMainActivity)[ViewmodelMainActivity::class.java]

    }

    private fun initializeComponentsView() {

        cmdDefineAreas.setOnClickListener {openMapsActivity()}
        cmdViewAreas.setOnClickListener {openMapsElderlyTrackActivity()}
        cmdViewAppointment.setOnClickListener {openCalendarAssistance()}
        cmdViewContacts.setOnClickListener {showContacts()}


        cmdDefineAreas.isVisible=false

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

        viewmodelMainActivity?.requestExactAlarmPermission?.observe(this){request->
            if(request==false)
                requestAlarmPermission()
            else
                Log.d(Definition.TAG_DEBUG,"Se tienen permisos de la alarma")
        }

        viewmodelMainActivity?.allPermissionGranted?.observe(this) { isRequired ->
            //Si se otorgaron todos los permisos, entonces se inicia el service
            startGeofenceService()
        }
        Log.d(Definition.TAG_DEBUG,"Inicializa Obersever")
    }

    private fun requestAlarmPermission() {
        AlertDialog.Builder(this)
            .setTitle("Permiso necesario")
            .setMessage("La app debe programar alarmas a hora exacta para tus citas.")
            .setPositiveButton("Configurar") { _, _ ->
                startActivity(
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:$packageName")
                    }
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
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

    private fun openCalendarAssistance(){
        startActivity(Intent(this , AssistanceCalendarActivity::class.java))
    }
    private fun showContacts() {
        startActivity(Intent(this , ContactsActivity::class.java))

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_option, menu)

        itemAdmin   = menu?.findItem(R.id.opt_cambiar_modo_usuario)
        itemConfig  = menu?.findItem(R.id.opt_configuracion)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.opt_cambiar_modo_usuario -> {
                if(SharedVariables.user!=SharedVariables.USER_ADMIN)
                    {
                        LoginDialogFragmentDialogFragment().show(
                            supportFragmentManager,
                            "loginDialog"
                        )
                        setVisibleComponents(SharedVariables.user)
                        true
                    }else{
                        SharedVariables.user=""
                        Toast.makeText(this,"Cambiando a  Modo Usuario",Toast.LENGTH_SHORT).show()
                        setVisibleComponents(SharedVariables.user)
                        true
                }
            }
            R.id.opt_configuracion -> {
                val intent=Intent(this, ConfigActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onLogin(username: String, password: String) {
        //guardo el usuario en la variable global
        SharedVariables.user=username
        //seteo el usuaerio en la vista
        setVisibleComponents(username)
    }

    private fun openPlayStoreForApp(context: Context, packageName: String) {
        val appUri = Uri.parse("market://details?id=$packageName")
        val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")

        val playIntent = Intent(Intent.ACTION_VIEW, appUri)
        try {
            context.startActivity(playIntent)
        } catch (_: Exception) {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
            } catch (_: Exception) {
                Toast.makeText(context, "No se pudo abrir Play Store ni el navegador.", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun showInstallWearOsDialog(activity: androidx.fragment.app.FragmentActivity) {
        AlertDialog.Builder(activity)
            .setTitle("Instalar Wear OS")
            .setMessage("Es necesario instalar la app Wear OS para poder comunicarse con el reloj.\n\nSe abrirá Google Play para instalarla. ¿Querés continuar?")
            .setPositiveButton("Abrir Play") { _, _ ->
                openPlayStoreForApp(activity, "com.google.android.wearable.app")
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    private fun setVisibleComponents(username: String) {

        if(username==SharedVariables.USER_ADMIN){

            cmdDefineAreas.isVisible=true
            cmdViewContacts.text= getString(R.string.cmd_define_contacts)
            cmdViewAppointment.text= getString(R.string.cmd_define_assistance)
            cmdViewAreas.text= getString(R.string.cmd_view_areas_geofences)
            itemAdmin?.title=getString(R.string.item_exit_mode_admin)
            itemConfig?.isVisible=true

            Toast.makeText(this,"Cambiando a  Modo Administrador",Toast.LENGTH_SHORT).show()
        }else{
            cmdDefineAreas.isVisible=false
            cmdViewContacts.text= getString(R.string.cmd_view_contacts)
            cmdViewAppointment.text= getString(R.string.cmd_view_assistance)
            itemAdmin?.title=getString(R.string.item_enter_mode_admin)
            itemConfig?.isVisible=false

        }

    }

    private fun freeListeners() {
        cmdDefineAreas.setOnClickListener(null)
        cmdViewContacts.setOnClickListener(null)
        cmdViewAreas.setOnClickListener(null)
        cmdViewAppointment.setOnClickListener(null)
    }

    private fun freeViewmodel(){
        // Desvincular observadores de LiveData para evitar fugas de memoria
        viewmodelMainActivity?.permissionsToRequest?.removeObservers(this)
        viewmodelMainActivity?.backgroundPermissionRequired?.removeObservers(this)
        viewmodelMainActivity?.allPermissionGranted?.removeObservers(this)
        viewmodelMainActivity?.requestExactAlarmPermission?.removeObservers(this)
        // Notificar al ViewModel que la actividad se destruyó
        viewmodelMainActivity?.onDestroyed()
        viewmodelMainActivity=null

    }

    private fun freeComponent(){
        //se libera los listeners de los botones
        freeListeners()
        freeViewmodel()

        notificationManagerHelper=null
    }

    override fun onStop() {
        super.onStop()
        Log.d(Definition.TAG_DEBUG,"onStop MainMenuActivity")

    }

    override fun onDestroy() {
        super.onDestroy()
        freeComponent()
        //remuevo el callback del boton back de Android
        if (::backPressedCallback.isInitialized) {
            backPressedCallback.remove() // Libera el callback
        }
        notificationManagerHelper?.cancelCorutineInit()
        Log.d(Definition.TAG_DEBUG, "onDestroy MainMenuActivity")
        //System.exit(0)
    }


}


