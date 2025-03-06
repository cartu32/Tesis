package com.example.comunicationwearmobile.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.datasource.local.AbuMonitorDatabase
import com.example.abumonitor.data.repository.RepositoryAreaGeofence
import com.example.comunicationwearmobile.ui.utils.Mannager.GeofenceManager
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ViewmodelMapsActivity(application: Application): AndroidViewModel(application) {

    private var geofenceManager: GeofenceManager?=null

    private var _showMessage:MutableLiveData<String>? = MutableLiveData<String>()
    val showMessage: LiveData<String>? = _showMessage

    private var repositoryArea: RepositoryAreaGeofence ?=null


    init {
        viewModelScope.launch {
            val database = AbuMonitorDatabase.getDatabase(application, this)
            val daoAreaGeofence = database.entityAreaGeofenceDao()
            val daoJoinAreaGeofence = database.joinAreaGeofence()

            repositoryArea = RepositoryAreaGeofence(daoAreaGeofence, daoJoinAreaGeofence)
            geofenceManager = GeofenceManager(repositoryArea!!)

            Log.d(Definition.TAG_DEBUG,"Base de datos abierta en ViewmodelMapsActivity")
        }

    }

    fun storeInTemporaryArea(latLng: LatLng, circle: Circle?, marker: Marker?) {
        geofenceManager?.storeInTemporaryArea(
            latLng=latLng,
            circle=circle,
            marker=marker
        )
    }

    fun saveGeofenceAreaInBD(itemEvent: Int?, itemPriority: Int?, isSecurityZone: Boolean?, dwellTime: Int?, description: String?, meters: String) {
        var idNewArea:Long?=null

        viewModelScope.launch {
            idNewArea=geofenceManager?.saveGeofenceAreaInBD(
                itemEvent = itemEvent,
                itemPriority = itemPriority,
                isSecurityZone = isSecurityZone,
                dwellTime = dwellTime,
                description = description,
                meters = meters
            )

            if(idNewArea!=null){
                showMessage("Nueva area agreagada")
            }else{
                showMessage("No se pudo agregar el area en la BD")
            }
        }
    }

    fun showMessage(msg:String) {
        //para seguir el patron MVVM no se muestra el Toast desde el viewmodel
        //sino que lo muestra la activity, atreves del observer modificando el livedata
        //_showMessage
        _showMessage?.postValue(msg)
    }

    fun cancelInMap(){
        geofenceManager?.cancelInMap()
        showMessage("Area eliminada")
    }


    fun updateCircleRadius(radius: Double){
        geofenceManager?.updateCircleRadius(radius)
    }
    fun deleteAreaGeofence(latLng: LatLng) {
        viewModelScope.launch {
            val idAreaSelected = geofenceManager?.determineWithinAnyCircle(latLng) ?: return@launch
            val rowEliminated = geofenceManager?.deleteAreaGeofence(idAreaSelected)

            if(rowEliminated==null){
                Log.e(Definition.TAG_DEBUG,"Error rowElminated es null")
                return@launch
            }

            withContext(Dispatchers.Main) {
                if (rowEliminated > 0) {
                    geofenceManager?.deleteAreaInListAreasAndMap(idAreaSelected)
                    showMessage("Área de Geofence eliminada")
                } else {
                    showMessage("No se encontró el ID del área para borrar")
                }
            }
        }
    }


    fun clearGeofenceManagerHelper(){
        viewModelScope.launch {
            geofenceManager?.clear()
            geofenceManager=null
        }
    }

    fun onDestroyed() {
        clearGeofenceManagerHelper()

        repositoryArea=null
        // Limpio el LiveData
        _showMessage = null

        // Finalmente cancelo el scope
        viewModelScope.cancel()
    }


}