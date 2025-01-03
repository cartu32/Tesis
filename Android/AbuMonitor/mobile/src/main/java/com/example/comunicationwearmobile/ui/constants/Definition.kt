package com.example.abumonitor.constants

import android.app.Application
import com.example.abumonitor.ui.viewmodel.ViewModelFactory
import com.example.abumonitor.ui.viewmodel.ViewmodelAreaGeofence
import com.example.comunicationwearmobile.ui.ui.viewmodel.ViewmodelMainActivity
import retrofit2.http.Tag

object Definition {
    const val DATABASE_NAME= "AbuMonitorDatabase.db"
    const val TAG_DEBUG    = "ABUMONITOR_DEBUG"
    lateinit var application:Application

    val factory: ViewModelFactory by lazy {
        ViewModelFactory(
            mapOf(
                ViewmodelAreaGeofence::class.java to { ViewmodelAreaGeofence(application) },
                ViewmodelMainActivity::class.java to { ViewmodelMainActivity(application) },
            )
        )
    }


}