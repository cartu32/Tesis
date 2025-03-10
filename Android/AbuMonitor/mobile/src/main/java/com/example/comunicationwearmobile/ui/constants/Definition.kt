package com.example.abumonitor.constants

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application

object Definition {
    //nombre del archivo de la  base de datos Room
    const val DATABASE_NAME= "AbuMonitorDatabase.db"
    //TAG para hacer los logs
    const val TAG_DEBUG    = "ABUMONITOR_DEBUG"

    //Intent que se usa para pasar al viewmodel los datos de la nueva area de gofecne
    // cuando el usuario crea una nueva
    const val INTENT_DATA_NEW_AREA_GEOF = "INTENT_DATA_NEW_AREA_GEOF"
    const val INTENT_STATE_OPERATION = "INTENT_STATE_OPERATION"

    const val BUNDLE_FRAGMENT_RESULT_NEW_AREA = "BUNDLE_FRAGMENT_RESULT_NEW_AREA"

    //Definicion de configuracion del mapa
    const val GEOFENCE_RADIUS_DEFAULT:Double = 100.0

    //ID de la primera notificacion generada
    const val FIRST_NOTIFICATION_ID          = 1

    //ID del Request permisson
    lateinit var application:Application

    //Nombre de los serializables de los intent
    const val  RESOLVABLE_API_EXCEPTION: String = "Resolvable_Api"

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
        Manifest.permission.FOREGROUND_SERVICE_LOCATION,
        Manifest.permission.WAKE_LOCK,
    )



}