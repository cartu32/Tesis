package com.example.comunicationwearmobile.ui.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.datasource.local.AbuMonitorDatabase
import kotlinx.coroutines.launch

class ViewmodelMainActivity(application: Application): AndroidViewModel(application) {

    private val _permissionsToRequest = MutableLiveData<List<String>>()
    val permissionsToRequest: LiveData<List<String>> = _permissionsToRequest

    private val _backgroundPermissionRequired = MutableLiveData<Boolean>()
    val backgroundPermissionRequired: LiveData<Boolean> = _backgroundPermissionRequired

    init {
        viewModelScope.launch {
            val database = AbuMonitorDatabase.getDatabase(application, this)

            if(database!=null)
            //se creo e inicializo la base de datos
                Log.d(Definition.TAG_DEBUG,"Geo:Base de datos Inicializada")

        }
    }


    fun checkPermissions() {
        val context = getApplication<Application>().applicationContext

        val missingPermissions = Definition.permissonNecesary.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            _permissionsToRequest.value = missingPermissions
        } else if ((ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                   (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)){
            // Solicitar ACCESS_BACKGROUND_LOCATION si ACCESS_FINE_LOCATION ya está otorgado
            _backgroundPermissionRequired.value = true
        }
    }

    fun onBackgroundPermissionHandled() {
        _backgroundPermissionRequired.value = false
    }


    fun onDestroyed(){
        viewModelScope.launch {
            AbuMonitorDatabase.closeDatabase()
        }
    }

}