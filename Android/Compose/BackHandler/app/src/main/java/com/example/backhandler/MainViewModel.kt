package com.example.backhandler

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlin.random.Random


class MainViewModel : ViewModel(){
    var color by mutableStateOf(Color.White)
        private  set

    fun changeColor(){
        val r =  Random.nextInt(0,255)
        val g =  Random.nextInt(0,255)
        val b =  Random.nextInt(0,255)

        color= Color(r,g,b)

    }

    fun resetColor(){
        color= Color.White
    }
}