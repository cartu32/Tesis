package com.example.comunicationwearmobile.ui.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
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

    fun verifyPermisson(context: Context , permisson: Array<String>) {
        val permissonNotGranted = permisson.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (permissonNotGranted.isNotEmpty()) {
            _permissionRequest.value = permissonNotGranted
        }
    }

}