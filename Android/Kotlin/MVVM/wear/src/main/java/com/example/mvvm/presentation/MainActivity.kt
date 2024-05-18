package com.example.mvvm.presentation

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.mvvm.presentation.ModelViewConLivedata.UsuarioScreenLV
import com.example.mvvm.presentation.theme.MVVMTheme

class MainActivity : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            //Aplica el theme
            MVVMTheme {
                //llama a la funcion que crea la pantalla
                //UsuarioScreen()
                UsuarioScreenLV()
            }
        }
    }
}