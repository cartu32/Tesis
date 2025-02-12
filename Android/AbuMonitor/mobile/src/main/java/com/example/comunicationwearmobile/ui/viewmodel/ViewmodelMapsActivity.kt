package com.example.comunicationwearmobile.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.maps.AreaGeofenceInMap
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


class ViewmodelMapsActivity(private var application: Application): AndroidViewModel(application) {

    //atributo que almacena el Id del la ultima area geofence agregada
    private var lastIdArea=0

    private var _showMessage:MutableLiveData<String>? = MutableLiveData<String>()
    var showMessage: LiveData<String>? = _showMessage

    //defino un objeto areaTemporary para ir almacenando temporalmentelos datos de la nueva area
    //que va ir agregando el usuario a traves del mapa. Luego estos datos se agregan al listado
    //de areas en el mapa
    private var areaTemporary:AreaGeofenceInMap?=AreaGeofenceInMap()

    //se crea este listado de areas en el map, para poder mostrar graficamente
    //las areas de geofencing en el mapa
    private var listAreaGeofence:ArrayList<AreaGeofenceInMap>?= ArrayList()

    fun loadAreaTemporary(latLng: LatLng, circle: Circle?) {

        if (circle != null)  {
            areaTemporary?.circle = circle
            areaTemporary?.latLng = latLng

        }else{
            Log.e(Definition.TAG_DEBUG,"Error: loadAreaTemporary is null")
        }
    }


    private fun removeAllMarkersAndCircles() {
        listAreaGeofence?.forEach { areaData ->
            areaData.circle?.remove()
            areaData.latLng=null
        }

    }

    override fun onCleared() {
        super.onCleared()


        areaTemporary?.circle=null
        areaTemporary?.latLng=null
        areaTemporary=null

        // Limpio los recursos antes de cancelar el scope
        removeAllMarkersAndCircles()

        //limpio el listado de area geofncing
        listAreaGeofence?.clear()
        listAreaGeofence = null

        // Limpio el LiveData
        _showMessage = null
        showMessage =null

        // Finalmente cancelo el scope
        viewModelScope.cancel()
    }


}