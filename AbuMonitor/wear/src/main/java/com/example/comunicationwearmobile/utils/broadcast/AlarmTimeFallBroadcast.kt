package com.example.comunicationwearmobile.utils.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmTimeFallBroadcast:BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {

        //como se cumplio el tiempo de la alarma se notifica al viewmodel
        AlarmTimeFallEventManager.notifyAlarm()
    }
}