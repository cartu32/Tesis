package com.example.comunicationwearmobile.ui.viewmodel


import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.repository.RepositoryLocation

class ViewmodelLocation(application: Application) : AndroidViewModel(application) {

    private var repositoryLocation: RepositoryLocation? = null

    //Esta es otra forma de hacer un observer de livedata
    //Como no los datos de ubicacion del gps no se modifican para enviarselo a la activty
    //se usa al viemodel como un pasamanos entre el repository y la activity.
    //Entonces se puede definir de esta manera. Que es hace lo mismo de otra manera de como
    //hace en ViewmodelMaps, pero más optimo.

    var locationLiveData: LiveData<Location>? =null

    init {
        repositoryLocation = RepositoryLocation.getInstance(application)
        repositoryLocation?.startLocationUpdates()
        locationLiveData= repositoryLocation?.locationLiveData
    }


    fun onDestroyed(){
        repositoryLocation = null // Liberar memoria

        Log.d(Definition.TAG_DEBUG,"OnCleared ViewmodelLocation")
    }
}
