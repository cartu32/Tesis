package com.example.comunicationwearmobile.viewModels

import android.content.Intent
import android.util.Log
import com.example.comunicationwearmobile.MainActivity
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService


class MobileDataListenerService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "onMessageReceived(): $messageEvent")
        Log.d(TAG, String(messageEvent.data))
        if (messageEvent.path == MESSAGE_PATH) {
           val startIntent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("MessageData", messageEvent.data)
            }
            startActivity(startIntent)

            Log.d(TAG, "Message received: ${String(messageEvent.data)}")
        }
    }

    companion object{
        private const val TAG = "PhoneListenerService"
        private const val MESSAGE_PATH = "/deploy"
    }

    private fun processReceivedMessage(message: String) {
        // Aquí puedes agregar el código para procesar el mensaje recibido
        Log.d(TAG, "Processing message: $message")
    }

}
