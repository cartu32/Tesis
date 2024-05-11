package com.example.estadoscompose.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainViewModel():ViewModel() {
    fun updataName() {
        state="Juan"
    }

    var state by mutableStateOf("Pedro")
        private set //private set sirve para indicar que solo se va a modificar el state
                    //dentro de la viewmodel, no afuera.

}