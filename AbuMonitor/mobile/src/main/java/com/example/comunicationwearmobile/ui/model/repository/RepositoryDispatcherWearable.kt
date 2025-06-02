package com.example.comunicationwearmobile.ui.model.repository

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.utils.Mannager.NotificationManagerHelper
import com.example.comunicationwearmobile.ui.utils.Mannager.SmsManager
import com.example.comunicationwearmobile.ui.utils.services.SenderToWearableService
import com.example.shared_library.SharedData
import com.example.shared_library.toByteArray
import com.google.android.gms.wearable.MessageEvent

object RepositoryDispatcherWearable {
    private val smsManager = SmsManager()

     suspend fun dispatcherMsgFromWearable(context: Context, messageEvent: MessageEvent) {
        val notificationManager = NotificationManagerHelper.getInstance(context.applicationContext)

        notificationManager.let {
            when (messageEvent.path) {
                SharedData.PATH_VIEWED_NOTIFICATION ->notificationManager?.notificationViewedOnWearable(messageEvent.data)
                SharedData.PATH_FALL_DETECTION_SMS ->smsManager.sendSMS(context, messageEvent.data)
                else -> Log.d(Definition.TAG_DEBUG, "Unknown path received: ${messageEvent.path}")
            }
        }?: run {
            Log.d(Definition.TAG_DEBUG, "NotificationManagerHelper is null")
        }
         Log.d(Definition.TAG_DEBUG,"termina dispatcher")

     }


     inline fun <reified T> sendDataToWearable(context: Context, path: String, data: T) {
         val byteArrayData: ByteArray = toByteArray(data)

         val intent=Intent(context, SenderToWearableService::class.java)

         intent.putExtra(Definition.PATH_SEND_DATA_TO_WEARABLE,path)
         intent.putExtra(Definition.MSG_TO_WEARABLE,byteArrayData)
         context.startService(intent)
     }

}

