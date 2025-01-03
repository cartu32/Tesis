package com.example.comunicationwearmobile.ui.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.datasource.local.AbuMonitorDatabase
import com.example.abumonitor.data.repository.RepositoryAreaGeofence
import kotlinx.coroutines.launch

class ViewmodelMainActivity(application: Application): AndroidViewModel(application) {

    lateinit var repositoryArea: RepositoryAreaGeofence
    val TAG = "ViewmodelMainActivity"

    init {
        viewModelScope.launch {
            val database = AbuMonitorDatabase.getDatabase(application, this)

            if(database!=null)
                //se creo e inicializo la base de datos
                Log.d(Definition.TAG_DEBUG,"Geo:Base de datos Inicializada")

        }
    }

}