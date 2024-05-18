package com.example.mvvm.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UsuarioViewModel : ViewModel() {
    val usuarioModel= MutableLiveData<UsuarioModel>()
    var cantClicks by mutableStateOf(0)

    fun incrementarEdad(){
        usuarioModel.value?.edad= usuarioModel.value?.edad!! +1
    }

    fun incrementarClicks(){
        cantClicks++
    }

}