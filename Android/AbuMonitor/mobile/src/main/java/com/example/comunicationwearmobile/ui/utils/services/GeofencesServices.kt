package com.example.comunicationwearmobile.ui.utils.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.abumonitor.constants.Definition
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
    private val requestChannel = Channel<Intent>(Channel.UNLIMITED)
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var notificationManagerHelper:NotificationManagerHelper?= null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        notificationManagerHelper=NotificationManagerHelper.getInstance(applicationContext)
        notificationManagerHelper?.createChannelForegroundServices()
        val notification = notificationManagerHelper?.createNotificationForegroundService()


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

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(Service.STOP_FOREGROUND_REMOVE)
        serviceScope.cancel() // Cancela la corutina cuando el servicio se destruye

        Log.d(Definition.TAG_DEBUG," GeofenceService Destruido")
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Encola la solicitud en el Channel
        intent?.let {
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