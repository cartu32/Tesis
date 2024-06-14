package com.example.comunicationwearmobile.viewModels

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.comunicationwearmobile.common.TypeMsg
import com.example.comunicationwearmobile.models.MobileDataListenerService
import com.example.comunicationwearmobile.models.MsgAlertModel
import com.example.comunicationwearmobile.models.MsgAlertState
import com.example.shared_library.SharedData
import kotlinx.coroutines.launch

/*
Los viewmodels se usan para mantener el estado de las variables. Esto sirve mas que nada para cuando
se gira la pantalla, o no se quiere guardar el estado de las mismas sin la necesidad de usar un
objeto bundle
 */
class AlertsViewModel:ViewModel() {
    var state by mutableStateOf(MsgAlertState())
    private set

    init {
        viewModelScope.launch {
            state=state.copy(
                alertsList = listOf(
                    /*MsgAlertModel("Recordatorio de Hoy","Turno Medico",TypeMsg.Reminder),
                    MsgAlertModel("Perdio turno","No fue al medico",TypeMsg.Alert),*/
                    MsgAlertModel("Todo esta bien","Sin notificaciones",TypeMsg.WithoutNotifications),
                    )
            )
        }

        initLocalBrodacast()
    }

    private fun initLocalBrodacast() {
    /*
    En el constructor se definen los broadcast que va a usar el service de WearableListenerService
    para enviar datos a la view. Como es un sevice se usan para ello los braodcast receiver
    */
       val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val message = intent?.getStringExtra("message") ?: return
                setMessage(message)
            }

        }

        LocalBroadcastManager.getInstance(application).registerReceiver(
            receiver, IntentFilter(SharedData.broadcast_mobile_data)
        )

        val serviceIntent = Intent(appContext, MobileDataListenerService::class.java)
        appContext.startService(serviceIntent)

    }

    fun removeMsgAlertList(msgAlert:MsgAlertModel) {

        val index= state.alertsList.indexOf(msgAlert)
        val updateAlert=state.alertsList.toMutableList()

        updateAlert.removeAt(index)
        state=state.copy(
            alertsList = updateAlert)
    }

    fun addMsgAlertList(msgAlert:MsgAlertModel){
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
