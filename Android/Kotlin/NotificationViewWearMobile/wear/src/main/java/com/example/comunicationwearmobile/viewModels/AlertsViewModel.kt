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
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.comunicationwearmobile.models.MobileDataListenerService
import com.example.comunicationwearmobile.models.MsgAlertState
import com.example.shared_library.SharedData
import com.example.shared_library.fromByteArray
import kotlinx.coroutines.launch

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

        viewModelScope.launch {
            state=state.copy(
                alertsList = listOf(
                    /*MsgAlertModel("Recordatorio de Hoy","Turno Medico",TypeMsg.Reminder),
                    MsgAlertModel("Perdio turno","No fue al medico",TypeMsg.Alert),*/
                    SharedData.MsgNotification("Todo esta bien","Sin notificaciones",
                        SharedData.TypeNotification.WithoutNotifications),
                    )
            )
        }

         initLocalBrodacast()
    }

  private fun initLocalBrodacast() {

    /*En el constructor se definen los broadcast que va a usar el service de WearableListenerService
    para enviar datos a la view. Como es un sevice se usan para ello los braodcast receiver
    */
       val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {

                val msgBytes:ByteArray = intent?.getByteArrayExtra(SharedData.INTENT_BODY_MESSAGE)!!
                val msgAlert:SharedData.MsgNotification = fromByteArray(msgBytes)
                addMsgAlertList(msgAlert)
            }

        }

        LocalBroadcastManager.getInstance(appContext).registerReceiver(
            receiver, IntentFilter(SharedData.BROADCAST_MOBILE_DATA)
        )

        val serviceIntent = Intent(appContext, MobileDataListenerService::class.java)
        appContext.startService(serviceIntent)

    }

    fun removeMsgAlert(indexList: Int){
        val updateAlert=state.alertsList.toMutableList()

        val idMsg:Int =getIdMsgList(indexList,updateAlert)
        removeMsgAlertList(indexList,updateAlert)

        sendMsgRemoveMobile(idMsg)
    }


    private fun getIdMsgList(indexList: Int, updateAlert: MutableList<SharedData.MsgNotification>): Int {
        val msgAlert= updateAlert.get(indexList)
        val idMsg=msgAlert.idNotification
        return idMsg
    }

    fun removeMsgAlertList(index: Int, updateAlert: MutableList<SharedData.MsgNotification>) {

        updateAlert.removeAt(index)
        state=state.copy(
            alertsList = updateAlert)
    }

    private fun sendMsgRemoveMobile(idMsg: Int) {

        }
    fun addMsgAlertList(msgAlert:SharedData.MsgNotification){
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
