package com.example.comunicationwearmobile.common

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.comunicationwearmobile.models.wearable.WearableDataListenerService
import com.example.comunicationwearmobile.presenter.notificationWear.helper.NotificationPresenter
import com.example.shared_library.SharedData
import com.example.shared_library.toByteArray
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object Utils
{
    fun showToast(activity: ComponentActivity, s: String) {
        Toast.makeText(activity, s, Toast.LENGTH_SHORT).show()
    }

    inline fun<reified T> sendMessageToService(mContext:Context,path: String , data:T){
        val byteArrayData:ByteArray = toByteArray(data)
        val serviceIntent = Intent(mContext, WearableDataListenerService::class.java).apply {
            putExtra(SharedData.ParamIntent.MESSAGE_PATH.name,path)
            putExtra(SharedData.ParamIntent.MESSAGE_BODY.name, byteArrayData)
        }
        mContext.startService(serviceIntent)
    }

    fun sendMessageToWearable(mContext: Context,title:String,message:String, typeNotification:SharedData.TypeNotification){
        val msgNotification:SharedData.MsgNotification
        val notification= NotificationPresenter.getInstance(mContext)

        var formatter = DateTimeFormatter.ofPattern("HH:mm")
        val currentTime = LocalTime.now().format(formatter)

        formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val currentDay = LocalDate.now().format(formatter)

        msgNotification = SharedData.MsgNotification(
            title = title,
            message = message,
            typeNotification = typeNotification,
            date = currentDay,
            hour = currentTime
        )
        notification.showNotification(msgNotification)
        sendMessageToService(mContext ,SharedData.PATH_ADD_NOTIFICATION,msgNotification)


    }
}