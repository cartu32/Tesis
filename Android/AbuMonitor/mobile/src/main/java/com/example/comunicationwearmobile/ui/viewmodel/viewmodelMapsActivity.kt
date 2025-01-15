package com.example.comunicationwearmobile.ui.viewmodel

import android.app.Application
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.comunicationwearmobile.ui.model.maps.AreaGeofence
import com.example.comunicationwearmobile.ui.utils.Tools
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

object ViewModelManager {
    lateinit var sharedViewmodelMapsActivity: ViewmodelMapsActivity
}

class ViewmodelMapsActivity(application: Application): AndroidViewModel(application) {

    private var lastIdArea=0

    var areaTemporary:AreaGeofence=AreaGeofence()

    private val _updateCircleRadius = MutableLiveData<Int>()
    val updateCircleRadius: LiveData<Int> = _updateCircleRadius

    private val _updateCircleColor = MutableLiveData<Int>()
    val updateCircleColor: LiveData<Int> = _updateCircleColor

    private val _confirmAddCircle = MutableLiveData<Boolean>()
    val confirmAddCircle: LiveData<Boolean> = _confirmAddCircle

//    private val areaCircleDataList = mutableListOf<AreaGeofence>()
    private var areaCircleDataList = ArrayList<AreaGeofence>()

    init {
        areaTemporary.entityArea= EntityAreaGeofence()
    }

    fun loadAreaTemporaryConfig(event:Int,priority: Int,securityZone:Boolean,dwellTime:Int,descripcion:String){
        //este metodo agrega en el area temporary los campos que fueron ingresados en la
        //activity propertiesGeofenceActivty.
        //Este metodo se llama cuando se apreta en el guardar de esa activity

        with(areaTemporary.entityArea){
            id_area=lastIdArea
            id_event=event
            id_priority=priority
            description=descripcion
            dwell_time=dwellTime
            security_zone=securityZone
        }

        lastIdArea++

    }

    fun loadAreaTemporaryCircle(circle: Circle , latLng: LatLng ) {
        //este metodo agrega en el area temporary el circulo y la latitud y longitud
        //Este metodo se llama cada vez que se agrega un circulo en el mapa, al hacer click
        //sobre el
        areaTemporary.circle=circle

        areaTemporary.entityArea.latitude=latLng.latitude
        areaTemporary.entityArea.longitude=latLng.longitude
    }

    fun updateCircleRadius(radius: Int ){

        areaTemporary.entityArea.meters=radius

        _updateCircleRadius.value = radius
    }

    fun updateCircleColor(color:Int){
        areaTemporary.entityArea.id_color=color

        _updateCircleColor.value=color
    }

    fun saveAreaGeofence(event:Int,priority: Int,securityZone:Boolean,dwellTime:Int,descripcion:String){

        loadAreaTemporaryConfig(event,priority,securityZone,dwellTime,descripcion)
        saveInListArea()
        saveInDatabase()
        confirmInMap()
    }

    private fun confirmInMap() {
        _confirmAddCircle.value=true
    }

    private fun saveInDatabase() {
        return
    }

    private fun saveInListArea() {
        //guardo primero el id del area
        areaTemporary.entityArea.id_area=lastIdArea

        areaCircleDataList.add(areaTemporary)
    }

    fun onDestroyed() {
        //por si uso alguna corutina la cancelo
        viewModelScope.cancel()

        //limpio el livedata
        _updateCircleRadius.value = 0
        _updateCircleColor.value = 0
    }


    fun determineWithinAnyCircle(latLng: LatLng){

        viewModelScope.launch {
            areaCircleDataList.forEach{areaData ->
                with(areaData.entityArea) {
                    //convirto la latitud y longitud LatLng
                    val locationAreaCenter = LatLng(latitude , longitude)

                    if (Tools.isPointInsideCircle(latLng , locationAreaCenter , meters.toFloat())) {
                        Log.d(Definition.TAG_DEBUG , "Esta dentro del area: $id_area")
                    } else {
                        Log.d(Definition.TAG_DEBUG , "No se enceuntra en el area: $id_area")
                    }
                }
            }
        }
    }

    fun deleteCircle(){

    }
}