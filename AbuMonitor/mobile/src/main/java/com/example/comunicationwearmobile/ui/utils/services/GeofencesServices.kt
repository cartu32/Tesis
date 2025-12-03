package com.example.comunicationwearmobile.ui.utils.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.Observer
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.model.extra.GeofenceEventParameter
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDebugLogger
import com.example.comunicationwearmobile.ui.model.repository.RepositoryLocation
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.GeofenceEventProcessorHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.GeofenceScheduleHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.Notification.NotificationHelper
import com.example.comunicationwearmobile.ui.utils.Tools.getParcelable
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

    private var notificationManagerHelper: NotificationHelper?= null
    private var repositoryLocation: RepositoryLocation? = null
    private var locationObserver :Observer<Location>?=null


    private lateinit var connectivityManager: ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate() {
        super.onCreate()

        val pid = android.os.Process.myPid()
        RepositoryDebugLogger.log(this, "ForegroundService.onCreate() PID=$pid")

        requestChannel=Channel<Intent>(Channel.UNLIMITED)
        serviceScope=CoroutineScope(Dispatchers.IO + Job())

        repositoryLocation = RepositoryLocation.getInstance(application)

        notificationManagerHelper= NotificationHelper.getInstance(applicationContext)

        val notification = notificationManagerHelper?.createNotificationForegroundService()

        notificationManagerHelper?.let {
            startForeground(it.ID_NOTIFICATION_FOREGROUND_SERVICE, notification)
        }

        // Inicializo ConnectivityManager y registro callback
        connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        registerNetworkCallback()

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

    private fun channelLector() {
        // Lector del Channel: consume las solicitudes encoladas

        serviceScope?.launch {
            requestChannel?.let { channel ->
                for (intent in channel) {
                    try {
                        handleIntent(intent) // Procesa cada intent
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e(Definition.TAG_DEBUG, "Error al procesar intent: ${e.message}")
                    }
                }
            }
        }

    }
    private suspend fun handleIntent(intent: Intent?)  {
        val geofenceHelper= GeofenceScheduleHelper(this)

        when(intent?.action){
            Definition.ACTION_ALARM_FOR_CHECKS-> geofenceHelper.executeActionsOfAlarm()
            Definition.ACTION_GEOFENCE_EVENT_BROADCAST -> callGeofenceEventProcessor(intent)

        }
    }

    private suspend fun callGeofenceEventProcessor(intent: Intent) {
        val parameter = intent.getParcelable<GeofenceEventParameter>(Definition.PARAMETER_SERVICE)

        parameter?.let {
            GeofenceEventProcessorHelper.handleEvent(it.triggeringIds, it.transition)
        }
    }

    private fun registerNetworkCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                // Se conectó a una red (WiFi o datos)
                RepositoryDebugLogger.log(
                    this@GeofencesServices,
                    "NETWORK_CHANGE: onAvailable -> pido ubicación puntual"
                )
                refreshLocationAfterNetworkChange()
            }

            override fun onLost(network: Network) {
                // Se perdió una red (ej: apagaste WiFi)
                RepositoryDebugLogger.log(
                    this@GeofencesServices,
                    "NETWORK_CHANGE: onLost -> pido ubicación puntual"
                )
                refreshLocationAfterNetworkChange()
            }
        }

        connectivityManager.registerNetworkCallback(request, networkCallback!!)
    }

    private fun unregisterNetworkCallback() {
        try {
            networkCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
        } catch (_: Exception) {
            // por si ya estaba unregister
        }
    }

    private fun refreshLocationAfterNetworkChange() {
        val appContext = applicationContext

        serviceScope?.launch {
            try {
                // 1) Ver si tiene sentido hacer algo (que haya áreas activas)
                val repoAreas = RepositoryAreaDB.getInstance(appContext)
                val activeAreas = repoAreas.getAllActiveAreasWithEvents() // usa tu método real

                if (activeAreas.isEmpty()) {
                    RepositoryDebugLogger.log(
                        appContext,
                        "NETWORK_CHANGE: no hay áreas activas, no pido ubicación"
                    )
                    return@launch
                }

                // 2) Pedir UNA sola ubicación liviana
                val repoLoc = RepositoryLocation.getInstance(appContext)
                val loc = repoLoc.getSingleBalancedLocation()

                if (loc != null) {
                    RepositoryDebugLogger.log(
                        appContext,
                        "NETWORK_CHANGE: ubicación puntual -> " +
                                "lat=${loc.latitude}, lon=${loc.longitude}, acc=${loc.accuracy}"
                    )
                    // El solo hecho de obtener esta ubicación ya "despierta" al proveedor
                    // y hace recalcular las geofences
                } else {
                    RepositoryDebugLogger.log(
                        appContext,
                        "NETWORK_CHANGE: no se pudo obtener ubicación puntual"
                    )
                }

            } catch (e: Exception) {
                RepositoryDebugLogger.log(
                    appContext,
                    "NETWORK_CHANGE: excepción al pedir ubicación puntual: ${e.message}"
                )
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null


    override fun onDestroy() {
        super.onDestroy()

        try {
            RepositoryDebugLogger.log(applicationContext, "GeofencesServices.onDestroy() llamado")

            stopForeground(STOP_FOREGROUND_REMOVE)

            //remueve los observer de livedata del repository
                repositoryLocation?.stopLocationUpdates()

            locationObserver?.let {
                repositoryLocation?.locationLiveData?.removeObserver(it)
            }

            // Cancela la corutina cuando el servicio se destruye
            serviceScope?.cancel()
            requestChannel?.close()
            serviceScope=null

            // Desregistrar callback de red
            unregisterNetworkCallback()

            //libero los recursos
            requestChannel=null
            notificationManagerHelper=null
            repositoryLocation=null

            Log.d(Definition.TAG_DEBUG," GeofenceService Destruido")
        } catch (e: Exception) {
            Log.e(Definition.TAG_DEBUG, "Error deteniendo location updates: ${e.message}")
        }

    }

}

