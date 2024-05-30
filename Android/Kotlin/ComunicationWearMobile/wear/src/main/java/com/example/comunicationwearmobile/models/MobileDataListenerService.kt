package com.example.comunicationwearmobile.models

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.comunicationwearmobile.viewModels.MobileViewModel
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService


class MobileDataListenerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "onMessageReceived(): $messageEvent")
        Log.d(TAG, String(messageEvent.data))
        if (messageEvent.path == MESSAGE_PATH) {

            val intent = Intent("MobileDataListenerService.MessageReceived")
            intent.putExtra("message", String(messageEvent.data))
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

            Log.d(TAG, "Message received: ${String(messageEvent.data)}")
        }
    }

    companion object{
        private const val TAG = "PhoneListenerService"
        private const val MESSAGE_PATH = "/mensaje"
    }


}

