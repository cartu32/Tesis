package com.example.comunicationwearmobile.ui.viewmodel

import android.app.Application
import android.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel

class ViewmodelMapsActivity(application: Application): AndroidViewModel(application) {

    private val _updateCircleRadius = MutableLiveData<Float>()
    val updateCircleRadius: LiveData<Float> = _updateCircleRadius

    fun determineColor(): Int {
        var color = Color.RED
        return color
    }

    fun updateCircleRadius(radius:Float){
        _updateCircleRadius.value = radius
    }

    fun onDestroyed() {
        //por si uso alguna corutina la cancelo
        viewModelScope.cancel()

        //limpio el livedata
        _updateCircleRadius.value = 0f
    }
}