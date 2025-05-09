package com.example.abumonitor


import android.app.Application
import leakcanary.AppWatcher
import leakcanary.LeakCanary
import leakcanary.ReachabilityWatcher


class AbuMonitorApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Configuración adicional si es necesario
        LeakCanary.config = LeakCanary.config.copy(dumpHeap = false)
    }
}

