package com.example.comunicationwearmobile.presenter

import androidx.activity.ComponentActivity
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

class MainActivityPresenter(activity: ComponentActivity) {
    private val activity: ComponentActivity = activity

    fun sendDataWear() {
        val putDataMapReq = PutDataMapRequest.create("/data_path")
        putDataMapReq.dataMap.putString("key", "valor")
        val putDataReq = putDataMapReq.asPutDataRequest()
        Wearable.getDataClient(activity.applicationContext).putDataItem(putDataReq)
    }

}