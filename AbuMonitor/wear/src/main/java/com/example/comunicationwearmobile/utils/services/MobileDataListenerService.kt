package com.example.comunicationwearmobile.utils.services

import android.content.Context
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
                sendDataToWearable(applicationContext,path, message)
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




        private fun sendDataToWearable(context: Context, path: String, msg: ByteArray) {
            Wearable.getNodeClient(context).connectedNodes
                .addOnSuccessListener { nodes ->
                    val nodeId = nodes.firstOrNull()?.id
                    nodeId?.let {
                        Wearable.getMessageClient(context)
                            .sendMessage(it, path, msg)
                            .addOnSuccessListener {
                                Log.d("SendToWearableService", "OnSuccess")
                            }
                            .addOnFailureListener { e ->
                                Log.d("SendToWearableService", "OnFailure", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("SendToWearableService", "Error al obtener los nodos disponibles", e)
                }
        }


        override fun onDestroy() {
            super.onDestroy()
            scope.cancel()
        }
    }

