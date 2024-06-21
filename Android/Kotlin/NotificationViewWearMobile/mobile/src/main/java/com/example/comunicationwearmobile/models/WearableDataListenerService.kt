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


    private val TAG: String = "WearableDataListnener"
    private var transcriptionNodeId: String? = null
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val typeMsg = it.getStringExtra(SharedData.ParamIntent.INTERNAL_OPERATION.name)
            val message = it.getByteArrayExtra(SharedData.ParamIntent.MESSAGE_BODY.name)

            if (message == null || typeMsg == null) {
                return START_STICKY
            }
            when (typeMsg) {
                SharedData.InternalOperationType.sendMessageDevice.name -> {
                    sendDataMobile(typeMsg, message)
                }

                SharedData.InternalOperationType.cancelCoroutines.name -> onCleared()
            }
        }
        return START_STICKY
    }


    private fun getNodes(): Collection<String> {
        return Tasks.await(Wearable.getNodeClient(applicationContext).connectedNodes).map { it.id }
    }


    private fun sendDataMobile(path: String, msg: ByteArray) {
        scope.launch() {
            transcriptionNodeId = getNodes().first().also { nodeId ->
                val sendTask: Task<*> = Wearable.getMessageClient(applicationContext).sendMessage(
                    nodeId,
                    path,
                    msg //send your desired information here
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

        val intent = Intent(SharedData.Broadcast.fromWearData.name)
        intent.putExtra(SharedData.ParamIntent.MESSAGE_BODY.name, messageEvent.data)

        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }
}


