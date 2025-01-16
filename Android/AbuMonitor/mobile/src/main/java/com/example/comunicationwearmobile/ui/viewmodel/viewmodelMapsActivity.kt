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
import java.lang.ref.WeakReference


object ViewModelManager {
    lateinit var sharedViewmodelMapsActivity: ViewmodelMapsActivity
}

class ViewmodelMapsActivity(application: Application): AndroidViewModel(application) {

    private var lastIdArea=0

    var areaTemporary=AreaGeofenceInMap()

    private val _updateCircleRadius = MutableLiveData<Int>()
    val updateCircleRadius: LiveData<Int> = _updateCircleRadius

    private val _updateCircleColor = MutableLiveData<Int>()
    val updateCircleColor: LiveData<Int> = _updateCircleColor

    private val _confirmAddCircle = MutableLiveData<Boolean>()
    val confirmAddCircle: LiveData<Boolean> = _confirmAddCircle


    private var listAreaGeofence= ArrayList<AreaGeofenceInMap>()

    fun loadConfigInAreaTemporary(dwellTime:Int){
        //este metodo agrega en el area temporary los campos que fueron ingresados en la
        //activity propertiesGeofenceActivty.
        //Este metodo se llama cuando se apreta en el guardar de esa activity

        with(areaTemporary){
            id_area=lastIdArea
            dwell_time=dwellTime
        }


    }

    fun loadMarkerInAreaTemporary(marker: Marker) {
        areaTemporary.marker= marker
    }

    fun loadCircleInAreaTemporary(circle: Circle , latLng: LatLng ) {
        //este metodo agrega en el area temporary el circulo y la latitud y longitud
        //Este metodo se llama cada vez que se agrega un circulo en el mapa, al hacer click
        //sobre el
        areaTemporary.circle=circle

        areaTemporary.latitude=latLng.latitude
        areaTemporary.longitude=latLng.longitude
    }

    fun updateCircleRadius(radius: Int ){

        areaTemporary.meters=radius

        _updateCircleRadius.value = radius
    }

    fun updateCircleColor(color:Int){
    //    areaTemporary.id_color=color

        _updateCircleColor.value=color
    }

    fun saveAreaGeofence(event:Int,priority: Int,securityZone:Boolean,dwellTime:Int,descripcion:String){

        loadConfigInAreaTemporary(dwellTime)
        saveInListAreaMap()
        saveInDatabase()
    }

    fun confirmInMap() {
        _confirmAddCircle.value=true
    }

    fun cancelInMap(){
        //como el usuario cancela la opcion se borra el circulo y el marcador en el mapa mapa
        areaTemporary.marker.remove()
        areaTemporary.circle.remove()
        _confirmAddCircle.value=false
    }

    private fun saveInDatabase() {
        return
    }

    private fun saveInListAreaMap() {
        //agrego en el area de geofencing creado el areTemporary que se fue creando
        listAreaGeofence.add(areaTemporary)

        // Reinicia el estado de areaTemporary para el siguiente uso
        areaTemporary = AreaGeofenceInMap()

        lastIdArea++
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
            listAreaGeofence.forEach{areaData ->
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


}