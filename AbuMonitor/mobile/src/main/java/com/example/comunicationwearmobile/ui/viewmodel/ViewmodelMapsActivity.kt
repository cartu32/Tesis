package com.example.comunicationwearmobile.ui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.model.dto.DataAreaGeofAux
import com.example.comunicationwearmobile.ui.model.pojo.AreaGeofenceForMap
import com.example.comunicationwearmobile.ui.model.pojo.JoinAreaGeofence
import com.example.comunicationwearmobile.ui.utils.Helpers.Alarm.AlarmHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.PlayServiceGeofenceStrategyHelper
import com.example.comunicationwearmobile.ui.utils.broadcast.AlarmBroadcastReceiver
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

    private var _isDeleteArea: MutableLiveData<Long?>? = MutableLiveData<Long?>()
    val isDeleteArea: LiveData<Long?>? = _isDeleteArea

    private var _allAreas:MutableLiveData<List<AreaGeofenceForMap>>?=MutableLiveData<List<AreaGeofenceForMap>>()
    val  allAreas: LiveData<List<AreaGeofenceForMap>>? =_allAreas

    private val _areaGeofenceForId = MutableLiveData<JoinAreaGeofence?>()
    val areaGeofenceForId: LiveData<JoinAreaGeofence?> = _areaGeofenceForId

    private var repositoryAreaDB: RepositoryAreaDB ?=null


    init {
        viewModelScope.launch(Dispatchers.IO) {

            withContext(Dispatchers.Main) {

                repositoryAreaDB = RepositoryAreaDB.getInstance(application)

                getListAreasGefence()

                Log.d(Definition.TAG_DEBUG, "Base de datos abierta en ViewmodelMapsActivity")
            }
        }
    }


    private fun getListAreasGefence() {
        viewModelScope.launch(Dispatchers.IO) {
            //obtengfo el listado de la base de datos
            val rawGeofenceList = repositoryAreaDB?.getListAreasForMap()

            //configuro el livedata para la view y le envio el el listado de todas las areas que estan en la bd
            //a mapactivity
            rawGeofenceList?.let { listAllAreasGeof ->
                _allAreas?.postValue(listAllAreasGeof)
            }
        }

    }


    fun insertAreaGeof(context: Context, dataAreaGeofAux: DataAreaGeofAux) {
        if(dataAreaGeofAux.entityAreaGeofence.id_type_area==Definition.TYPE_AREA_ID_SECURITY_ZONE){
            _isSecurityZone?.postValue(true)
        }

        insertAreaGeofComplete(context,dataAreaGeofAux)
    }

    private fun insertAreaGeofComplete(context: Context, dataAreaGeofAux: DataAreaGeofAux) {
        viewModelScope.launch(Dispatchers.IO) {
            val newAreaId = repositoryAreaDB?.insertAreaGeofence(dataAreaGeofAux,true)?: Definition.ERROR_INSERT_BD_GEOF
            var finalId   = newAreaId

            //si se pudo insertar correctamente la nueva area en la base de datos
            if (newAreaId > 0) {

                dataAreaGeofAux.entityAreaGeofence.id_area = newAreaId
                //activo el area de geofence
                val stateActivateGeof =
                    PlayServiceGeofenceStrategyHelper.activateGeofence(context, dataAreaGeofAux)

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
        var dato: JoinAreaGeofence?=null
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





    fun deleteAreaGeof(context: Context,idArea: Long) {
        viewModelScope.launch {
            val error=-1

            val result = repositoryAreaDB?.deleteAreaWithId(idArea)

            if (result!=error)
            {
                //desactivo el area de geofence
                PlayServiceGeofenceStrategyHelper.desactivateGeofence(context,idArea.toString())

                //si es un area dwell time cancelo su alarma
                AlarmHelper.cancelAlarm(
                    context,
                    idArea,
                    Definition.ACTION_ALARM_FOR_DWELL_TIME,
                    AlarmBroadcastReceiver::class.java
                )

                //le aviso a la view que borre el circulo del mapa grafico
                _isDeleteArea?.postValue(idArea)

            }
            else{
                _isDeleteArea?.postValue(null)

            }

        }
    }

    fun showMessage(msg:String) {
        //para seguir el patron MVVM no se muestra el Toast desde el viewmodel
        //sino que lo muestra la activity, atreves del observer modificando el livedata
        //_showMessage
        _showMessage?.postValue(msg)
    }




    fun onDestroyed() {

        repositoryAreaDB=null

        // Limpio el LiveData
        _showMessage = null
        _allAreas=null
        _isDeleteArea=null
        _idNewAreaGeof=null

        // Finalmente cancelo el scope
        viewModelScope.cancel()
    }


}
