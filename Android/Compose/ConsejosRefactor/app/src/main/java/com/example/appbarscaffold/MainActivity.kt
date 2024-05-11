package com.example.appbarscaffold

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.appbarscaffold.ui.ui.screen.main.MainAppBar
import com.example.appbarscaffold.ui.ui.screen.main.MediaList
import com.example.appbarscaffold.ui.ui.MyMoviesApp
import com.example.appbarscaffold.ui.ui.screen.main.MainScreen

class MainActivity : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            //Aplica el theme
            MyMoviesApp{
                //llama a la funcion que crea la pantalla
                    MainScreen()
            }
        }
    }
}





