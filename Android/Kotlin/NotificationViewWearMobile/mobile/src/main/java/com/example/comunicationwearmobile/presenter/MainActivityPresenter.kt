
package com.example.comunicationwearmobile.presenter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.comunicationwearmobile.common.InterfaceMainAct
import com.example.comunicationwearmobile.common.InterfaceMainPre
import com.example.comunicationwearmobile.models.CounterNotification
import com.example.comunicationwearmobile.models.WearableDataListenerService
import com.example.shared_library.SharedData
import com.example.shared_library.fromByteArray
import com.example.shared_library.toByteArray


class MainActivityPresenter(interMainView: InterfaceMainAct):InterfaceMainPre {

    private var interMainView: InterfaceMainAct? = interMainView
    private val mContext: Context? = interMainView as? Context


    init{

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val msgBytes:ByteArray = intent?.getByteArrayExtra(SharedData.INTENT_BODY_MESSAGE)!!

                val msgAlert: SharedData.MsgResponseNotification = fromByteArray(msgBytes)
                interMainView.updateTextBox(msgAlert.idNotification.toString())
            }

        }

        LocalBroadcastManager.getInstance(mContext!!).registerReceiver(
            receiver, IntentFilter("WearDataListenerService.MessageReceived")
        )

        val serviceIntent = Intent(mContext, WearableDataListenerService::class.java)
        mContext.startService(serviceIntent)
    }
    override fun onDataReceived(data: String) {
        interMainView?.showToast(data)
        interMainView?.updateTextBox(data)
    }

    public fun sendDataWearable(msg:SharedData.MsgNotification){

        //Establezco el id de la notificacion segun el contador
        CounterNotification.incrementCounter()
        msg.idNotification=CounterNotification.getCounter()

        sendMessageToService(SharedData.TypeMsg.messageDevice.name,msg)
    }


    fun onCleared() {
        sendMessageToService(SharedData.TypeMsg.cancelCoroutines.name,SharedData.MsgNotification())
    }

    private fun sendMessageToService(typeMsg: String, msgAlert:SharedData.MsgNotification){
        val byteArrayData:ByteArray =toByteArray(msgAlert)
        val serviceIntent = Intent(mContext, WearableDataListenerService::class.java).apply {
            putExtra(SharedData.INTENT_TYPE_MSG, typeMsg)
            putExtra(SharedData.INTENT_BODY_MESSAGE, byteArrayData)
        }
        mContext?.startService(serviceIntent)
    }


}