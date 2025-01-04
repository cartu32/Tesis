package com.example.abumonitor.constants

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import com.example.abumonitor.ui.viewmodel.ViewModelFactory
import com.example.abumonitor.ui.viewmodel.ViewmodelAreaGeofence
import com.example.comunicationwearmobile.ui.ui.viewmodel.ViewmodelMainActivity
import retrofit2.http.Tag

object Definition {
    const val DATABASE_NAME= "AbuMonitorDatabase.db"
    const val TAG_DEBUG    = "ABUMONITOR_DEBUG"
    lateinit var application:Application


    @SuppressLint("InlinedApi")
    val permissonNecesary = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_PHONE_STATE
    )

    val factory: ViewModelFactory by lazy {
        ViewModelFactory(
            mapOf(
                ViewmodelAreaGeofence::class.java to { ViewmodelAreaGeofence(application) },
                ViewmodelMainActivity::class.java to { ViewmodelMainActivity(application) },
            )
        )
    }


}