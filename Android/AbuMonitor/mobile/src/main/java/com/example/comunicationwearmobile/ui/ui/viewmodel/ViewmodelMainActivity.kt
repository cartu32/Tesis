package com.example.comunicationwearmobile.ui.ui.viewmodel

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
import com.example.abumonitor.data.repository.RepositoryAreaGeofence
import kotlinx.coroutines.launch

class ViewmodelMainActivity(application: Application): AndroidViewModel(application) {

    private val _permissionRequest = MutableLiveData<List<String>>()
    val permissionRequest: LiveData<List<String>> = _permissionRequest

    init {
        viewModelScope.launch {
            val database = AbuMonitorDatabase.getDatabase(application, this)

            if(database!=null)
                //se creo e inicializo la base de datos
                Log.d(Definition.TAG_DEBUG,"Geo:Base de datos Inicializada")

        }
    }

    fun verifyPermission(context: Context, permissions: Array<String>) {
        viewModelScope.launch {
            val permissionsNotGranted = permissions.filter { permission ->
                // Verifica si el permiso es POST_NOTIFICATIONS y si la versión del SDK es menor a 33
                if (permission == Manifest.permission.POST_NOTIFICATIONS && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    false // No es necesario verificar este permiso en versiones anteriores
                } else {
                    ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
                }
            }
            if (permissionsNotGranted.isNotEmpty()) {
                _permissionRequest.postValue(permissionsNotGranted)
            }
        }
    }

    fun onDestroyed(){
        viewModelScope.launch {
            AbuMonitorDatabase.closeDatabase()
        }
    }
}