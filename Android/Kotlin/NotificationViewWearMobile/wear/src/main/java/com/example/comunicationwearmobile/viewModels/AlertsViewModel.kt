package com.example.comunicationwearmobile.viewModels

import android.app.Application
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.comunicationwearmobile.MainActivity
import com.example.comunicationwearmobile.models.MobileDataListenerService
import com.example.comunicationwearmobile.models.MsgAlertState
import com.example.shared_library.SharedData
import com.example.shared_library.fromByteArray
import com.example.shared_library.toByteArray

/*
Los viewmodels se usan para mantener el estado de las variables. Esto sirve mas que nada para cuando
se gira la pantalla, o no se quiere guardar el estado de las mismas sin la necesidad de usar un
objeto bundle
 */
class AlertsViewModel(application: Application) : AndroidViewModel(application) {

   companion object {
       private var msgBytesDestroyed: ByteArray? = null
   }
    private val appContext: Context = application.applicationContext

    private var lifecycleOwner: LifecycleOwner? = null

    private val _stateListNotif = MutableLiveData<MsgAlertState>()
    val stateListNotif: LiveData<MsgAlertState> get() = _stateListNotif



    init {
        initLocalBroadcast()
        _stateListNotif.value = MsgAlertState()
    }

    private fun initLocalBroadcast() {
        val receiver = createBroadcastReceiver()

        LocalBroadcastManager.getInstance(appContext).registerReceiver(
            receiver , IntentFilter(SharedData.Broadcast.fromMobileData.name)
        )

        val serviceIntent = Intent(appContext , MobileDataListenerService::class.java)
        appContext.startService(serviceIntent)
    }

    private fun createBroadcastReceiver(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context? , intent: Intent?) {
                val path: String =
                    intent?.getStringExtra(SharedData.ParamIntent.MESSAGE_PATH.name)!!

                val msgBytes: ByteArray =  intent.getByteArrayExtra(SharedData.ParamIntent.MESSAGE_BODY.name)!!
                putActivityForeground()

                if(lifecycleOwner?.lifecycle?.currentState!=Lifecycle.State.DESTROYED) {
                    when (path) {
                        SharedData.PATH_ADD_NOTIFICATION -> addMsgAlertList(msgBytes)
                        SharedData.PATH_VIEWED_NOTIFICATION -> removeMsgAlert(msgBytes)
                    }
                }else{
                    msgBytesDestroyed=msgBytes
                }
            }
        }
    }

    fun setCompleteRecomposition(){
        msgBytesDestroyed?.let {
            addMsgAlertList(it)
            msgBytesDestroyed=null
        }
        Log.d("AlertViewModel","Se completo recomposition")
    }


    fun setLifecycleOwner(owner: LifecycleOwner) {
        lifecycleOwner = owner
    }



    private fun putActivityForeground() {
        Log.d("AlertsViewmodels","AlertViewmodels"+" thread: " + Thread.currentThread().getId())


        val currentSate = lifecycleOwner?.lifecycle?.currentState

        if((currentSate!=Lifecycle.State.RESUMED)or(currentSate!=Lifecycle.State.DESTROYED)){

            val activityIntent = Intent(appContext,MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            }

            val pendingIntent = PendingIntent.getActivity(appContext, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            try {
                // Iniciar la Activity
                pendingIntent.send()
            } catch (e: PendingIntent.CanceledException) {
                e.printStackTrace()
            }
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
        sendMsgRemoveMobile(indexList)
    }

    private fun updateRemoveMsg(indexList: Int) {
        _stateListNotif.value?.let { currentState ->
            val currentAlertsList = currentState.alertsList?.toMutableList()
            if (currentAlertsList != null) {
                if (indexList in currentAlertsList.indices) {
                    currentAlertsList.removeAt(indexList)
                    _stateListNotif.value = currentState.copy(alertsList = currentAlertsList)
                }
            }
        }
    }


    private fun sendMsgRemoveMobile(idMsg: Int) {
        val byteArrayData: ByteArray = toByteArray(idMsg)
        val serviceIntent = Intent(appContext , MobileDataListenerService::class.java).apply {
            putExtra(SharedData.ParamIntent.MESSAGE_PATH.name , SharedData.PATH_VIEWED_NOTIFICATION)
            putExtra(SharedData.ParamIntent.MESSAGE_BODY.name , byteArrayData)
        }
        appContext.startService(serviceIntent)
    }

    fun addMsgAlertList(msgBytes: ByteArray) {
        val msgAlert: SharedData.MsgNotification = fromByteArray(msgBytes)
        _stateListNotif.value?.let { currentState ->
            val currentAlertsList = currentState.alertsList?.toMutableList()
            currentAlertsList?.add(msgAlert)
            _stateListNotif.value = currentState.copy(alertsList = currentAlertsList)
        }
    }
}
