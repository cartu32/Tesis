package com.example.comunicationwearmobile.ui.model.repository

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.dto.LastLocationCache
import com.example.comunicationwearmobile.ui.utils.Helpers.Notification.NotificationHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.Notification.SmsHelper
import com.example.comunicationwearmobile.ui.utils.services.SenderToWearableService
import com.example.shared_library.SharedData
import com.example.shared_library.toByteArray
import com.google.android.gms.wearable.MessageEvent

object RepositoryDispatcherWearable {

     suspend fun dispatcherMsgFromWearable(context: Context, messageEvent: MessageEvent) {
        val notificationManager = NotificationHelper.getInstance(context.applicationContext)

        notificationManager.let {
            when (messageEvent.path) {
                SharedData.PATH_VIEWED_NOTIFICATION ->notificationManager?.notificationViewedOnWearable(messageEvent.data)
                SharedData.PATH_FALL_DETECTION_SMS -> sendSMSFalldetection(context, messageEvent.data)
                SharedData.PATH_FALL_CONTINUE_SMS -> SmsHelper.sendSMSFallContinue(context, messageEvent.data)
                else -> Log.d(Definition.TAG_DEBUG, "Unknown path received: ${messageEvent.path}")
            }
        }?: run {
            Log.d(Definition.TAG_DEBUG, "NotificationHelper is null")
        }
         Log.d(Definition.TAG_DEBUG,"termina dispatcher")

     }

    private fun sendSMSFalldetection(context: Context, msg: ByteArray) {
        var lastLatitude    = "0.0"
        var lastLongitude   = "0.0"
        //obtengo la ultima ubicacion leida en geofenceservice
        //al ser atomico no se usa mutex
        val cached = LastLocationCache.last

        //me fijo si la ubicacion que se leyo en startLocationUpdates() es actual.
        // O sea si se obtuve en un intervalo de tiempo menor a Definition.TIME_FRESH_LAST_LOCATION_MS
        val isFresh = (cached != null) &&
                      (System.currentTimeMillis() - cached.timeMs) < (Definition.TIME_FRESH_LAST_LOCATION_MS)

        if (isFresh){
            lastLatitude=cached?.lat.toString()
            lastLongitude=cached?.lon.toString()
        }

        SmsHelper.sendSMSFallDetection(
            context,
            msg,
            lastLatitude,
            lastLongitude
        )
    }


    inline fun <reified T> sendDataToWearable(context: Context, path: String, data: T) {
         val byteArrayData: ByteArray = toByteArray(data)

         val intent=Intent(context, SenderToWearableService::class.java)

         intent.putExtra(Definition.PATH_SEND_DATA_TO_WEARABLE,path)
         intent.putExtra(Definition.MSG_TO_WEARABLE,byteArrayData)
         context.startService(intent)
     }

}

