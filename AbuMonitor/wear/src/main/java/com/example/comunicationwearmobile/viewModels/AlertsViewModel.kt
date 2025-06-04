package com.example.comunicationwearmobile.viewModels

import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.comunicationwearmobile.models.entities.DataClass_MsgAlertState
import com.example.comunicationwearmobile.models.repository.RepositoryHealthServices
import com.example.comunicationwearmobile.utils.isScreenLock
import com.example.comunicationwearmobile.utils.isScreenOn
import com.example.comunicationwearmobile.utils.mannager.MediaMannager
import com.example.comunicationwearmobile.utils.mannager.PermissionManager
import com.example.comunicationwearmobile.utils.sendMessageMobile
import com.example.comunicationwearmobile.utils.services.MobileDataListenerService
import com.example.comunicationwearmobile.view.activities.MainActivity
import com.example.shared_library.SharedData
import com.example.shared_library.fromByteArray
import com.example.shared_library.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


open class AlertsViewModel(private var app: Application) : AndroidViewModel(app) {

     private val TAG: String = "AlertViewModel"

    private val repositoryHealthServices = RepositoryHealthServices.getInstance(app)

    private var lifecycleOwner: LifecycleOwner? = null
    private val _stateListNotif = MutableLiveData<DataClass_MsgAlertState>()
    val stateListNotif: LiveData<DataClass_MsgAlertState> get() = _stateListNotif

    private var previousActivityState= Lifecycle.State.DESTROYED

    private var isFallMsgShowing=false


    companion object {
        private var msgBytesDestroyed: ByteArray? = null
    }

    init {
        initLocalBroadcast()
        _stateListNotif.value = DataClass_MsgAlertState()
    }

    private fun initLocalBroadcast() {
        val receiver = createBroadcastReceiver()

        //se usa el mismo handler reciever del bordcast para recepcionar tanto los datos
        //del mobile como cuando se detecta las caidas.
        LocalBroadcastManager.getInstance(app).registerReceiver(
            receiver , IntentFilter(SharedData.Broadcast.fromMobileData.name))

        LocalBroadcastManager.getInstance(app).registerReceiver(
            receiver , IntentFilter(SharedData.Broadcast.alertFallDetect.name))

        app.startService( Intent(app , MobileDataListenerService::class.java))
    }

    private fun createBroadcastReceiver(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            @RequiresApi(Build.VERSION_CODES.S)
            override fun onReceive(context: Context? , intent: Intent?) {
                intent?.let {
                    val path: String? = it.getStringExtra(SharedData.ParamIntent.MESSAGE_PATH.name)
                    val msgBytes: ByteArray? = it.getByteArrayExtra(SharedData.ParamIntent.MESSAGE_BODY.name)

                    analizeStateActvity(path,msgBytes)
                }
            }

        }
    }

    // Método suspendido para manejar la lógica de negocios
    suspend fun checkPermissionsAndCapabilities(permissionManager: PermissionManager):Boolean{
        // Verificar permisos
        if (!permissionManager.checkPermissionGiven()) {
            return false
        }

        // Verificar capacidades de eventos de salud
        if (!repositoryHealthServices.hasHealthEventsCapability()) {
            return false
        }

        // Registrar para eventos de salud
        repositoryHealthServices.registerFallDetectorEventsData()

        return true

    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun analizeStateActvity(path: String? , msgBytes: ByteArray?)
    {
        val currentActivityState = lifecycleOwner?.lifecycle?.currentState

        if(currentActivityState==Lifecycle.State.INITIALIZED)
            return

        isActivitiyInBackground(currentActivityState)
        isActivitiyStateNotDestoyed(currentActivityState,path,msgBytes)

        currentActivityState?.let{ previousActivityState= it}

    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun isActivitiyStateNotDestoyed(currentActivityState: Lifecycle.State? , path: String? , msgBytes: ByteArray?) {
        if(currentActivityState!=Lifecycle.State.DESTROYED) {
            msgBytes?.let { analizepath(path.toString() , it) }
        }else{
            msgBytesDestroyed=msgBytes
        }
    }

    private fun isActivitiyInBackground(currentActivityState: Lifecycle.State?) {
        if(currentActivityState!=Lifecycle.State.RESUMED && !isScreenLock(app) && isScreenOn(app)){
            putActivityForeground()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun analizepath(path: String , msgBytes: ByteArray) {
        when (path) {
            SharedData.PATH_ADD_NOTIFICATION_GENERAL -> {
                addMsgAlertList(msgBytes)
                MediaMannager.generateVibration(app)
            }
            SharedData.PATH_VIEWED_NOTIFICATION -> removeMsgAlert(msgBytes)

            SharedData.PATH_ADD_NOTIFICATION_FALL -> {
                addMsgAlertList(msgBytes)
                MediaMannager.generateVibration(app)
            }

            else->Log.d (TAG,"Error de path al analizar el path")
        }
    }

    //@RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("NewApi")
    fun setCompleteRecomposition() {
        if (previousActivityState == Lifecycle.State.DESTROYED) {
            msgBytesDestroyed?.let { msgBytes ->
                if (msgBytes.isNotEmpty()) {
                    addMsgAlertList(msgBytes)
                    MediaMannager.generateVibration(app)

                    msgBytesDestroyed = null
                }
            }

            Log.d(TAG, "Se completó recomposition")
        }
    }



    fun setLifecycleOwner(owner: LifecycleOwner) {
        lifecycleOwner = owner
    }



    private fun putActivityForeground() {
        try {
            val activityIntent = Intent(app, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            }
            val pendingIntent = PendingIntent.getActivity(app, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            // Iniciar la Activity
            pendingIntent.send()
        } catch (e: PendingIntent.CanceledException) {
            e.printStackTrace()
        }
    }
    fun removeAllMsgInAllDevices() {
        val alerts = _stateListNotif.value?.alertsList ?: emptyList()

        alerts.forEach { msgAlert ->
            if (msgAlert.idMsgMobile != SharedData.ID_MSG_FALL_DETECTED) {
                removeMsgAlertInAllDevices(msgAlert.idMsgMobile)
            }
        }
    }



    private fun removeMsgAlert(msgBytes: ByteArray) {
        val msgAlert:SharedData.MsgNotification = fromByteArray(msgBytes)
        val idMsgMobile=msgAlert.idMsgMobile

        if (idMsgMobile==SharedData.GROUP_ID_NOTIFICATION){
            removeAllMsgInThisDevices()
        }else
        {
            updateRemoveMsg(idMsgMobile)
        }
    }

    private fun removeAllMsgInThisDevices() {
        val alerts = _stateListNotif.value?.alertsList ?: emptyList()

        alerts.forEach { msgAlert ->
            if (msgAlert.idMsgMobile != SharedData.ID_MSG_FALL_DETECTED) {
                updateRemoveMsg(msgAlert.idMsgMobile)
            }
        }
    }


    fun removeMsgAlertInAllDevices(idMsgMobile: Int) {

        //elimino el msg de la pantalla del wear
        updateRemoveMsg(idMsgMobile)

        //le aviso al smartphone que quite la notificacion de su bandeja de notifcaciones
        notifySmartphone(SharedData.PATH_VIEWED_NOTIFICATION, toByteArray(idMsgMobile))

    }

    private fun updateRemoveMsg(idMsgMobile: Int) {
        var resp=false

        _stateListNotif.value?.let {
            val currentAlertsList = it.alertsList?.toMutableList()
            if (currentAlertsList != null) {
                //borra el msg de la lista de notificaciones que tiene el idMsgMobile
                resp=currentAlertsList.removeIf { it.idMsgMobile == idMsgMobile } }

            if (resp==true)
                _stateListNotif.value = it.copy(alertsList = currentAlertsList)

        }
    }


    private fun addMsgAlertList(msgBytes: ByteArray) {
        // Si ya hay una alerta de caída en pantalla, ignoro nuevos mensajes
        //hasta que el usuario cancele la notificacion de caida
        if (isFallMsgShowing) {
            return
        }

        _stateListNotif.value?.let {
            val msgAlert: SharedData.MsgNotification = fromByteArray(msgBytes)
            val currentAlertsList = it.alertsList?.toMutableList() ?: mutableListOf()

            //si es un mensaje de alerta de caida marco la bandera que se esta mostrando.
            if (msgAlert.idMsgMobile==SharedData.ID_MSG_FALL_DETECTED)
                isFallMsgShowing=true

            currentAlertsList.add(msgAlert)

            _stateListNotif.value = it.copy(alertsList = currentAlertsList)
            currentAlertsList.lastIndex // Retorna el índice del elemento recién agregado
        }
    }

    fun notifySmartphone(path:String, msgBytes: ByteArray){
        viewModelScope.launch(Dispatchers.IO) { // Lanzar la corutina en Dispatchers.IO para operaciones de I/O
            try {
                sendMessageMobile(app, path, msgBytes)
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar el mensaje al móvil: ${e.message}")
            } finally {
                println("Limpieza al finalizar la corutina")
            }
        }

    }


    fun notifyFallBySmartPhone(){

        var msgFallDetection = SharedData.MsgFallDetection(
            title = "¡¡ALERTA!! ",
            message = "Abumonitor ha detectado una caida.",
            fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
        )

        notifySmartphone(SharedData.PATH_FALL_DETECTION_SMS, toByteArray( msgFallDetection))
    }


    fun cancelNotifyFallBySmartPhone() {
        //borro el msg de caida de la pantalla
        updateRemoveMsg(SharedData.ID_MSG_FALL_DETECTED)

        //habilito a que vuelva a mostrar notificaciones normals en pantalla
        isFallMsgShowing=false

        //se envia un mensaje al familiar que se recupero de la caida
        var msgFallDetection = SharedData.MsgFallDetection(
            title = "Se recupero de la caida",
            message = "La persona se recupero de la caida",
            fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
        )

        notifySmartphone(SharedData.PATH_FALL_DETECTION_SMS, toByteArray( msgFallDetection))
    }
}
