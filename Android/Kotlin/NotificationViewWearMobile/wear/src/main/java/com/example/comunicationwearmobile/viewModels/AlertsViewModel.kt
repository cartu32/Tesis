package com.example.comunicationwearmobile.viewModels

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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
    private val appContext:Context =application.applicationContext


    var state by mutableStateOf(MsgAlertState())
    private set

    init {
         initLocalBrodacast()
    }

  private fun initLocalBrodacast() {

    /*En el constructor se definen los broadcast que va a usar el service de WearableListenerService
    para enviar datos a la view. Como es un sevice se usan para ello los braodcast receiver
    */
       val receiver = createBroadcastReceiver()

        LocalBroadcastManager.getInstance(appContext).registerReceiver(
            receiver, IntentFilter(SharedData.Broadcast.fromMobileData.name)
        )

        val serviceIntent = Intent(appContext, MobileDataListenerService::class.java)
        appContext.startService(serviceIntent)

    }

    // Función para crear el BroadcastReceiver
    private fun createBroadcastReceiver(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val path:String = intent?.getStringExtra(SharedData.ParamIntent.MESSAGE_PATH.name)!!

                val msgBytes: ByteArray = intent.getByteArrayExtra(SharedData.ParamIntent.MESSAGE_BODY.name)!!


                when(path){
                    SharedData.PATH_ADD_NOTIFICATION -> addMsgAlertList(msgBytes)
                    SharedData.PATH_VIEWED_NOTIFICATION -> removeMsgAlert(msgBytes)
                }

            }
        }
    }

    private fun removeMsgAlert(msgBytes: ByteArray){
        val indexList:Int = fromByteArray(msgBytes)

        updateRemoveMsg(indexList)
    }

     fun removeMsgAlertList(indexList: Int){
        updateRemoveMsg(indexList)
        sendMsgRemoveMobile(indexList)
    }


    private fun updateRemoveMsg(indexList:Int){
        val updateAlert=state.alertsList.toMutableList()

        updateAlert.removeAt(indexList)
        state=state.copy(alertsList = updateAlert)

    }

    private fun sendMsgRemoveMobile(idMsg: Int) {

        val byteArrayData:ByteArray = toByteArray(idMsg)
        val serviceIntent = Intent(appContext, MobileDataListenerService::class.java).apply {
            putExtra(SharedData.ParamIntent.MESSAGE_PATH.name,SharedData.PATH_VIEWED_NOTIFICATION)
            putExtra(SharedData.ParamIntent.MESSAGE_BODY.name, byteArrayData)
        }
        appContext.startService(serviceIntent)
    }

    fun addMsgAlertList(msgBytes: ByteArray){
        val msgAlert: SharedData.MsgNotification = fromByteArray(msgBytes)

        val indexNewItem=state.alertsList.count()
        val updateAlert=state.alertsList.toMutableList()


        updateAlert.add(indexNewItem,msgAlert)
        state=state.copy(
            alertsList = updateAlert)
    }

    fun getCountItemList():Int{
        return state.alertsList.count()
    }
}
