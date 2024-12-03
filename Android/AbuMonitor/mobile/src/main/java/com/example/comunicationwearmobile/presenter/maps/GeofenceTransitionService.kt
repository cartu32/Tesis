package com.example.comunicationwearmobile.presenter.maps

import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.comunicationwearmobile.models.maps.SharedPreferencesRoutes
import com.example.comunicationwearmobile.ui.Activities.MapsActivity
import com.example.comunicationwearmobile.utils.maps.NotificationHelper
import com.example.comunicationwearmobile.utils.maps.Tools
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.Serializable

class GeofenceTransitionService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private val requestChannel = Channel<Intent>(Channel.UNLIMITED) // Cola de peticiones

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Encola la solicitud en el Channel
        intent?.let {
            requestChannel.trySend(it)
        }
        return START_NOT_STICKY
    }

    private suspend fun handleIntent(intent: Intent?)  {
        var operation = 0
        if (intent != null) {
            operation = intent.getIntExtra("Operation" , -1)
        }

        when (operation) {
            Tools.GEOFENCE_TRANSITION -> operationTransition(intent)
            Tools.GEOFENCE_ROUTE -> operationRoute()
            else -> Log.e(TAG , "Error en on HandleIntent")
        }
    }

    private suspend fun operationRoute() {
        val msg = "Persona fuera de ruta"

        // Retrieve GeofenceTrasition
        val notificationHelper = NotificationHelper.getInstance(applicationContext)
        // Send notification details as a String
        notificationHelper?.sendHighPriorityNotification(
            "Evento detectado" ,
            msg ,
            MapsActivity::class.java
        )
    }

    private suspend fun operationTransition(intent: Intent?) {
        val geoFenceTransition: Int
        val msg: String?

        val geofencingEvent = GeofencingEvent.fromIntent(intent!!)

        // Handling errors
        if (geofencingEvent!!.hasError()) {
            msg = getErrorString(geofencingEvent.errorCode)
            Log.e(TAG , msg)
            return
        }


        geoFenceTransition = geofencingEvent.geofenceTransition

        //se obtienen las areas que de geofecnes que se detectaron en ese momento
        val triggeringGeofences = geofencingEvent.triggeringGeofences

        //Si bien varios areas de geofences pueden detectarse al mismo tiempo
        //solamente se va a utilizar una sola para determinar el camino activo.
        msg = getMessageEvent(geoFenceTransition , triggeringGeofences!![0].requestId)

        //establezco la ruta activa. Leyendo los waypoints del sharedpreference
        determineRouteActive(geoFenceTransition , triggeringGeofences[0].requestId)

        // Retrieve GeofenceTrasition
        val notificationHelper = NotificationHelper.getInstance(applicationContext)
        // Send notification details as a String
        notificationHelper?.sendHighPriorityNotification(
            "Evento detectado" ,
            msg ,
            MapsActivity::class.java
        )
    }

    private suspend fun determineRouteActive(geoFenceTransition: Int , idArea: String) {
        if (!idArea.contains(Tools.WORD_INITIAL)) return

        if (geoFenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER) {
            return
        }

        determineStatusRoute(idArea)
    }

    private suspend  fun determineStatusRoute(idActual: String) {
        //me fijo si es un geofence origen que esta guardado en el shared preference
        val listRoute = SharedPreferencesRoutes.getStringArrayPref(applicationContext , idActual)

        //Si me la encontro dentro del archivo entonces quiere decir que es la nueva ruta activa.
        if (listRoute.size != 0) {
            setActiveRoute(idActual , listRoute)
        } else {
            clearActiveRoute(idActual)
        }
    }

    private suspend fun setActiveRoute(idActual: String , listRoute: List<LatLng>) {
        Log.d(TAG , "Ruta activa")

        val i = Intent("com.example.intentservice.intent.action.RESPUESTA_OPERACION")
        i.putExtra("Operacion" , Tools.OPERATION_UPDATE_ACTIVE_ROUTE)
        i.putExtra("rutaActiva" , listRoute as Serializable)

        //almaceno el id de la ruta Activa
        idActiveOrigin = idActual
        controlingRoute = true

        //Se envian la ruta nueva ruta activa al bradcast reciever de la activity principal
        sendBroadcast(i)
    }

    private suspend fun getPosNumberId(id: String?): Int {
        var auxChar: Char
        var flag = false

        var index = id!!.length - OFFSET_ARRAY
        do {
            auxChar = id[index]
            if (Character.isDigit(auxChar)) {
                index--
            } else {
                flag = true
            }
        } while (flag != true)

        return index + OFFSET_ARRAY
    }

    private suspend fun clearActiveRoute(idActual: String) {
        val i: Intent


        if (controlingRoute == false) return


        //calculo cual deberia ser el id del gefecence Destino.
        //Este deberia ser Origen+1
        val idActiveDestination = prepareNextId() ?: return

        //Si el id del Geofence actual es igual al que se estimo,
        //entonces estoy en el final de la ruta.
        if (idActual == idActiveDestination) {
            controlingRoute = false

            //Se envian la ruta nueva ruta activa al bradcast reciever de la activity principal
            i = Intent("com.example.intentservice.intent.action.RESPUESTA_OPERACION")
            i.putExtra("Operacion" , Tools.OPERATION_CLEAR_ACTIVE_ROUTE)
            sendBroadcast(i)
        }
    }

    private suspend fun prepareNextId(): String? {
        var nextId: Int
        val idActiveDestination: String


        val posNumberId = getPosNumberId(idActiveOrigin)

        if (posNumberId == -1) {
            Log.e(TAG , "Error en getPosNumberID()")
            return null
        }


        nextId = idActiveOrigin!!.substring(posNumberId).toInt()
        nextId++


        idActiveDestination = idActiveOrigin!!.substring(0 , posNumberId) + nextId

        return idActiveDestination
    }

    private suspend fun getMessageEvent(geoFenceTransition: Int , idArea: String): String? {
        var msg: String? = null

        when (geoFenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> msg = "Entrando en$idArea"
            Geofence.GEOFENCE_TRANSITION_EXIT -> msg = "Saliendo de$idArea"
            else -> Log.e(TAG , "error en getMessageEvent")
        }
        return msg
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel() // Cancela la corutina cuando el servicio se destruye
    }

    companion object {
        private val TAG: String = GeofenceTransitionService::class.java.simpleName
        private const val OFFSET_ARRAY = 1
        private var controlingRoute = false
        private var idActiveOrigin: String? = null

        // Handle errors
        private fun getErrorString(errorCode: Int): String {
            return when (errorCode) {
                GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> "GeoFence not available"
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> "Too many GeoFences"
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> "Too many pending intents"
                else -> "Unknown error."
            }
        }
    }
}


