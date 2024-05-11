package com.example.jetpackwithxml

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.example.jetpackwithxml.ui.ui.screen.Main.MainActBackgroundJava
import com.example.jetpackwithxml.ui.ui.screen.Main.MainActBackgroundKotlin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //aca va el codigo de la activity
        setContent {
            CodeLayoutXML()
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun CodeLayoutXML(){
        AndroidView(

            factory = {View.inflate(it, R.layout.main_layout, null)},/*en factory se asocia el layout a la activity*/
            modifier=Modifier.fillMaxSize(),
            update ={

                /*En update va todo el codigo del que las realizan las acciones de la acivity*/

                //Si el background quiero hacerlo en Kotlin
                //MainActBackgroundKotlin.Background(it)

                //Si el background quiero hacerlo en java
                MainActBackgroundJava.Background(it)
        }
        )

    }



}

