package com.example.comunicationwearmobile.viewModels

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.comunicationwearmobile.models.MobileMsgModel

class MobileViewModel(application: Application) : AndroidViewModel(application) {
    public val mobileMsgModel = MutableLiveData(MobileMsgModel())

    init {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val message = intent?.getStringExtra("message") ?: return
                setMessage(message)
            }
        }

        LocalBroadcastManager.getInstance(application).registerReceiver(
            receiver, IntentFilter("MobileDataListenerService.MessageReceived")
        )
    }
    fun setMessage(msg: String) {
        val dataMobile = mobileMsgModel.value ?: return
        // Modificar la propiedad edad
        dataMobile.message = msg

        mobileMsgModel.postValue(dataMobile)
    }

}