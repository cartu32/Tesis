package com.example.comunicationwearmobile.ui.model.repository

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
import kotlinx.coroutines.launch

object RepositoryDispatcherWearable {
    private val smsManager = SmsManager()

    fun dispatcherMsgFromWearable(context: Context, messageEvent: MessageEvent) {
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

    inline fun <reified T> sendDataToWearable(context: Context, path: String, data: T) {
        CoroutineScope(Dispatchers.IO).launch {
            val byteArrayData:ByteArray = toByteArray(data)
            SenderWearable.sendDataToWearable(context, path, byteArrayData)
        }
    }

}

