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
import com.example.comunicationwearmobile.MainActivity
import com.example.comunicationwearmobile.models.entities.DataClass_MsgAlertState
import com.example.comunicationwearmobile.models.repository.RepositoryHealthServices
import com.example.comunicationwearmobile.utils.isScreenLock
import com.example.comunicationwearmobile.utils.isScreenOn
import com.example.comunicationwearmobile.utils.mannager.MediaMannager
import com.example.comunicationwearmobile.utils.mannager.PermissionManager
import com.example.comunicationwearmobile.utils.sendMessageMobile
import com.example.comunicationwearmobile.utils.services.MobileDataListenerService
import com.example.shared_library.SharedData
import com.example.shared_library.fromByteArray
import com.example.shared_library.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AlertsViewModel(private var app: Application) : AndroidViewModel(app) {

    private val TAG: String = "AlertViewModel"

    private val repositoryHealthServices = RepositoryHealthServices.getInstance(app)

    private var lifecycleOwner: LifecycleOwner? = null
    private val _stateListNotif = MutableLiveData<DataClass_MsgAlertState>()
    val stateListNotif: LiveData<DataClass_MsgAlertState> get() = _stateListNotif

    private var previousActivityState= Lifecycle.State.DESTROYED

    companion object {
        private var msgBytesDestroyed: ByteArray? = null
    }

    init {
        initLocalBroadcast()
        _stateListNotif.value = DataClass_MsgAlertState()
    }

    private fun initLocalBroadcast() {
        val receiver = createBroadcastReceiver()

        LocalBroadcastManager.getInstance(app).registerReceiver(
            receiver , IntentFilter(SharedData.Broadcast.fromMobileData.name)
        )
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
            SharedData.PATH_ADD_NOTIFICATION -> {
                addMsgAlertList(msgBytes)
                MediaMannager.generateVibration(app)
            }
            SharedData.PATH_VIEWED_NOTIFICATION -> removeMsgAlert(msgBytes)
            else->Log.d (TAG,"Error de path al analizar el path")
        }
    }

    //@RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("NewApi")
    fun setCompleteRecomposition(){
        if(previousActivityState!=Lifecycle.State.DESTROYED)
            return

        msgBytesDestroyed?.let {
            if (msgBytesDestroyed!!.isNotEmpty()) {
                addMsgAlertList(msgBytesDestroyed!!)
                MediaMannager.generateVibration(app)
                msgBytesDestroyed = null
            }
        }

        Log.d(TAG,"Se completo recomposition")
    }


    fun setLifecycleOwner(owner: LifecycleOwner) {
        lifecycleOwner = owner
    }



    private fun putActivityForeground() {
        try {
            val activityIntent = Intent(app,MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            }
            val pendingIntent = PendingIntent.getActivity(app, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            // Iniciar la Activity
            pendingIntent.send()
        } catch (e: PendingIntent.CanceledException) {
            e.printStackTrace()
        }
    }

    fun removeAllMsg() {
        _stateListNotif.value?.alertsList?.forEach { msgAlert ->
            val indexList = _stateListNotif.value?.alertsList?.indexOf(msgAlert) ?: -1
            if (indexList != -1) {
                removeMsgAlertList(indexList)
            }
        }
    }


    private fun removeMsgAlert(msgBytes: ByteArray) {
        val indexList: Int = fromByteArray(msgBytes)
        updateRemoveMsg(indexList)
    }



    fun removeMsgAlertList(indexList: Int) {
        updateRemoveMsg(indexList)
        viewModelScope.launch(Dispatchers.IO) { // Lanzar la corutina en Dispatchers.IO para operaciones de I/O
            try {
                sendMessageMobile(app, SharedData.PATH_VIEWED_NOTIFICATION, toByteArray(indexList))
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar el mensaje al móvil: ${e.message}")
            } finally {
                println("Limpieza al finalizar la corutina")
            }
        }
    }
    private fun updateRemoveMsg(indexList: Int) {
        _stateListNotif.value?.let {
            val currentAlertsList = it.alertsList?.toMutableList()
            if (currentAlertsList != null) {
                if (indexList in currentAlertsList.indices) {
                    currentAlertsList.removeAt(indexList)
                    _stateListNotif.value = it.copy(alertsList = currentAlertsList)
                }
            }
        }
    }


    private fun addMsgAlertList(msgBytes: ByteArray) {
        val msgAlert: SharedData.MsgNotification = fromByteArray(msgBytes)
        _stateListNotif.value?.let {
            val currentAlertsList = it.alertsList?.toMutableList()
            currentAlertsList?.add(msgAlert)
            _stateListNotif.value = it.copy(alertsList = currentAlertsList)
        }
    }
}
