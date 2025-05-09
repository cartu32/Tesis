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
        RepositoryDispatcherWearable.dispatcherMsgFromWearable(context,messageEvent)

        Log.d(Definition.TAG_DEBUG, "onMessageReceived dato: $messageEvent")
    }

    override fun onDestroy() {
        super.onDestroy()
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
                sendDataToWearable(applicationContext, path, msg)
                Log.d("SendToWearableService", "Mensaje enviado correctamente")
            } catch (e: Exception) {
                Log.e("SendToWearableService", "Error al enviar mensaje", e)
            } finally {
                stopSelf()
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null


    private suspend fun sendDataToWearable(context: Context, path: String, msg: ByteArray) {
        // Obtener la lista de nodos conectados
        val nodes = Wearable.getNodeClient(context).connectedNodes.await()

        // Verificar si hay al menos un nodo conectado
        val node = nodes.firstOrNull()
        if (node == null) {
            throw Exception("No hay dispositivos Wear OS conectados.")
        }

        val nodeId = node.id

        // Enviar el mensaje al nodo encontrado
        Wearable.getMessageClient(context).sendMessage(nodeId, path, msg).await()
    }


    override fun onDestroy() {
        scope.cancel()
        Log.d("SendToWearableService", "Service destruido")
        super.onDestroy()
    }
}