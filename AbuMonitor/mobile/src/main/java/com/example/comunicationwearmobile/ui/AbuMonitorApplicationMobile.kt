package com.example.abumonitor


import android.app.Application
import com.example.comunicationwearmobile.ui.utils.Helpers.InitializerApplication

//import leakcanary.LeakCanary


class AbuMonitorApplicationMobile : Application() {
    override fun onCreate() {
        super.onCreate()
        InitializerApplication.init(this)
      }


    override fun onTerminate() {
        super.onTerminate()
        InitializerApplication.onTerminate()
    }

}

