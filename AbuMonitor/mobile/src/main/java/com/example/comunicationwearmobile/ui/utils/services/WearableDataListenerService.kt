package com.example.comunicationwearmobile.ui.utils.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDispatcherWearable
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

/* clase que se encarga de recibir los mensajes del wearable.
 Es un servicio que se inicia automaticamente en el manifest.xml
 */
class WearableDataListenerService : WearableListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        Log.d(Definition.TAG_DEBUG, "WearableDataListenerService iniciado")
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val context = applicationContext
        serviceScope.launch {
            RepositoryDispatcherWearable.dispatcherMsgFromWearable(context, messageEvent)

            Log.d(Definition.TAG_DEBUG, "onMessageReceived dato: $messageEvent")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(Definition.TAG_DEBUG, "WearableDataListenerService destruido")
    }
}
class SenderToWearableService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val path = intent?.getStringExtra(Definition.PATH_SEND_DATA_TO_WEARABLE) ?: return START_NOT_STICKY
        val msg = intent.getByteArrayExtra(Definition.MSG_TO_WEARABLE) ?: return START_NOT_STICKY

        Log.d("SendToWearableService", "Service iniciado")

        scope.launch {
            try {
                withContext(Dispatchers.IO){
                    sendDataToWearable(applicationContext, path, msg)
                }
            } finally {
                stopSelf()
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private suspend fun sendDataToWearable(context: Context, path: String, msg: ByteArray) {
        try {
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()

            if (nodes.isEmpty()) {
                Log.e(Definition.TAG_DEBUG, "No hay dispositivos Wear OS conectados.")
                return
            }

            for (node in nodes) {
                Log.d("Wearable", "Nodo ID: ${node.id}, Nombre: ${node.displayName}, Cerca: ${node.isNearby}")

                if (!node.isNearby) continue

                if (node.displayName.contains("Watch", ignoreCase = true) ||
                    node.displayName.contains("Wear", ignoreCase = true)) {

                    Wearable.getMessageClient(context).sendMessage(node.id, path, msg).await()
                    Log.d(Definition.TAG_DEBUG, "Mensaje enviado correctamente a ${node.displayName}")
                }
            }
        } catch (e: Exception) {
            if (e is CancellationException) {
                Log.w(Definition.TAG_DEBUG, "Cancelación después del envío (ignorable)", e)
            } else {
                Log.e(Definition.TAG_DEBUG, "Error al enviar mensaje", e)
            }
        }
    }



    override fun onDestroy() {
        scope.cancel()
        Log.d("SendToWearableService", "Service destruido")
        super.onDestroy()
    }
}