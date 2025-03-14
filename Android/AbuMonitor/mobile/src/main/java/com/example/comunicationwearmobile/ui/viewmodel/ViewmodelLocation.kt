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
        repositoryLocation = RepositoryLocation(application)
    }

    fun startTracking() {
        locationObserver = { location ->
            _locationLiveData.value = location
        }

        viewModelScope.launch {
            repositoryLocation?.getLocationLiveData()?.observeForever(locationObserver!!)
            repositoryLocation?.startLocationUpdates()
        }
    }

    fun stopTracking() {
        repositoryLocation?.stopLocationUpdates()
    }

    override fun onCleared() {
        super.onCleared()
        stopTracking() // Detenemos las actualizaciones

        //  Eliminar observador
        locationObserver?.let { observer ->
            repositoryLocation?.getLocationLiveData()?.removeObserver(observer)
        }

        _locationLiveData = MutableLiveData() // Limpieza de LiveData
        repositoryLocation = null // Liberar memoria

        viewModelScope.cancel() // Cancelar corrutinas
    }
}
