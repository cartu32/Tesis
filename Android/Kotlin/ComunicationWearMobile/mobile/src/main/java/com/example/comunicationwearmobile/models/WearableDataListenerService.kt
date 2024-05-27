package com.example.comunicationwearmobile.models

import android.content.Context
import android.util.Log
import com.example.comunicationwearmobile.ui.MainActivity
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class WearableDataListenerService(context: Context) : WearableListenerService() {

    private var mContext:Context = context
    private val TAG: String="MainActivityPresenter"
    private var transcriptionNodeId: String? = null
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private fun getNodes(): Collection<String> {
        return Tasks.await(Wearable.getNodeClient(mContext.applicationContext).connectedNodes).map { it.id }
    }

    fun sendDataWearable(path:String,msg:String)
    {
        scope.launch() {
            transcriptionNodeId = getNodes().first().also { nodeId ->
                val sendTask: Task<*> = Wearable.getMessageClient(mContext.applicationContext).sendMessage(
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

    fun onCleared() {
        job.cancel() // Cancela todas las coroutines cuando ya no sean necesarias
    }

}