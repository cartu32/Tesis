package com.example.comunicationwearmobile.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.data.model.EntityScheduledAssistance
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.GeofenceActivatorHelper
import com.example.comunicationwearmobile.ui.model.repository.RepositoryScheduleAssistance
import kotlinx.coroutines.launch

class AssistanceDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repoAssistance = RepositoryScheduleAssistance(application)
    private val repositoryAreaDB = RepositoryAreaDB(application)

    private val _assistanceDetail = MutableLiveData<EntityScheduledAssistance>()
    val assistanceDetail: LiveData<EntityScheduledAssistance> get() = _assistanceDetail

    private val _areaGeofenceData = MutableLiveData<EntityAreaGeofence?>()
    val areaGeofenceData: LiveData<EntityAreaGeofence?> get() = _areaGeofenceData


    fun loadAssistanceDetail(id: Int) {
        viewModelScope.launch {
            val result = repoAssistance.getAssistanceWithId(id)
            _assistanceDetail.postValue(result)
        }
    }

    fun deleteAreaById(context: Context, idArea: Long, onComplete: (result:Boolean) -> Unit) {
        viewModelScope.launch {
            repositoryAreaDB.deleteAreaWithId(idArea)
            val error=-1
            val result = repositoryAreaDB.deleteAreaWithId(idArea)

            if (result!=error) {
                //desactivo el area de geofence
                GeofenceActivatorHelper.desactivateGeofence(context, idArea.toString())

                //si se pudo eliminar la area de geofence le aviso a a la activity
                //enviandole true como parametro de la funcion callback onComplete
                onComplete(true)
            }
            else{
                //si se pudo eliminar la area de geofence le aviso a a la activity
                //enviandole false como parametro de la funcion callback onComplete
                  onComplete(false)
            }
        }
    }

    fun getAreaGeofenceById(id: Long) {
        viewModelScope.launch {
            val result = repositoryAreaDB.getAreaGeofenceById(id)
            _areaGeofenceData.postValue(result)
        }
    }
}
