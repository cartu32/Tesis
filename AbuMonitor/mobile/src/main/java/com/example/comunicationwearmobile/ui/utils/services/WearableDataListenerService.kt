package com.example.comunicationwearmobile.ui.utils.services

import android.content.Context
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDispatcherWearable
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/* clase que se encarga de recibir los mensajes del wearable.
 Es un servicio que se inicia automaticamente en el manifest.xml
 */
class WearableDataListenerService : WearableListenerService() {

    override fun onCreate() {
        super.onCreate()
        Log.d(Definition.TAG_DEBUG, "WearableDataListenerService iniciado")
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val context=applicationContext

        //por cada mensaje recibido creo una corutina para que lo maneje.
        //Esta corutina la hice independiente del service por que se desturuye el service
        //siga estando la corutina sino termino de enviar el mensaje o hacer algo.
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(Definition.TAG_DEBUG, "onMessageReceived dato: $messageEvent")
            RepositoryDispatcherWearable.dispatcherMsgFromWearable(context, messageEvent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(Definition.TAG_DEBUG, "WearableDataListenerService destruido")
    }
}

/* clase que se encarga de enviar los mensajes al wearable.
 */
object SenderWearable {

    private val mutex = Mutex()

    suspend fun sendDataToWearable(context: Context, path: String, msg: ByteArray) {
        try {
            withContext(Dispatchers.IO) {
                mutex.withLock {
                    val applicationContext = context.applicationContext
                    val nodes = getNodes(applicationContext)
                    val nodeId = nodes.firstOrNull()

                    nodeId?.let {
                        Wearable.getMessageClient(applicationContext)
                            .sendMessage(it, path, msg)
                            .addOnSuccessListener {
                                Log.d(Definition.TAG_DEBUG, "OnSuccess")
                            }
                            .addOnFailureListener {
                                Log.d(Definition.TAG_DEBUG, "OnFailure")
                            }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(Definition.TAG_DEBUG, "Error sending message", e)
        }
    }

    private suspend fun getNodes(context: Context): List<String> = withContext(Dispatchers.IO) {
        val task = Wearable.getNodeClient(context).connectedNodes
        Tasks.await(task).map { it.id }
    }
}
