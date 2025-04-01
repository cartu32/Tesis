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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

    class MobileDataListenerService : WearableListenerService() {
        private  val TAG = "PhoneListenerService"


        private var transcriptionNodeId: String? = null
            get() {
                synchronized(this) {
                    return field
                }
            }
            set(value) {
                synchronized(this) {
                    field = value
                }
            }


        private val job = Job()
        private val scope = CoroutineScope(Dispatchers.IO + job)

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

            intent?.let {
                val path    = it.getStringExtra(SharedData.ParamIntent.MESSAGE_PATH.name)
                val message = it.getByteArrayExtra(SharedData.ParamIntent.MESSAGE_BODY.name)

                if ((message == null)||(path==null)) {
                    return START_STICKY
                }
                sendDataToMobile(path, message)
            }
            return START_STICKY
        }

        override fun onMessageReceived(messageEvent: MessageEvent) {
            try {
                val intent = Intent(SharedData.Broadcast.fromMobileData.name)
                intent.putExtra(SharedData.ParamIntent.MESSAGE_BODY.name, messageEvent.data)
                intent.putExtra(SharedData.ParamIntent.MESSAGE_PATH.name, messageEvent.path)

                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error handling incoming message", e)
            }
        }


        private fun getNodes(): Collection<String> {
            return Tasks.await(Wearable.getNodeClient(applicationContext).connectedNodes).map { it.id }
        }

        private fun sendDataToMobile(path: String, msg: ByteArray) {
            scope.launch {
                try {
                    transcriptionNodeId = getNodes().firstOrNull()
                    transcriptionNodeId?.let { nodeId ->
                        val sendMessageTask = Wearable.getMessageClient(applicationContext).sendMessage(nodeId, path, msg)
                        Tasks.await(sendMessageTask) // Espera sincrónica para asegurar que el mensaje se envíe
                        Log.d(TAG, "Message sent successfully to node: $nodeId")
                    } ?: run {
                        Log.e(TAG, "No connected nodes found")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending message", e)
                }
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            scope.cancel()
        }
    }

