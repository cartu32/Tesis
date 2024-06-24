
package com.example.comunicationwearmobile.presenter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.comunicationwearmobile.common.InterfaceMainAct
import com.example.comunicationwearmobile.common.InterfaceMainPre
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
                val msgBytes:ByteArray = intent?.getByteArrayExtra(SharedData.ParamIntent.MESSAGE_BODY.name)!!

                val msgAlert:SharedData.MsgViewNotification = fromByteArray(msgBytes)
                interMainView.updateTextBox(msgAlert.numberNotification.toString())
            }

        }

        LocalBroadcastManager.getInstance(mContext!!).registerReceiver(
            receiver, IntentFilter(SharedData.Broadcast.fromWearData.name)
        )

        val serviceIntent = Intent(mContext, WearableDataListenerService::class.java)
        mContext.startService(serviceIntent)
    }
    override fun onDataReceived(data: String) {
        interMainView?.showToast(data)
        interMainView?.updateTextBox(data)
    }

    public fun sendDataWearable(msg:SharedData.MsgNotification){

        sendMessageToService("",msg)
    }


    private fun sendMessageToService(typeMsg: String, msgAlert:SharedData.MsgNotification){
        val byteArrayData:ByteArray =toByteArray(msgAlert)
        val serviceIntent = Intent(mContext, WearableDataListenerService::class.java).apply {
            putExtra(SharedData.ParamIntent.MESSAGE_BODY.name, byteArrayData)
        }
        mContext?.startService(serviceIntent)
    }


}