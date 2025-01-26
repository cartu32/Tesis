package com.example.comunicationwearmobile.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.maps.AreaGeofenceInMap
import com.example.comunicationwearmobile.ui.utils.Tools
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


class ViewmodelMapsActivity(application: Application): AndroidViewModel(application) {

    //atributo que almacena el Id del la ultima area geofence agregada
    private var lastIdArea=0

    private var _showMessage:MutableLiveData<String>? = MutableLiveData<String>()
    val showMessage: LiveData<String>? = _showMessage

    //defino un objeto areaTemporary para ir almacenando temporalmentelos datos de la nueva area
    //que va ir agregando el usuario a traves del mapa. Luego estos datos se agregan al listado
    //de areas en el mapa
    private var areaTemporary:AreaGeofenceInMap?=AreaGeofenceInMap()

    //se crea este listado de areas en el map, para poder mostrar graficamente
    //las areas de geofencing en el mapa
    private var listAreaGeofence:ArrayList<AreaGeofenceInMap>?= ArrayList()

    fun loadAreaTemporary(latLng: LatLng , marker: Marker? , circle: Circle?) {

        if (marker != null && circle != null)  {
            areaTemporary?.marker = marker
            areaTemporary?.circle = circle
            areaTemporary?.latLng = latLng

        }else{
            Log.e(Definition.TAG_DEBUG,"Error: loadAreaTemporary is null")
        }
    }

    fun saveAreaGeofence(
        itemEvent: Int? ,
        itemPriority: Int? ,
        isSecurityZone: Boolean? ,
        dwellTime: Int? ,
        description: String?
    ) {


        saveInListAreaMap()
        saveInDatabase()

        confirmInMap()
    }

    fun confirmInMap() {
        _showMessage?.value="Area Agregada"
    }

    fun cancelInMap(){
        //como el usuario cancela la opcion se borra el circulo y el marcador en el mapa mapa
        areaTemporary?.marker?.remove()
        areaTemporary?.circle?.remove()

        _showMessage?.value="Area Cancelada"
    }

    private fun saveInDatabase() {
        return
    }

    private fun saveInListAreaMap() {
        //agrego en la lista el area geofen que se fue llenando anterirormente en areaTemporary
        areaTemporary?.let {
            //le asigno el id al area
            it.id_area=lastIdArea
            listAreaGeofence?.add(it)
        }

        // Reinicia el estado de areaTemporary para el siguiente uso
        areaTemporary = AreaGeofenceInMap()

        //creo el siguiente id para una nueva area
        lastIdArea++
    }

    fun updateCircleRadius(radius: Int ){

       areaTemporary?.circle?.radius=radius.toDouble()
    }

    fun updateCircleColor(color:Int){
       //areaTemporary?.circle.strokeColor(color)
    }


    fun determineWithinAnyCircle(latLng: LatLng){

        viewModelScope.launch {
            var radius:Float=0f

            listAreaGeofence?.forEach{areaData ->
                with(areaData) {
                    radius= areaData.circle.radius.toFloat()

                    if (Tools.isPointInsideCircle(latLng , areaData.latLng ,radius)) {
                        Log.d(Definition.TAG_DEBUG , "Esta dentro del area: $id_area")
                    } else {
                        Log.d(Definition.TAG_DEBUG , "No se enceuntra en el area: $id_area")
                    }
                }
            }
        }
    }
    private fun removeAllMarkersAndCircles() {
        viewModelScope.launch {
            listAreaGeofence?.forEach { areaData ->
                areaData.circle.remove()
                areaData.marker.remove()
            }
        }
    }

    fun onDestroyed() {
        // Limpio los recursos antes de cancelar el scope
        removeAllMarkersAndCircles()

        //limpio el listado de area geofncing
        listAreaGeofence?.clear()
        listAreaGeofence = null
        areaTemporary=null

        // Limpio el LiveData
        _showMessage = null

        // Finalmente cancelo el scope
        viewModelScope.cancel()
    }


}