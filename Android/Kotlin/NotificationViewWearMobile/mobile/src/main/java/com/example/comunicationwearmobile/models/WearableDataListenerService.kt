package com.example.comunicationwearmobile.models

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.shared_library.SharedData
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class WearableDataListenerService : WearableListenerService() {


    private val TAG: String="WearableDataListnener"
    private var transcriptionNodeId: String? = null
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val typeMsg = it.getStringExtra("typeMsg")
            val message = it.getStringExtra("message")

            if (typeMsg == null || message == null)
                return  START_STICKY
           /* if (typeMsg==SharedData.TypeMsg.cancel_path)
                onCleared()*/
            else
                sendDataMobile(typeMsg, message)
        }
        return START_STICKY
    }



    private fun getNodes(): Collection<String> {
        return Tasks.await(Wearable.getNodeClient(applicationContext).connectedNodes).map { it.id }
    }


    private fun sendDataMobile(path:String, msg:String)
    {
        scope.launch() {
            transcriptionNodeId = getNodes().first().also { nodeId ->
                val sendTask: Task<*> = Wearable.getMessageClient(applicationContext).sendMessage(
                    nodeId,
                    path,
                    msg.toByteArray() //send your desired information here
                ).apply {
                    addOnSuccessListener { Log.d(TAG, "OnSuccess") }
                    addOnFailureListener { Log.d(TAG, "OnFailure") }
                }
            }

        }
    }

    private fun onCleared() {
        job.cancel() // Cancela todas las coroutines cuando ya no sean necesarias
    }
    override fun onMessageReceived(messageEvent: MessageEvent) {

        if (messageEvent.path == SharedData.msg_wear_to_mobile) {

            val intent = Intent(SharedData.broadcast_wear_data)
            intent.putExtra("message", String(messageEvent.data))
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

        }
    }



}