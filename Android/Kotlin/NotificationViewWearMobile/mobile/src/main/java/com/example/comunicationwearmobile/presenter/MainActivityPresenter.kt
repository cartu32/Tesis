
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


class MainActivityPresenter(interMainView: InterfaceMainAct):InterfaceMainPre {

    private var interMainView: InterfaceMainAct? = interMainView
    private val mContext: Context? = interMainView as? Context


    init{

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val message = intent?.getStringExtra("message") ?: return

                interMainView.updateTextBox(message)
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

    public fun sendDataWearable(path:SharedData.TypeMsg,msg:String){
        sendMessageToService(path,msg)
    }


    fun onCleared() {
        sendMessageToService(SharedData.TypeMsg.cancel_path,"")
    }

    private fun sendMessageToService(typeMsg: SharedData.TypeMsg, message:String){
        val serviceIntent = Intent(mContext, WearableDataListenerService::class.java).apply {
            putExtra("typeMsg", typeMsg)
            putExtra("message", message)
        }
        mContext?.startService(serviceIntent)
    }


}