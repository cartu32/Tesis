package com.example.mvvm.presentation.ModelViewConLivedata

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mvvm.presentation.ModelViewSinLivedata.UsuarioModel
import com.example.mvvm.presentation.ModelViewSinLivedata.UsuarioViewModel

class UsuarioViewModelLV : ViewModel() {
    public val usuarioModel = MutableLiveData(UsuarioModel())


    /*Compse no detecta cambios en objetos complejos automaticamente (en este caso UsuarioModel)
    ,por lo que se si quiere actualizar el gui con el valor  incrementado de la edad, se dpuede realizar de 2 maneras:

    1) Se debe genera una copia del objeto, esto asegura que se genere un nuevo objweto UsuarioModel,
       disparando automaticamente una recomposicion

    2)Otra opcion es usar Livedata. Que contiene un observer
    * */
    fun incrementarEdad() {
        val currentUsuario = usuarioModel.value ?: return
        // Modificar la propiedad edad
        currentUsuario.edad++
        // Notificar el cambio al observer con setValue
        usuarioModel.value = currentUsuario
    }

    fun asignName(name:String) {
        val currentUsuario = usuarioModel.value ?: return
        // Modificar la propiedad edad
        currentUsuario.nombre=name
        // Notificar el cambio al observer con setValue
        usuarioModel.value = currentUsuario
    }
}