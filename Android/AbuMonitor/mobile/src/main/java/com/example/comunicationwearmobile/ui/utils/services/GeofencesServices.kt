package com.example.comunicationwearmobile.ui.utils.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.utils.Mannager.NotificationManagerHelper
import com.example.comunicationwearmobile.ui.view.activities.EnableGpsDialog
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class GeofencesServices: Service() {

    // Canal que se utiliza para encolar las peticiones realizadas cada vez que
    // se ejecuta stratservice
    private val requestChannel = Channel<Intent>(Channel.UNLIMITED)
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var notificationManagerHelper:NotificationManagerHelper?= null

    override fun onBind(intent: Intent?): IBinder? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var settingsClient: SettingsClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationSettingsRequest: LocationSettingsRequest

    override fun onCreate() {
        super.onCreate()

        notificationManagerHelper=NotificationManagerHelper.getInstance(applicationContext)
        notificationManagerHelper?.createChannelForegroundServices()
        val notification = notificationManagerHelper?.createNotificationForegroundService()

        configCheckStatusGps()
        startForeground(Definition.FIRST_NOTIFICATION_ID, notification)
        // Lector del Channel: consume las solicitudes encoladas
        serviceScope.launch {
            for (intent in requestChannel) {
                try {
                    handleIntent(intent) // Procesa cada intent
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun configCheckStatusGps() {
        // Inicializar clientes
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        settingsClient = LocationServices.getSettingsClient(this)

        // Crear una solicitud de ubicación
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).build()

        // Crear configuración de ajustes
        locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

    }

    private fun checkLocationSettings() {
        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                // El GPS está activado
                Log.d("LocationService", "GPS está activado")
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    // El GPS no está activado, pedir al usuario que lo active
                    val intent = Intent(this, EnableGpsDialog::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK )
                    intent.putExtra("resolution", exception.resolution);
                    startActivity(intent)
                } else {
                    Log.e("LocationService", "No se puede resolver: ${exception.message}")
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(Service.STOP_FOREGROUND_REMOVE)
        serviceScope.cancel() // Cancela la corutina cuando el servicio se destruye

        Log.d(Definition.TAG_DEBUG," GeofenceService Destruido")
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Encola la solicitud en el Channel
        intent?.let {
            // Comprobar el estado del GPS
            checkLocationSettings()

            requestChannel.trySend(it)
        }

        return START_STICKY
    }




    private suspend fun handleIntent(intent: Intent?)  {
        var operation = 0
        if (intent != null) {
            operation = intent.getIntExtra("Operation" , -1)
        }

        when (operation) {
            //Tools.GEOFENCE_TRANSITION -> operationTransition(intent)
            //Tools.GEOFENCE_ROUTE -> operationRoute()
            else ->
                Log.e(Definition.TAG_DEBUG , "Error en on HandleIntent")
        }
    }



}