package com.example.mvvm.presentation.ModelViewSinLivedata

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UsuarioViewModel : ViewModel() {
    var usuarioModel by mutableStateOf(UsuarioModel())

    /*Compse no detecta cambios en objetos complejos automaticamente (en este caso UsuarioModel)
    ,por lo que se si quiere actualizar el gui con el valor  incrementado de la edad, se dpuede realizar de 2 maneras:

    1) Se debe genera una copia del objeto, esto asegura que se genere un nuevo objweto UsuarioModel,
       disparando automaticamente una recomposicion

    2)Otra opcion es usar Livedata. Que contiene un observer
    * */
    fun incrementarEdad() {
        usuarioModel = usuarioModel.copy(edad = usuarioModel.edad + 1)
    }

}