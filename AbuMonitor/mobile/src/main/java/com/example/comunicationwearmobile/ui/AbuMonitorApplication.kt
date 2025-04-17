package com.example.abumonitor


import android.app.Application
import leakcanary.LeakCanary


class AbuMonitorApplication: Application() {
    private val ACTIVATE_LEAK_CANARY:Boolean= false

    override fun onCreate() {
        super.onCreate()

        // Modifica la configuración de LeakCanary para evitar heap dumps automáticos
        LeakCanary.config = LeakCanary.config.copy(dumpHeap = ACTIVATE_LEAK_CANARY)
    }

}