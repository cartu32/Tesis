package com.example.comunicationwearmobile.ui.viewmodel


import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.comunicationwearmobile.ui.model.repository.RepositoryLocation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ViewmodelLocation(application: Application) : AndroidViewModel(application) {

    private var repositoryLocation: RepositoryLocation? = null
    private var _locationLiveData: MutableLiveData<Location?> = MutableLiveData()
    val locationLiveData: LiveData<Location?> = _locationLiveData

    private var locationObserver: ((Location?) -> Unit)? = null // Guardamos el observer

    init {
        repositoryLocation = RepositoryLocation.getInstance(application)
    }

    fun startTracking() {
        //definicion de la funcion lambda locationobserver
        locationObserver = { location ->
            _locationLiveData.postValue(location)
        }

        viewModelScope.launch {
            //cuando dentro de la clase Repository se hace un postvalue de _locationLiveData.
            //entonces cuando eso pasa lo detecta el observer y automaticamente invoca a la funcion
            //lamda locatiobserver

            repositoryLocation?.locationLiveData?.observeForever(locationObserver!!)

            repositoryLocation?.startLocationUpdates()
        }
    }

    fun stopTracking() {
        //  Eliminar observador
        locationObserver?.let { observer ->
            repositoryLocation?.locationLiveData?.removeObserver(observer)
        }
    }

    fun checkStatusGPS() {
        repositoryLocation?.checkStatusGPS()
    }


    override fun onCleared() {
        super.onCleared()
        stopTracking() // Detenemos las actualizaciones

        _locationLiveData = MutableLiveData() // Limpieza de LiveData
        repositoryLocation = null // Liberar memoria
        locationObserver=null

        viewModelScope.cancel() // Cancelar corrutinas
    }
}
