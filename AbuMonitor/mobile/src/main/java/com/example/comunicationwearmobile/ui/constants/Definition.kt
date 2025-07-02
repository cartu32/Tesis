package com.example.abumonitor.constants

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application

object Definition {
    val ERROR_INDEX_OUT_OF_BOUNDS: Int =-1
    const val ZOOM_MAP: Float = 15f

    //nombre del archivo de la  base de datos Room
    const val DATABASE_NAME= "AbuMonitorDatabase.db"
    //TAG para hacer los logs
    const val TAG_DEBUG    = "ABUMONITOR_DEBUG"

    //Intent que se usa para pasar al viewmodel los datos de la nueva area de gofecne
    // cuando el usuario crea una nueva
    const val INTENT_DATA_NEW_AREA_GEOF = "INTENT_DATA_NEW_AREA_GEOF"
    const val INTENT_STATE_OPERATION = "INTENT_STATE_OPERATION"
    const val INTENT_SMS_MSG = "INTENT_SMS_MSG"

    //constantes que sirve para los intents que se la manda al foregroundservice
    const val OPERATION_START_FOREGROUND_SERVICE = "OPERATION_INTENT"
    const val OPERATION_GOEFENCE_SEND_SMS = "1"

    const val BUNDLE_FRAGMENT_RESULT_NEW_AREA = "BUNDLE_FRAGMENT_RESULT_NEW_AREA"

    //Definicion de configuracion del mapa
    const val GEOFENCE_RADIUS_DEFAULT:Double = 100.0

    //constantes que indican cada cuanto tiempo se lee del gps para mover el
    //mapa
    const val INTERVAL_MILLIS_ACTUALIZATION_POS_GPS:Long=5000
    const val SETUP_UPDATE_INTERVAL_MILLIS:Long = 2000

    //tiempo que se indica para cancelar las cortuinas que estan el dispatcher
    const val TIMEOUT_COURTINE_DISPATCH:Long = 10000
    //ID del Request permisson
    lateinit var application:Application

    //Nombre de los serializables de los intent
    const val  RESOLVABLE_API_EXCEPTION: String = "Resolvable_Api"

    //constantes de errores
    const val ERROR_NULL:Long                = -1
    const val ERROR_INSERT_BD_GEOF:Long      = -2
    const val ERROR_ACTIVATE_GEOF:Long       = -3

    //constantes que indican la prioridad de las notificaciones
    const val PRIORITY_BAJA   = 1
    const val PRIORTY_MEDIA   = 2
    const val PRIORITY_ALTA   = 3



    const val PATH_SEND_DATA_TO_WEARABLE:String = "PATH_SEND_DATA_TO_WEARABLE"
    const val MSG_TO_WEARABLE:String="MSG_TO_WEARABLE"

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

        //backoground location se pide en el viewmodel despues
        //de comprobar de que los permisos generales fueron otorgados
        //sobre todo access_fine_location, ya que es necesario para funcionar
        //Manifest.permission.ACCESS_BACKGROUND_LOCATION,

        )



}