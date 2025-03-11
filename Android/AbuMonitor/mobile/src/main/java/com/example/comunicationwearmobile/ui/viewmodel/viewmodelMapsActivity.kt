package com.example.comunicationwearmobile.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.datasource.local.AbuMonitorDatabase
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.data.repository.RepositoryAreaGeofence
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


class ViewmodelMapsActivity(application: Application): AndroidViewModel(application) {

    private var _showMessage:MutableLiveData<String>? = MutableLiveData<String>()
    val showMessage: LiveData<String>? = _showMessage

    private var _idNewAreaGeof:MutableLiveData<Long>? = MutableLiveData<Long>()
    val idNewAreaGeof: LiveData<Long>? = _idNewAreaGeof

    private var _resultDeleteArea: MutableLiveData<Int>? = MutableLiveData<Int>()
    val resultDeleteArea: MutableLiveData<Int>? = _resultDeleteArea

    private var _allAreas:MutableLiveData<List<EntityAreaGeofence>>?=MutableLiveData<List<EntityAreaGeofence>>()
    val  allAreas: LiveData<List<EntityAreaGeofence>>? =_allAreas

    private var repositoryArea: RepositoryAreaGeofence ?=null

    init {
        val database = AbuMonitorDatabase.getDatabase(application, viewModelScope)
        val daoAreaGeofence = database.entityAreaGeofenceDao()
        val daoJoinAreaGeofence = database.joinAreaGeofence()

        repositoryArea = RepositoryAreaGeofence(daoAreaGeofence, daoJoinAreaGeofence)
        //geofenceManager = GeofenceManager(repositoryArea!!)

        getListAreasGefence()

        Log.d(Definition.TAG_DEBUG,"Base de datos abierta en ViewmodelMapsActivity")

    }



    private fun getListAreasGefence() {
        viewModelScope.launch {
            //obtengfo el listado de la base de datos
            val rawGeofenceList = repositoryArea?.getListAllAreas()

            //configuro el livedata para la view y le envio el el listado de todas las areas que estan en la bd
            //a mapactivity
            rawGeofenceList?.let { listAllAreasGeof ->
                _allAreas?.postValue(listAllAreasGeof)
            }
        }

    }

    fun insertAreaInBD(areaGeofence: EntityAreaGeofence){

        viewModelScope.launch {
            val error=-1L
            val newId:Long?=repositoryArea?.insertAreaGeofence(areaGeofence)
            //si newId es null postvalue envia error, si no envia el newid
            _idNewAreaGeof?.postValue(newId?:error)
       }
    }

    fun deleteAreaInBD(idArea:Long){
        viewModelScope.launch {
            val error=-1
            val result=repositoryArea?.deleteAreaWithId(idArea)

            _resultDeleteArea?.postValue(result?:error)
        }
    }
    fun showMessage(msg:String) {
        //para seguir el patron MVVM no se muestra el Toast desde el viewmodel
        //sino que lo muestra la activity, atreves del observer modificando el livedata
        //_showMessage
        _showMessage?.postValue(msg)
    }

    fun onDestroyed() {

        repositoryArea=null

        // Limpio el LiveData
        _showMessage = null
        _allAreas=null
        _resultDeleteArea=null
        _idNewAreaGeof=null

        // Finalmente cancelo el scope
        viewModelScope.cancel()
    }


}