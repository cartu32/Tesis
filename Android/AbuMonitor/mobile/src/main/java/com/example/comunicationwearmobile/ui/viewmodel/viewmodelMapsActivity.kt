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


object ViewModelManager {
    lateinit var sharedViewmodelMapsActivity: ViewmodelMapsActivity
}

class ViewmodelMapsActivity(application: Application): AndroidViewModel(application) {

    //atributo que almacena el Id del la ultima area geofence agregada
    private var lastIdArea=0

    //declaro los livedata
    private var _updateCircleRadius:MutableLiveData<Int>? = MutableLiveData<Int>()
    val updateCircleRadius: LiveData<Int> ?= _updateCircleRadius

    private var _updateCircleColor:MutableLiveData<Int>? = MutableLiveData<Int>()
    val updateCircleColor: LiveData<Int> ?= _updateCircleColor

    private var _confirmAddCircle:MutableLiveData<Boolean>? = MutableLiveData<Boolean>()
    val confirmAddCircle: LiveData<Boolean>? = _confirmAddCircle

    //defino un objeto areaTemporary para ir almacenando temporalmentelos datos de la nueva area
    //que va ir agregando el usuario a traves del mapa. Luego estos datos se agregan al listado
    //de areas en el mapa
    private var areaTemporary:AreaGeofenceInMap?=AreaGeofenceInMap()

    //se crea este listado de areas en el map, para poder mostrar graficamente
    //las areas de geofencing en el mapa
    private var listAreaGeofence:ArrayList<AreaGeofenceInMap>?= ArrayList()

    fun loadConfigInAreaTemporary(dwellTime:Int?){
        //este metodo agrega en el area temporary los campos que fueron ingresados en la
        //activity propertiesGeofenceActivty.
        //Este metodo se llama cuando se apreta en el guardar de esa activity

        areaTemporary?.let {
            with(it) {
                id_area = lastIdArea

                if (dwellTime != null) {
                    dwell_time = dwellTime
                }
            }
        }

    }

    fun loadMarkerInAreaTemporary(marker: Marker) {
        areaTemporary?.marker= marker
    }

    fun loadCircleInAreaTemporary(circle: Circle , latLng: LatLng ) {
        //este metodo agrega en el area temporary el circulo y la latitud y longitud
        //Este metodo se llama cada vez que se agrega un circulo en el mapa, al hacer click
        //sobre el
        areaTemporary?.circle=circle

        areaTemporary?.latitude=latLng.latitude
        areaTemporary?.longitude=latLng.longitude
    }


    fun saveAreaGeofence(event:Int?,priority: Int?,securityZone:Boolean?,dwellTime:Int?,descripcion:String?){

        loadConfigInAreaTemporary(dwellTime)
        saveInListAreaMap()
        saveInDatabase()
    }

    fun confirmInMap() {
        _confirmAddCircle?.value=true
    }

    fun cancelInMap(){
        //como el usuario cancela la opcion se borra el circulo y el marcador en el mapa mapa
        areaTemporary?.marker?.remove()
        areaTemporary?.circle?.remove()
        _confirmAddCircle?.value=false
    }

    private fun saveInDatabase() {
        return
    }

    private fun saveInListAreaMap() {
        //agrego en el area de geofencing creado el areTemporary que se fue creando
        areaTemporary?.let { listAreaGeofence?.add(it) }

        // Reinicia el estado de areaTemporary para el siguiente uso
        areaTemporary = AreaGeofenceInMap()

        lastIdArea++
    }

    fun updateCircleRadius(radius: Int ){

        areaTemporary?.meters=radius

        _updateCircleRadius?.value = radius
    }

    fun updateCircleColor(color:Int){
        //    areaTemporary.id_color=color

        _updateCircleColor?.value=color
    }


    fun determineWithinAnyCircle(latLng: LatLng){

        viewModelScope.launch {
            listAreaGeofence?.forEach{areaData ->
                with(areaData) {
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

    fun onDestroyed() {
        //por si uso alguna corutina la cancelo
        viewModelScope.cancel()

        //limpio el objeto de areTemporary
        areaTemporary=null
        //elimino de la memoria el listado de AreaGeofence
        listAreaGeofence?.clear()
        listAreaGeofence=null

        //limpio el livedata
        _updateCircleRadius = null
        _updateCircleColor = null
        _confirmAddCircle=null

    }



}