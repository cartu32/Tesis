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
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDebugLogger
import com.example.comunicationwearmobile.ui.model.repository.RepositoryLocation
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.GeofenceEventProcessorHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.ManualGeofenceStrategyHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.GeofenceScheduleHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.Notification.NotificationHelper
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_DWELL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class GeofencesServices: Service() {

    // Canal que se utiliza para encolar las peticiones realizadas cada vez que
    // se ejecuta stratservice
    private var requestChannel=Channel<Intent>(Channel.UNLIMITED)
    private var serviceScope=CoroutineScope(Dispatchers.IO + Job())

    private var notificationManagerHelper: NotificationHelper?= null
    private var repositoryLocation: RepositoryLocation? = null


    private lateinit var connectivityManager: ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private var mutexLocationUpdate= Mutex()

    companion object {
        // Mínimo intervalo entre pedidos de ubicación provocados por cambios de red
        private const val MIN_NETWORK_REFRESH_INTERVAL_MS = 30_000L // 30s, ajustable
        private var lastNetworkRefreshTimeMs: Long = 0L
    }

    override fun onCreate() {
        super.onCreate()

        val pid = android.os.Process.myPid()
        RepositoryDebugLogger.log(this, "ForegroundService.onCreate() PID=$pid")

        initializeComponent()
        showNotificationForeground()

        startLocationUpdates()

        channelLector()

    }

    private fun startLocationUpdates() {
        //empieza a recibir actualizaciones del gps
        repositoryLocation?.startLocationUpdates()

        serviceScope.launch {
            repositoryLocation?.locationFlow
                //  ?.sample(Definition.SAMPLE_TAKE_LOCATION_UPDATE)
                ?.conflate()
                ?.collect { location ->
                    mutexLocationUpdate.withLock {
                        Log.d(Definition.TAG_DEBUG, "Nueva ubicación in GeofencesServices: ${location.latitude}, ${location.longitude}")

                        ManualGeofenceStrategyHelper.callGeofenceManualStrategy(this@GeofencesServices,location)
                    }
                }
        }
    }

    private fun showNotificationForeground() {
        val notification = notificationManagerHelper?.createNotificationForegroundService()

        notificationManagerHelper?.let {
            startForeground(it.ID_NOTIFICATION_FOREGROUND_SERVICE, notification)
        }

    }

    private fun initializeComponent() {
        repositoryLocation = RepositoryLocation.getInstance(application)
        notificationManagerHelper= NotificationHelper.getInstance(applicationContext)
        // Inicializo ConnectivityManager y registro callback
        connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // registerNetworkCallback()

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
            Definition.ACTION_ALARM_FOR_DWELL_TIME->executeActionsOfAlarmDwell(intent)

        }
    }

    private suspend fun executeActionsOfAlarmDwell(intent: Intent) {
        val areaId = intent.extras?.getLong(Definition.INTENT_ALARM_PARAM1) ?: return

        try {
            // 1) Obtener área desde DB
            val repoAreas = RepositoryAreaDB.getInstance(applicationContext)
            val areaJoin = repoAreas.getJoinAreaGeofence(areaId) ?: run {
                RepositoryDebugLogger.log(applicationContext, "DWELL: area inexistente id=$areaId -> descarto")
                return
            }
            val area = areaJoin.areaGeofence

            // 2) Pedir UNA ubicación puntual para revalidar (evita dwell falso)
            val repoLoc = RepositoryLocation.getInstance(applicationContext)
            val loc = repoLoc.getSingleBalancedLocation() ?: run {
                RepositoryDebugLogger.log(applicationContext, "DWELL: sin ubicación puntual -> descarto area=$areaId")
                return
            }

            // 3) Distancia al centro
            val center = Location("dwell_center").apply {
                latitude = area.latitude.toDouble()
                longitude = area.longitude.toDouble()
            }

            val dist = loc.distanceTo(center)
            val radius = area.meters.toFloat()

            // 4) Margen por accuracy (simple pero efectivo)
            val margin = kotlin.math.max(5f, loc.accuracy * 0.5f)

            // Adentro si está suficientemente lejos del borde hacia dentro
            val inside = dist <= (radius - margin)

            if (!inside) {
                RepositoryDebugLogger.log(
                    applicationContext,
                    "DWELL: revalidación FAIL area=$areaId dist=${dist} r=$radius acc=${loc.accuracy} margin=$margin -> NO disparo"
                )
                return
            }

            RepositoryDebugLogger.log(
                applicationContext,
                "DWELL: revalidación OK area=$areaId dist=${dist} r=$radius acc=${loc.accuracy} margin=$margin -> DISPARO"
            )

            GeofenceEventProcessorHelper.handleEvent(mutableListOf(areaId), GEOFENCE_TRANSITION_DWELL)

        } finally {
            Log.d(Definition.TAG_DEBUG, "!!!!Alarma de Dwell Time (procesada)")
        }
    }



    private fun registerNetworkCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            // Si quisieras solo WiFi, podrías agregar:
            // .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                RepositoryDebugLogger.log(
                    this@GeofencesServices,
                    "NETWORK_CHANGE: onAvailable net=${network.hashCode()} -> pido ubicación puntual"
                )
                refreshLocationAfterNetworkChange()
            }

            override fun onLost(network: Network) {
                RepositoryDebugLogger.log(
                    this@GeofencesServices,
                    "NETWORK_CHANGE: onLost net=${network.hashCode()} -> pido ubicación puntual"
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

        // --- 0) Anti-spam por flapping de red ---
        val now = System.currentTimeMillis()
        val elapsed = now - lastNetworkRefreshTimeMs

        if (elapsed < MIN_NETWORK_REFRESH_INTERVAL_MS) {
            RepositoryDebugLogger.log(
                appContext,
                "NETWORK_CHANGE: ignorado (solo pasaron ${elapsed}ms; min=$MIN_NETWORK_REFRESH_INTERVAL_MS)"
            )
            return
        }
        lastNetworkRefreshTimeMs = now

        // --- 1) Lógica original ---
        serviceScope?.launch {
            try {
                // 1) Ver si tiene sentido hacer algo (que haya áreas activas)
                val repoAreas = RepositoryAreaDB.getInstance(appContext)
                val activeAreas = repoAreas.getAllActiveAreasWithEvents()

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
                    // Con esto ya "despertás" el proveedor y refrescás geofences
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

            repositoryLocation?.stopLocationUpdates()


            // Cancela la corutina cuando el servicio se destruye
            serviceScope.cancel()
            requestChannel.close()

            // Desregistrar callback de red
            unregisterNetworkCallback()

            //libero los recursos
            notificationManagerHelper=null
            repositoryLocation=null

            Log.d(Definition.TAG_DEBUG," GeofenceService Destruido")
        } catch (e: Exception) {
            Log.e(Definition.TAG_DEBUG, "Error deteniendo location updates: ${e.message}")
        }

    }

}

