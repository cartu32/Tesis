package com.example.abumonitor

import android.app.Application
import com.example.abumonitor.constants.Definition

class AbuMonitorApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Definition.application = this
    }
}