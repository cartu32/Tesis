package com.example.abumonitor.ui.viewmodel

import android.app.Application
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.abumonitor.data.datasource.local.AbuMonitorDatabase
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.data.model.JoinAreaGeofence
import com.example.abumonitor.data.repository.RepositoryAreaGeofence
import kotlinx.coroutines.launch

class ViewmodelAreaGeofence(application: Application):AndroidViewModel(application) {
    lateinit var repositoryArea:RepositoryAreaGeofence

    private val _showMessageView = MutableLiveData<String>()
    val showMessageView: LiveData<String> get() = _showMessageView

    private val _isInitialized = MutableLiveData<Boolean>()
    val isInitialized: LiveData<Boolean> get() = _isInitialized


    val listJoinAreaGeofence: MediatorLiveData<List<JoinAreaGeofence>> = MediatorLiveData()

    init {
        viewModelScope.launch {
            val database = AbuMonitorDatabase.getDatabase(application, this)
            val daoAreaGeofence = database?.entityAreaGeofenceDao()
            val daoJoinAreaGeofence = database?.joinAreaGeofence()
            repositoryArea = RepositoryAreaGeofence(daoAreaGeofence, daoJoinAreaGeofence)

            //aca asocio el list del repository con el observer del main Thread.
            //Esto lo hago mediante el MediatorLiveData.
            //Esto lo hice porque sino no se llegaba a inicializar el listJoin antes de que el
            //MainThread lo use y por eso tiraba error.
            val liveDataFromRepo = repositoryArea.listJoinAreaGeofence
            listJoinAreaGeofence.addSource(liveDataFromRepo) {
                listJoinAreaGeofence.value = it
            }
            _isInitialized.postValue(true)

        }
    }

    private fun showMessage(msg:String){
        _showMessageView.postValue(msg)
    }

    fun insertAreaGeofence(areaGeofence:EntityAreaGeofence){
        try {
            viewModelScope.launch{
                repositoryArea.insertAreaGeonfence(areaGeofence)
                showMessage("Area de Geofence agregada")
            }
        }catch(e:Exception){
            showMessage("Error:No se puedo inserta el area de geofence")
            Log.e(TAG,"Error: No se pudo insertar el area.${e.message}")
        }

    }

    fun deleteAreaGeofence(idArea:Int){
        var rowEliminated:Int
        try {
            viewModelScope.launch {
                rowEliminated= repositoryArea.deleteAreaWithId(idArea)!!
                if (rowEliminated>0)
                    showMessage("Area de Geofence eliminada")
                else
                    showMessage("No se enecontro el Id del area para borrar")
            }
        }catch (e:Exception){
            showMessage("Error:No se pudo eliminar el area")
            Log.e(TAG,"Error: No se pudo eliminar el area.${e.message}")
        }
    }

    fun updateAreaGeofence(entityAreaGeofence: EntityAreaGeofence) {
        var rowModified:Int
        try {
            viewModelScope.launch {
                rowModified= repositoryArea.updateArea(entityAreaGeofence)!!
                if (rowModified>0)
                    showMessage("Area de Geofence modificada")
                else
                    showMessage("No se enecontro el Id del area para modificar")
            }
        }catch (e:Exception){
            showMessage("Error:No se pudo modificar el area")
            Log.e(TAG,"Error: No se pudo modificar el area.${e.message}")
        }
    }


}