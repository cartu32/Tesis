package com.example.comunicationwearmobile.ui.utils.services

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.Observer
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDispatcherWearable
import com.example.comunicationwearmobile.ui.model.repository.RepositoryLocation
import com.example.comunicationwearmobile.ui.utils.Mannager.NotificationManagerHelper
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
    private var locationObserver :Observer<Location>?=null

    override fun onCreate() {
        super.onCreate()

        requestChannel=Channel<Intent>(Channel.UNLIMITED)
        serviceScope=CoroutineScope(Dispatchers.IO + Job())

        repositoryLocation = RepositoryLocation.getInstance(application)

        notificationManagerHelper=NotificationManagerHelper.getInstance(applicationContext)

        val notification = notificationManagerHelper?.createNotificationForegroundService()

        notificationManagerHelper?.let {
            startForeground(it.ID_NOTIFICATION_FOREGROUND_SERVICE, notification)
        }

        //empieza a recibir actualizaciones del gps
        repositoryLocation?.startLocationUpdates()

        channelLector()
        configOberserverLivedata()
    }

    private fun configOberserverLivedata() {

        locationObserver = Observer<Location> { location ->
           // Log.d("LocationService", "Nueva ubicación in GeofencesServices: ${location.latitude}, ${location.longitude}")
        }

        repositoryLocation?.locationLiveData?.observeForever(locationObserver!!)
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

        //remueve los observer de livedata del repository
        repositoryLocation?.stopLocationUpdates()

        locationObserver?.let {
            repositoryLocation?.locationLiveData?.removeObserver(it)
        }

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
    private fun handleIntent(intent: Intent?)  {
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

