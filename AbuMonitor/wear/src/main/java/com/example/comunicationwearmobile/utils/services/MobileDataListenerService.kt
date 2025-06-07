package com.example.comunicationwearmobile.utils.services


import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class MobileDataListenerService : WearableListenerService() {
        private  val TAG = "PhoneListenerService"

        override fun onMessageReceived(messageEvent: MessageEvent) {
            try {
                wakeLock()
                val intent = Intent(SharedData.Broadcast.fromMobileData.name)
                intent.putExtra(SharedData.ParamIntent.MESSAGE_BODY.name, messageEvent.data)
                intent.putExtra(SharedData.ParamIntent.MESSAGE_PATH.name, messageEvent.path)

                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

            } catch (e: Exception) {
                Log.e(TAG, "Error handling incoming message", e)
            }
        }

        private fun wakeLock() {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "MyApp:MyWakelockTag"
            )
            wakeLock.acquire(3000) // Mantiene la pantalla encendida por 3 segundos
        }

        override fun onDestroy() {
            super.onDestroy()
        }
    }
class SenderToMobileService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val path = intent?.getStringExtra(SharedData.ParamIntent.MESSAGE_PATH.name ) ?: return START_NOT_STICKY
        val msg = intent.getByteArrayExtra(SharedData.ParamIntent.MESSAGE_BODY.name ) ?: return START_NOT_STICKY

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
