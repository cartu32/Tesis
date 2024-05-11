package com.example.backhandler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import com.example.backhandler.ui.theme.BackHandlerTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun MainScreen() {

        val viewmodel: MainViewModel by  viewModels()
        Box(
            Modifier
                .fillMaxSize()
                .background(viewmodel.color), contentAlignment = Alignment.Center)
        {
            //Si presiono el boton de volver para atras, pongo el color blanco.
            //ojo porque si en el paramtero enabled de la fc  porngo true, entonces no va a cerrar la app.
            //Para que eso no pase debo hacer un como parametro
            BackHandler(viewmodel.color!=Color.White) {
                viewmodel.resetColor()
            }

            //Si presiono el boton cambia de color
            Button(onClick = { viewmodel.changeColor()})
            {
                Text(text = "Cambiar Color")
            }
        }
    }
}