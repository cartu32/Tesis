package com.example.comunicationwearmobile.ui.model.repository

import android.content.BroadcastReceiver.PendingResult
import android.content.Context
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.utils.Mannager.NotificationManagerHelper
import com.example.comunicationwearmobile.ui.utils.Mannager.SmsManager
import com.example.comunicationwearmobile.ui.utils.services.SenderWearable
import com.example.shared_library.SharedData
import com.example.shared_library.toByteArray
import com.google.android.gms.wearable.MessageEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

object RepositoryDispatcherWearable {
    private val smsManager = SmsManager()
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

     fun dispatcherMsgFromWearable(context: Context, messageEvent: MessageEvent) {
         scope.launch {
             try {
                 withTimeout(Definition.TIMEOUT_COURTINE_DISPATCH) {
                     dispatcherMsg(context,messageEvent)
                 }
             } catch (e: TimeoutCancellationException) {
                 Log.d(Definition.TAG_DEBUG, "dispatch msg se canceló por timeout")
             } catch (e: Exception) {
                 Log.e(Definition.TAG_DEBUG, "Error al despachar el mensaje", e)
             } finally {
                 Log.d(Definition.TAG_DEBUG, "dispatch Pending intent finalizado")
             }

         }
     }

    suspend fun dispatcherMsg(context: Context, messageEvent: MessageEvent) {
        val notificationManager = NotificationManagerHelper.getInstance(context)

        notificationManager.let {
            when (messageEvent.path) {
                SharedData.PATH_VIEWED_NOTIFICATION ->notificationManager?.notificationViewedOnWearable(messageEvent.data)
                SharedData.PATH_FALL_DETECTION ->smsManager.sendSMS(context, messageEvent.data)
                else -> Log.d(Definition.TAG_DEBUG, "Unknown path received: ${messageEvent.path}")
            }
        }?: run {
            Log.d(Definition.TAG_DEBUG, "NotificationManagerHelper is null")
        }
    }


     inline fun <reified T> sendDataToWearable(context: Context,pendingIntent:PendingResult, path: String, data: T) {
         val byteArrayData: ByteArray = toByteArray(data)

         scope.launch {
             try {
                 withTimeout(Definition.TIMEOUT_COURTINE_DISPATCH) {
                     SenderWearable.sendDataToWearable(context, path, byteArrayData)
                 }
             } catch (e: TimeoutCancellationException) {
                 Log.d(Definition.TAG_DEBUG, "send data Wear se canceló por timeout")
             } catch (e: Exception) {
                 Log.e(Definition.TAG_DEBUG, "Error al enviar mensaje", e)
             } finally {
                 pendingIntent.finish()
                 Log.d(Definition.TAG_DEBUG, "Dispatch Pending intent finalizado")
             }

         }
     }

    fun onCancel(){
        scope.cancel()
    }
}

