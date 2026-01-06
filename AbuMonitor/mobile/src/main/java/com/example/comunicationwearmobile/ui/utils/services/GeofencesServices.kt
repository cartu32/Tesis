package com.example.comunicationwearmobile.ui.utils.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDebugLogger
import com.example.comunicationwearmobile.ui.model.repository.RepositoryLocation
import com.example.comunicationwearmobile.ui.utils.Helpers.Alarm.GeofenceDwellAlarmHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.GeofenceScheduleHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.ManualGeofenceStrategyHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.Network.NetWorkHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.Notification.NotificationHelper
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

    private var mutexLocationUpdate= Mutex()



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
        //NetWorkHelper.initNetworkHelper(applicationContext,serviceScope)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            // Comprobar el estado del GPS
            repositoryLocation?.checkStatusGPS()

            // Encola la solicitud en el Channel
            requestChannel.trySend(it)


        }

        return START_STICKY
    }

    private fun channelLector() {
        // Lector del Channel: consume las solicitudes encoladas

        serviceScope.launch {
            requestChannel.let { channel ->
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
            Definition.ACTION_ALARM_FOR_DWELL_TIME->{
                val areaId=intent.extras?.getLong(Definition.INTENT_ALARM_ID)?:0
                GeofenceDwellAlarmHelper.executeActionsOfAlarmDwell(applicationContext,areaId)
            }
            Definition.ACTION_ALARM_FOR_ACTIVATION_AREA->{
                val timeCurrentAlarm=intent.extras?.getLong(Definition.INTENT_ALARM_TIME)?:0
                geofenceHelper.activateGeofenceScheduled(timeCurrentAlarm)
            }
            Definition.ACTION_ALARM_FOR_DESACTIVATION_AREA -> {
                val timeCurrentAlarm = intent.extras?.getLong(Definition.INTENT_ALARM_TIME) ?: 0
                geofenceHelper.deactivateGeofenceScheduled(timeCurrentAlarm)
            }
            Definition.ACTION_ALARM_FOR_REMINDER->{
                val timeCurrentAlarm=intent.extras?.getLong(Definition.INTENT_ALARM_TIME)?:0
                geofenceHelper.notifyReminderScheduled(timeCurrentAlarm)
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
            NetWorkHelper.unregisterNetworkCallback()

            //libero los recursos
            notificationManagerHelper=null
            repositoryLocation=null

            Log.d(Definition.TAG_DEBUG," GeofenceService Destruido")
        } catch (e: Exception) {
            Log.e(Definition.TAG_DEBUG, "Error deteniendo location updates: ${e.message}")
        }

    }

}

