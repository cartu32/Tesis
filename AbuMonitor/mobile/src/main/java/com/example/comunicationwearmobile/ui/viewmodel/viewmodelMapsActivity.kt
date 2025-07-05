package com.example.comunicationwearmobile.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.data.model.JoinAreaGeofence
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.model.dto.DataAreaGeofAux
import com.example.comunicationwearmobile.ui.model.repository.RepositoryGeofActivate
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ViewmodelMapsActivity(application: Application): AndroidViewModel(application) {

    private var _showMessage:MutableLiveData<String>? = MutableLiveData<String>()
    val showMessage: LiveData<String>? = _showMessage

    private var _idNewAreaGeof:MutableLiveData<Long>? = MutableLiveData<Long>()
    val idNewAreaGeof: LiveData<Long>? = _idNewAreaGeof

    private var _isSecurityZone:MutableLiveData<Boolean>? = MutableLiveData<Boolean>()
    val securityZone: LiveData<Boolean>? = _isSecurityZone

    private var _resultDeleteArea: MutableLiveData<Circle?>? = MutableLiveData<Circle?>()
    val resultDeleteArea: LiveData<Circle?>? = _resultDeleteArea

    private var _allAreas:MutableLiveData<List<EntityAreaGeofence>>?=MutableLiveData<List<EntityAreaGeofence>>()
    val  allAreas: LiveData<List<EntityAreaGeofence>>? =_allAreas

    private val _areaGeofenceForId = MutableLiveData<JoinAreaGeofence?>()
    val areaGeofenceForId: LiveData<JoinAreaGeofence?> = _areaGeofenceForId

    private var repositoryAreaDB: RepositoryAreaDB ?=null
    private var repositoryGeofActivate: RepositoryGeofActivate ?=null

    private var circlesMap = mutableMapOf<Long?, Circle?>()

    init {
        viewModelScope.launch(Dispatchers.IO) {

            withContext(Dispatchers.Main) {

                repositoryAreaDB = RepositoryAreaDB.getInstance(application.applicationContext, viewModelScope)
                repositoryGeofActivate = RepositoryGeofActivate()

                getListAreasGefence()

                Log.d(Definition.TAG_DEBUG, "Base de datos abierta en ViewmodelMapsActivity")
            }
        }
    }


    private fun getListAreasGefence() {
        viewModelScope.launch(Dispatchers.IO) {
            //obtengfo el listado de la base de datos
            val rawGeofenceList = repositoryAreaDB?.getListAllAreas()

            //configuro el livedata para la view y le envio el el listado de todas las areas que estan en la bd
            //a mapactivity
            rawGeofenceList?.let { listAllAreasGeof ->
                _allAreas?.postValue(listAllAreasGeof)
            }
        }

    }

    fun insertAreaGeof(context: Context, dataAreaGeofAux: DataAreaGeofAux) {
        if(dataAreaGeofAux.entityAreaGeofence.security_zone){
            _isSecurityZone?.postValue(true)
        }

        insertAreaGeofComplete(context,dataAreaGeofAux)
    }

    private fun insertAreaGeofComplete(context: Context, dataAreaGeofAux: DataAreaGeofAux) {
        viewModelScope.launch(Dispatchers.IO) {
            val newAreaId = repositoryAreaDB?.insertAreaGeofence(dataAreaGeofAux)?: Definition.ERROR_INSERT_BD_GEOF
            var finalId   = newAreaId

            //si se pudo insertar correctamente la nueva area en la base de datos
            if (newAreaId > 0) {

                dataAreaGeofAux.entityAreaGeofence.id_area = newAreaId
                //activo el area de geofence
                val stateActivateGeof = repositoryGeofActivate?.activateGeofence(context, dataAreaGeofAux) == true

                // Si falla, eliminamos el registro de la base de datos
                if (!stateActivateGeof) {
                    repositoryAreaDB?.deleteAreaWithId(newAreaId)
                    finalId = Definition.ERROR_ACTIVATE_GEOF
                }
            }

            // Publicamos el resultado
            _idNewAreaGeof?.postValue(finalId)
        }
    }


    fun getAreaGeofWithId(idArea: Long){
        var dato:JoinAreaGeofence?=null
        try{
            viewModelScope.launch {

                //para que el observer del livedata detecte el cambio,
                //se cambia de valor a null y luego se vuelve a asignar
                //esto se hace asi por si llama a la funcion dos veces seguidas
                //con el mismo idArea.
                _areaGeofenceForId.postValue(null)


                dato=repositoryAreaDB?.getJoinAreaGeofence(idArea)

                if (dato!=null)
                {
                    //aca se envia el dato verdadero al obsever del livedata
                    _areaGeofenceForId.postValue(dato)
                    Log.d(
                        Definition.TAG_DEBUG,
                        "Lat:${dato?.areaGeofence?.latitude} Long:${dato?.areaGeofence?.latitude}"
                    )
                }else
                    showMessage("No se encontro el Id del Area")
            }

        }catch (e:Exception){
            showMessage("Error:No se pudo buscar el area")
            Log.e(Definition.TAG_DEBUG,"Error: No se pudo buscar el area.${e.message}")
        }
    }



    fun createCircle(latLng: LatLng, radius: Double, securityZone: Boolean): CircleOptions {
        val alpha = 100
        val circleBackground: Int
        val circleBorder=Color.BLACK

        circleBackground = if (!securityZone) {
            Color.BLUE
        } else {
            Color.GREEN
        }

        val circleOptions = CircleOptions()
                .center(latLng)
                .strokeColor(circleBorder)
                .fillColor(ColorUtils.setAlphaComponent(circleBackground, alpha))
                .radius(radius)
                .strokeWidth(4f)
                .clickable(true)
        return circleOptions
    }

    fun deleteAreaGeof(context: Context,idArea: Long) {
        viewModelScope.launch {
            var circleToDeleteinGraphic:Circle?=null
            val error=-1

            val result = repositoryAreaDB?.deleteAreaWithId(idArea)

            if (result!=error)
            {
                //desactivo el area de geofence
                repositoryGeofActivate?.desactivateGeofence(context,idArea.toString())

                //elimino el area del listado de circulos
                circleToDeleteinGraphic= circlesMap[idArea]
                circlesMap.remove(idArea)

                //le aviso a la view que borre el circulo del mapa grafico
                _resultDeleteArea?.postValue(circleToDeleteinGraphic)

            }
            else{
                _resultDeleteArea?.postValue(null)

            }

        }
    }

    fun showMessage(msg:String) {
        //para seguir el patron MVVM no se muestra el Toast desde el viewmodel
        //sino que lo muestra la activity, atreves del observer modificando el livedata
        //_showMessage
        _showMessage?.postValue(msg)
    }

    fun addCircleInList(circleWithId:Circle){
        val id:Long

        id= circleWithId.tag as Long
        circlesMap[id]=circleWithId
    }

    private fun removeAllCircle(){
        circlesMap.values.forEach{it?.remove()} // Elimina los círculos del mapa
        circlesMap.clear() // Limpia todas las referencias del Map

    }

    fun extractDataNewAreaOfIntent(data: Bundle): DataAreaGeofAux? {
        //Recibo los datos desde la activty PropertiesGeofence Activty
        val dataNewAreaGeof = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            data.getParcelable<DataAreaGeofAux>(Definition.INTENT_DATA_NEW_AREA_GEOF,DataAreaGeofAux::class.java)
        } else {
            data.getParcelable<DataAreaGeofAux>(Definition.INTENT_DATA_NEW_AREA_GEOF)
        }
        return dataNewAreaGeof
    }

    fun onDestroyed() {

        removeAllCircle()
        repositoryAreaDB=null
        repositoryGeofActivate=null

        // Limpio el LiveData
        _showMessage = null
        _allAreas=null
        _resultDeleteArea=null
        _idNewAreaGeof=null

        // Finalmente cancelo el scope
        viewModelScope.cancel()
    }


}