package com.example.abumonitor


import android.app.Application
import android.util.Log
import com.example.abumonitor.constants.Definition


class AbuMonitorApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Definition.application = this

        val maxMemory = Runtime.getRuntime().maxMemory()
        val maxMemoryMB = maxMemory / (1024 * 1024)

        Log.d(Definition.TAG_DEBUG , "Límite de memoria Heap: " + maxMemoryMB + "MB")
    }
}