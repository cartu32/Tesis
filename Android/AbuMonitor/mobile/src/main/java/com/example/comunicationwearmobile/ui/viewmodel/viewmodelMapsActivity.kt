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
import com.example.comunicationwearmobile.ui.model.maps.AreaGeofenceInMap
import com.example.comunicationwearmobile.ui.utils.Tools
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.sql.Time


class ViewmodelMapsActivity(application: Application): AndroidViewModel(application) {

    //atributo que almacena el Id del la ultima area geofence agregada
    private var lastIdArea=0

    private var _showMessage:MutableLiveData<String>? = MutableLiveData<String>()
    val showMessage: LiveData<String>? = _showMessage

    //defino un objeto areaTemporary para ir almacenando temporalmentelos datos de la nueva area
    //que va ir agregando el usuario a traves del mapa. Luego estos datos se agregan al listado
    //de areas en el mapa
    private var areaTemporary:AreaGeofenceInMap?=AreaGeofenceInMap()

    private var repositoryArea: RepositoryAreaGeofence ?=null

    //se crea este listado de areas en el map, para poder mostrar graficamente
    //las areas de geofencing en el mapa
    private var listAreaGeofence:ArrayList<AreaGeofenceInMap>?= ArrayList()


    init {
        viewModelScope.launch {
            val database = AbuMonitorDatabase.getDatabase(application, this)
            val daoAreaGeofence = database.entityAreaGeofenceDao()
            val daoJoinAreaGeofence = database.joinAreaGeofence()

            repositoryArea = RepositoryAreaGeofence(daoAreaGeofence, daoJoinAreaGeofence)
            Log.d(Definition.TAG_DEBUG,"Base de datos abierta en ViewmodelMapsActivity")
        }

    }

    fun storeInTemporaryArea(latLng: LatLng , marker: Marker? , circle: Circle?) {

        if (marker != null && circle != null)  {
            areaTemporary?.marker = marker
            areaTemporary?.circle = circle
            areaTemporary?.latLng = latLng

        }else{
            Log.e(Definition.TAG_DEBUG,"Error: storeInTemporaryArea is null")
        }
    }

    fun saveGeofenceAreaInBD(itemEvent: Int?, itemPriority: Int?, isSecurityZone: Boolean?, dwellTime: Int?, description: String?, meters: String) {
        //itemColor y idContact por el momento los dejo harcodeado
        val itemColor=1
        val idContact=null

        var entityAreaGeofence:EntityAreaGeofence?=null

        storeNewAreaInListAreas()
        entityAreaGeofence=createObjEntityAreaGeofence(
            itemEvent,
            itemPriority,
            isSecurityZone,
            dwellTime,
            description,
            meters,
            idContact,
            itemColor
        )
        saveInDatabase(entityAreaGeofence)

    }

    private fun createObjEntityAreaGeofence(itemEvent: Int?, itemPriority: Int?, isSecurityZone: Boolean?, dwellTime: Int?, description: String?, meters: String,idContact:Int?, itemColor: Int?): EntityAreaGeofence? {

        val idTypeAreaGeofence:Int = 1

        val lastArea = listAreaGeofence?.lastOrNull() ?: return null

        return if (isSecurityZone != null && dwellTime != null && description !=null
                   && itemEvent!=null &&  itemPriority != null && itemColor != null) {
            EntityAreaGeofence(
                latitude = lastArea.latLng?.latitude?.toString() ?: "0",
                longitude = lastArea.latLng?.longitude?.toString() ?: "0",
                meters= meters.toIntOrNull() ?: 0,
                security_zone = isSecurityZone,
                dwell_time = dwellTime,
                description = description,
                id_color = itemColor,
                id_event =  itemEvent,
                id_priority = itemPriority,
                id_contact = idContact,
                id_type_area= idTypeAreaGeofence
            )
        } else {
            null
        }
    }

    fun showMessage(msg:String) {
        //para seguir el patron MVVM no se muestra el Toast desde el viewmodel
        //sino que lo muestra la activity, atreves del observer modificando el livedata
        //_showMessage
        _showMessage?.value=msg
    }

    fun cancelInMap(){
        //como el usuario cancela la opcion se borra el circulo y el marcador en el mapa mapa
        areaTemporary?.marker?.remove()
        areaTemporary?.circle?.remove()

        _showMessage?.value="Area Cancelada"
    }

    private fun saveInDatabase(entityAreaGeofence: EntityAreaGeofence?) {
        try {
/*            val entityAreaGeofence=EntityAreaGeofence(
                latitude = (-34.681680).toString(),
                longitude = (-58.554367).toString(),
                meters = 100,
                id_type_area = 1,
                id_color = 1,
                id_event = 2,
                security_zone = false,
                dwell_time = 10,
                id_contact = 2,
                id_priority = 1
            )*/
            viewModelScope.launch{
                if (entityAreaGeofence != null) {

                    repositoryArea?.insertAreaGeonfence(entityAreaGeofence)
                    showMessage("Area agregada")
                }else{
                    Log.e(Definition.TAG_DEBUG,"EntityAreaGeofence es null")
                }
            }
        }catch(e:Exception){
            showMessage("Error:No se puedo inserta el area de geofence")
            Log.e(Definition.TAG_DEBUG,"Error: No se pudo insertar el area.${e.message}")
        }

        return
    }

    private fun storeNewAreaInListAreas() {
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

            listAreaGeofence?.forEach { areaData ->
                val circleRadius = areaData.circle?.radius?.toFloat()
                val areaLatLng = areaData.latLng

                if (circleRadius != null && areaLatLng != null) {
                    // Realizar la verificación si los valores necesarios no son nulos
                    val isInside = Tools.isPointInsideCircle(latLng, areaLatLng, circleRadius)

                    if (isInside) {
                        Log.d(Definition.TAG_DEBUG, "Está dentro del área: ${areaData.id_area}")
                    } else {
                        Log.d(Definition.TAG_DEBUG, "No se encuentra en el área: ${areaData.id_area}")
                    }
                } else {
                    Log.w(Definition.TAG_DEBUG, "Datos incompletos para evaluar el área: ${areaData.id_area}")
                }
            }
        }
    }
    private fun removeAllMarkersAndCircles() {
        viewModelScope.launch {
            listAreaGeofence?.forEach { areaData ->
                areaData.circle?.remove()
                areaData.marker?.remove()
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

        repositoryArea=null

        // Limpio el LiveData
        _showMessage = null

        // Finalmente cancelo el scope
        viewModelScope.cancel()
    }


}