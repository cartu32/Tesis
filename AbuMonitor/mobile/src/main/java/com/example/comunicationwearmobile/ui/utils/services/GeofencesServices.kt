package com.example.comunicationwearmobile.ui.utils.services

import android.app.Activity
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.utils.Mannager.SmsManagerCustom
import com.example.comunicationwearmobile.ui.model.repository.RepositoryLocation
import com.example.comunicationwearmobile.ui.utils.Mannager.NotificationManagerHelper
import com.example.shared_library.SharedData
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
        registerSMSReceivers(this)
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
        var operation=""
        if (intent != null) {
            operation = intent.getStringExtra(Definition.OPERATION_START_FOREGROUND_SERVICE).toString()
        }

        when (operation) {
            Definition.OPERATION_GOEFENCE_SEND_SMS->sendSMSContact(intent)
            else ->
                Log.e(Definition.TAG_DEBUG , "Operation desconocido en HandleIntent")
        }
    }

    private fun sendSMSContact(intent: Intent?) {
        val smsManager= SmsManagerCustom()


        if (intent == null)
            return

        val msg: SharedData.MsgNotification? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(Definition.OPERATION_GOEFENCE_SEND_SMS, SharedData.MsgNotification::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(Definition.OPERATION_GOEFENCE_SEND_SMS) as? SharedData.MsgNotification
        }

        msg?.let {
            smsManager.sendSMSNotifyGeofence(this, it)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    fun registerSMSReceivers(context: Context) {
        // Receiver para el envío del SMS
        ContextCompat.registerReceiver(context, object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (resultCode) {
                    Activity.RESULT_OK -> Log.d("SMS", " SMS enviado correctamente")
                    SmsManager.RESULT_ERROR_GENERIC_FAILURE -> Log.e(
                        "SMS",
                        " Fallo genérico al enviar SMS"
                    )

                    SmsManager.RESULT_ERROR_NO_SERVICE -> Log.e(Definition.TAG_DEBUG, " Sin servicio")
                    SmsManager.RESULT_ERROR_NULL_PDU -> Log.e(Definition.TAG_DEBUG, " PDU nulo")
                    SmsManager.RESULT_ERROR_RADIO_OFF -> Log.e(Definition.TAG_DEBUG, " Radio apagada")
                }
            }
        }, IntentFilter("SMS_SENT"), ContextCompat.RECEIVER_NOT_EXPORTED)

        // Receiver para la entrega del SMS
        ContextCompat.registerReceiver(context, object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (resultCode) {
                    Activity.RESULT_OK -> Log.d(Definition.TAG_DEBUG, "SMS entregado correctamente")
                    else -> Log.e(Definition.TAG_DEBUG, " SMS no fue entregado")
                }
            }
        }, IntentFilter("SMS_DELIVERED"), ContextCompat.RECEIVER_EXPORTED)
    }

}

