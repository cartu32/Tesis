package com.example.abumonitor


import android.app.Application
import com.example.shared_library.SharedData


class AbuMonitorApplicationWearable : Application() {
    val persistentMessage = mutableListOf<SharedData.MsgNotification>()
    var persistentStateConfigInitiated:Boolean=false

    override fun onCreate() {
        super.onCreate()
    }

}

