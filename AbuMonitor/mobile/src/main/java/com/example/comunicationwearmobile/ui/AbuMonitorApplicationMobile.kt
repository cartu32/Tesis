package com.example.abumonitor


import android.app.Application
//import leakcanary.LeakCanary


class AbuMonitorApplicationMobile : Application() {
    override fun onCreate() {
        super.onCreate()

        // Configuración adicional si es necesario
     //   LeakCanary.config = LeakCanary.config.copy(dumpHeap = false)
    }

}

