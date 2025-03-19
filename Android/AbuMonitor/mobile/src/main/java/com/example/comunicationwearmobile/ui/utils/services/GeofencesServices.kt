package com.example.comunicationwearmobile.ui.utils.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.model.repository.RepositoryLocation
import com.example.comunicationwearmobile.ui.utils.Mannager.NotificationManagerHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class GeofencesServices: Service() {
    // Canal que se utiliza para encolar las peticiones realizadas cada vez que
    // se ejecuta stratservice
    private var requestChannel: Channel<Intent>? = null
    private var serviceScope:CoroutineScope? = null

    private var notificationManagerHelper:NotificationManagerHelper?= null
    private var repositoryLocation: RepositoryLocation? = null

    override fun onCreate() {
        super.onCreate()

        requestChannel=Channel<Intent>(Channel.UNLIMITED)
        serviceScope=CoroutineScope(Dispatchers.IO + Job())

        repositoryLocation = RepositoryLocation.getInstance(application)

        notificationManagerHelper=NotificationManagerHelper.getInstance(applicationContext)
        notificationManagerHelper?.createChannelForegroundServices()
        val notification = notificationManagerHelper?.createNotificationForegroundService()

        startForeground(Definition.FIRST_NOTIFICATION_ID, notification)

        //empieza a recibir actualizaciones del gps
        repositoryLocation?.startLocationUpdates()

        channelLector()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            // Comprobar el estado del GPS
            repositoryLocation?.checkStatusGPS()

            // Encola la solicitud en el Channel
            requestChannel?.trySend(it)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
        repositoryLocation?.stopLocationUpdates()

        // Cancela la corutina cuando el servicio se destruye
        serviceScope?.cancel()
        serviceScope=null

        //libero los recursos
        requestChannel=null
        notificationManagerHelper=null
        repositoryLocation=null

        Log.d(Definition.TAG_DEBUG," GeofenceService Destruido")

    }

    private fun channelLector() {
        // Lector del Channel: consume las solicitudes encoladas

        serviceScope?.launch {
            requestChannel?.let { channel ->
                for (intent in channel) {
                    try {
                        handleIntent(intent) // Procesa cada intent
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

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

    override fun onBind(intent: Intent?): IBinder? = null

}

