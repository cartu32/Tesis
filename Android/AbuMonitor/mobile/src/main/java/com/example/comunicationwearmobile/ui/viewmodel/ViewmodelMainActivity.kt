package com.example.comunicationwearmobile.ui.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.datasource.local.AbuMonitorDatabase
import com.example.abumonitor.data.model.EntityAreaGeofence
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ViewmodelMainActivity(application: Application): AndroidViewModel(application) {

    private var tempAreaGeof:EntityAreaGeofence?=null

    private val _permissionsToRequest = MutableLiveData<List<String>?>()
    val permissionsToRequest: LiveData<List<String>?> = _permissionsToRequest

    private val _backgroundPermissionRequired = MutableLiveData<Boolean?>()
    val backgroundPermissionRequired: LiveData<Boolean?> = _backgroundPermissionRequired

    private val _allPermissionGranted = MutableLiveData<Boolean?>()
    val allPermissionGranted: LiveData<Boolean?> = _allPermissionGranted

    init {
        viewModelScope.launch {
            val database = AbuMonitorDatabase.getDatabase(application.applicationContext, this)

            if(database!=null)
            //se creo e inicializo la base de datos
                Log.d(Definition.TAG_DEBUG,"Geo:Base de datos Inicializada")

            tempAreaGeof=EntityAreaGeofence()
        }
    }


    fun checkPermissions() {
        val context = getApplication<Application>().applicationContext

        val missingPermissions=checkGeneralPermissions(context)
        checkFineLocation(context,missingPermissions)

    }

    private fun checkFineLocation(context: Context,missingPermissions: List<String>) {

        if (missingPermissions.isNotEmpty()) {
            _permissionsToRequest.value = missingPermissions
        } else if (ContextCompat.checkSelfPermission(context , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkBackgroundLocation(context)
        }
    }

    private fun checkBackgroundLocation(context: Context) {
        if(ContextCompat.checkSelfPermission(context , Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //si Backgruond_location esta otorgado, entonces quiere decir que todos los permisos fueron otorgados
            _allPermissionGranted.value=true
        }else{
            //si background_location no esta otorgado, entonces se solicita
            _backgroundPermissionRequired.value = true
        }

    }

    private fun checkGeneralPermissions(context: Context): List<String> {
        val missingPermissions = Definition.permissonNecesary.filter {
            if((it==Manifest.permission.FOREGROUND_SERVICE_LOCATION)&&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE))
                false
            else if(ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED)
                true
            else
                false
        }
        return missingPermissions

    }

    fun onBackgroundPermissionHandled() {
        _backgroundPermissionRequired.value = false
    }


    fun onDestroyed(){
        //por si uso alguna corutina la cancelo
        viewModelScope.cancel()

        //limpio los livedata
        _allPermissionGranted.value = null
        _permissionsToRequest.value = null
        _backgroundPermissionRequired.value = null

        tempAreaGeof=null
    }

}