package com.example.comunicationwearmobile.models

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.shared_library.SharedData
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
            val path    = it.getStringExtra(SharedData.ParamIntent.MESSAGE_PATH.name)
            val message = it.getByteArrayExtra(SharedData.ParamIntent.MESSAGE_BODY.name)

            if ((message == null)||(path==null)) {
                return START_STICKY
            }
            sendDataToWearable(path, message)
           }
        return START_STICKY
    }


    private fun getNodes(): Collection<String> {
        return Tasks.await(Wearable.getNodeClient(applicationContext).connectedNodes).map { it.id }
    }

    private fun sendDataToWearable(path: String, msg: ByteArray) {
        scope.launch {
            try {
                transcriptionNodeId = getNodes().firstOrNull()
                transcriptionNodeId?.let { nodeId ->
                    Wearable.getMessageClient(applicationContext).sendMessage(nodeId, path, msg).apply {
                        addOnSuccessListener { Log.d(TAG, "OnSuccess") }
                        addOnFailureListener { Log.d(TAG, "OnFailure") }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message", e)
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {

        val intent = Intent(SharedData.Broadcast.fromWearData.name)

        intent.putExtra(SharedData.ParamIntent.MESSAGE_BODY.name, messageEvent.data)
        intent.putExtra(SharedData.ParamIntent.MESSAGE_PATH.name, messageEvent.path)

        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}


