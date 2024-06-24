
package com.example.comunicationwearmobile.presenter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.comunicationwearmobile.common.InterfaceMainAct
import com.example.comunicationwearmobile.models.WearableDataListenerService
import com.example.shared_library.SharedData
import com.example.shared_library.fromByteArray
import com.example.shared_library.toByteArray


class MainActivityPresenter(interMainView: InterfaceMainAct) {

    private var interMainView: InterfaceMainAct? = interMainView
    private val mContext: Context? = interMainView as? Context


    init{

        val receiver =createBroadcastReceiver()

        LocalBroadcastManager.getInstance(mContext!!).registerReceiver(
            receiver, IntentFilter(SharedData.Broadcast.fromWearData.name)
        )

        val serviceIntent = Intent(mContext, WearableDataListenerService::class.java)
        mContext.startService(serviceIntent)
    }

    // Función para crear el BroadcastReceiver
    private fun createBroadcastReceiver(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val path:String = intent?.getStringExtra(SharedData.ParamIntent.MESSAGE_PATH.name)!!

                val msgBytes: ByteArray = intent.getByteArrayExtra(SharedData.ParamIntent.MESSAGE_BODY.name)!!


                when(path){
                    SharedData.PATH_VIEWED_NOTIFICATION -> onDataReceived(msgBytes)
                }
            }
        }
    }

     fun onDataReceived(msgBytes: ByteArray) {
         val indexList:Int = fromByteArray(msgBytes)

         interMainView?.showToast(indexList.toString())
        interMainView?.updateTextBox(indexList.toString())
    }

    public fun sendDataWearable(msg:SharedData.MsgNotification){

        sendMessageToService(SharedData.PATH_ADD_NOTIFICATION,msg)
    }


    private fun sendMessageToService(path: String, msgAlert:SharedData.MsgNotification){
        val byteArrayData:ByteArray =toByteArray(msgAlert)
        val serviceIntent = Intent(mContext, WearableDataListenerService::class.java).apply {
            putExtra(SharedData.ParamIntent.MESSAGE_PATH.name,path)
            putExtra(SharedData.ParamIntent.MESSAGE_BODY.name, byteArrayData)
        }
        mContext?.startService(serviceIntent)
    }


}
