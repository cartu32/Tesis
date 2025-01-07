package com.example.abumonitor.constants

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import com.example.abumonitor.ui.viewmodel.ViewModelFactory
import com.example.abumonitor.ui.viewmodel.ViewmodelAreaGeofence
import com.example.comunicationwearmobile.ui.viewmodel.ViewmodelMainActivity
import retrofit2.http.Tag

object Definition {
    //nombre del archivo de la  base de datos Room
    const val DATABASE_NAME= "AbuMonitorDatabase.db"
    //TAG para hacer los logs
    const val TAG_DEBUG    = "ABUMONITOR_DEBUG"

    //constantes que indican cada cuanto se deben actualizar la señal del gps
    const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 15 //metros
    const val MIN_TIME_BW_UPDATES: Long = (1000 * 30 ).toLong()

    //ID de la primera notificacion generada
    const val FIRST_NOTIFICATION_ID          = 1

    //ID del Request permisson
    const val REQUEST_CODE_GENERAL_PERMISSON = 1023
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
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.FOREGROUND_SERVICE,
        //Manifest.permission.FOREGROUND_SERVICE_LOCATION,
        Manifest.permission.WAKE_LOCK,
        //Manifest.permission.ACCESS_BACKGROUND_LOCATION

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