package com.example.comunicationwearmobile.common

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.comunicationwearmobile.models.wearable.WearableDataListenerService
import com.example.shared_library.SharedData
import com.example.shared_library.toByteArray

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
}