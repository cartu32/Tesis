package com.example.comunicationwearmobile.ui.viewmodel

import android.app.Application
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.maps.AreaGeofence
import com.example.comunicationwearmobile.ui.utils.Tools
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ViewmodelMapsActivity(application: Application): AndroidViewModel(application) {

    private var lastIdArea=0

    private val _updateCircleRadius = MutableLiveData<Float>()
    val updateCircleRadius: LiveData<Float> = _updateCircleRadius

    private val areaCircleDataList = mutableListOf<AreaGeofence>()

    fun determineColor(): Int {
        var color = Color.RED
        return color
    }

    fun updateCircleRadius(radius:Float){
        _updateCircleRadius.value = radius
    }

    fun onDestroyed() {
        //por si uso alguna corutina la cancelo
        viewModelScope.cancel()

        //limpio el livedata
        _updateCircleRadius.value = 0f
    }

    fun insertListAreaCircle(circle: Circle , latLng: LatLng ) {
        val obj = AreaGeofence()

        obj.idArea = lastIdArea.toString()
        obj.radius = _updateCircleRadius.value?:0f
        obj.latitud = latLng.latitude
        obj.longitud = latLng.longitude
        obj.circle = circle

        areaCircleDataList.add(obj)

        lastIdArea++

        Log.d(Definition.TAG_DEBUG,"Se agrego al listado el area: $obj")
    }

    fun determineWithinAnyCircle(latLng: LatLng){

        viewModelScope.launch {
            areaCircleDataList.forEach{areaData ->
                //convirto la latitud y longitud LatLng
                val locationAreaCenter= LatLng(areaData.latitud,areaData.longitud)

                if(Tools.isPointInsideCircle(latLng,locationAreaCenter,areaData.radius)){
                    Log.d(Definition.TAG_DEBUG,"Esta dentro del area: ${areaData.idArea}")
                }
                else{
                    Log.d(Definition.TAG_DEBUG,"No se enceuntra en el area: ${areaData.idArea}")
                }
            }
        }
    }

    fun deleteCircle(){

    }
}