
package com.example.comunicationwearmobile.presenter

import android.util.Log
import androidx.activity.ComponentActivity
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable


class MainActivityPresenter(activity: ComponentActivity) {
    private val activity: ComponentActivity = activity

    fun sendDataWear() {
        val putDataMapReq = PutDataMapRequest.create("/data_path")
        putDataMapReq.dataMap.putString("key", "valor")
        val putDataReq = putDataMapReq.asPutDataRequest()
        Wearable.getDataClient(activity).putDataItem(putDataReq)
    }

    fun sendDataToWearable(path: String?, key: String?, value: String?) {
        val dataClient = Wearable.getDataClient(activity)

        val putDataMapRequest = PutDataMapRequest.create(path!!)
        val dataMap = putDataMapRequest.dataMap
        dataMap.putString(key!!, value!!)

        val request = putDataMapRequest.asPutDataRequest()
        val dataItemTask = dataClient.putDataItem(request)
        dataItemTask.addOnSuccessListener { dataItem ->
            Log.d(
                "TAG",
                "Data sent successfully: " + dataItem.uri
            )
        }
    }

}