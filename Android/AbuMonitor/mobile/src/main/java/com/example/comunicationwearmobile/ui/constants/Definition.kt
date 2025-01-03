package com.example.abumonitor.constants

import android.app.Application
import com.example.abumonitor.ui.viewmodel.ViewModelFactory
import com.example.abumonitor.ui.viewmodel.ViewmodelAreaGeofence

object Definition {
    const val DATABASE_NAME="AbuMonitorDatabase.db"
    lateinit var application:Application

    val factory: ViewModelFactory by lazy {
        ViewModelFactory(
            mapOf(
                ViewmodelAreaGeofence::class.java to { ViewmodelAreaGeofence(application) },
            )
        )
    }


}