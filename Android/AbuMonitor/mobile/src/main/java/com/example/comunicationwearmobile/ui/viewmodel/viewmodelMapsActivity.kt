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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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

    fun storeInTemporaryArea(latLng: LatLng , circle: Circle?, marker: Marker? ) {

        if (marker != null && circle != null)  {
            areaTemporary?.marker = marker
            areaTemporary?.radius = circle.radius
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

        var idNewArea:Long?=null
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

        viewModelScope.launch {
            idNewArea=saveInDatabase(entityAreaGeofence)
            storeNewIdAreaInListAreas(idNewArea)
        }
    }


    private fun storeNewIdAreaInListAreas(idNewArea: Long?) {
        if (idNewArea != null) {
            listAreaGeofence?.last()?.id_area=idNewArea
        }else{
            Log.e(Definition.TAG_DEBUG,"Error idNewArea is null")
        }
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

    private suspend  fun saveInDatabase(entityAreaGeofence: EntityAreaGeofence?): Long? {
        var lastIdArea:Long?=null
        try {
                if (entityAreaGeofence != null) {
                    withContext(Dispatchers.IO) {
                        lastIdArea = repositoryArea?.insertAreaGeonfence(entityAreaGeofence)
                    }
                    showMessage("Area agregada")
                }else{
                    Log.e(Definition.TAG_DEBUG,"EntityAreaGeofence es null")
                }
        }catch(e:Exception){
            showMessage("Error:No se puedo inserta el area de geofence")
            Log.e(Definition.TAG_DEBUG,"Error: No se pudo insertar el area.${e.message}")
        }

        return lastIdArea
    }

    private fun storeNewAreaInListAreas() {
        //agrego en la lista el area geofen que se fue llenando anterirormente en areaTemporary
        areaTemporary?.let {
            //le asigno el id al area
            listAreaGeofence?.add(it)
        }

        // Reinicia el estado de areaTemporary para el siguiente uso
        areaTemporary = AreaGeofenceInMap()
    }

    fun updateCircleRadius(radius: Int ){

       areaTemporary?.circle?.radius=radius.toDouble()
       areaTemporary?.radius =radius.toDouble()
    }

    fun updateCircleColor(color:Int){
       //areaTemporary?.circle.strokeColor(color)
    }

    fun deleteAreaGeofence(latLng: LatLng){
        var idAreaSelected:Long?=null
        var rowEliminated:Int?=null
        val msg:String=""

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                //determino cual es el id del area seleccionada
                idAreaSelected = determineWithinAnyCircle(latLng)
                //lo borro en la base de datos
                rowEliminated=repositoryArea?.deleteAreaWithId(idAreaSelected)
                if(rowEliminated!!>0){

                }

            }
            withContext(Dispatchers.Main) {
                if (rowEliminated!! > 0) {
                    deleteAreaInListAreasAndMap(idAreaSelected)
                    showMessage("Area de Geofence eliminada")
                }
                else{
                    showMessage("No se enecontro el Id del area para borrar")
                    }
            }
        }
    }

    private fun deleteAreaInListAreasAndMap(idAreaSelected: Long?) {
        listAreaGeofence?.find { it.id_area == idAreaSelected }?.let { areaData ->
            areaData.circle?.remove()
            areaData.marker?.remove()
            listAreaGeofence?.remove(areaData)
        }
    }

    fun determineWithinAnyCircle(latLng: LatLng): Long? {
        var idAreaSlected:Long?=null

         listAreaGeofence?.forEach { areaData ->
            val circleRadius = areaData.radius
            val areaLatLng = areaData.latLng

            if (circleRadius != null && areaLatLng != null) {
                // Realizar la verificación si los valores necesarios no son nulos
                val isInside = Tools.isPointInsideCircle(latLng, areaLatLng, circleRadius)

                if (isInside) {
                    idAreaSlected=areaData.id_area
                    Log.d(Definition.TAG_DEBUG, "Está dentro del área: ${areaData.id_area}")
                } else {
                    Log.d(Definition.TAG_DEBUG, "No se encuentra en el área: ${areaData.id_area}")
                }
            } else {
                Log.w(Definition.TAG_DEBUG, "Datos incompletos para evaluar el área: ${areaData.id_area}")
            }
        }

        return idAreaSlected
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