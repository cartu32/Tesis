
package com.example.comunicationwearmobile.presenter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.comunicationwearmobile.common.InterfaceMainAct
import com.example.comunicationwearmobile.common.InterfaceMainPre
import com.example.comunicationwearmobile.models.WearableDataListenerService


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

    public fun sendDataWearable(path:String,msg:String){
        val serviceIntent = Intent(mContext, WearableDataListenerService::class.java).apply {
            putExtra("path", path)
            putExtra("message", msg)
        }
        mContext?.startService(serviceIntent)
    }

    fun onCleared() {
        //listenerService?.onCleared()
    }


}