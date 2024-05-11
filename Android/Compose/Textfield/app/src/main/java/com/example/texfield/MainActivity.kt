package com.example.texfield

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.texfield.ui.theme.TexfieldTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }
}


@Composable
fun MainScreen() {
    var name by remember { //El texfield no mantiene el estado(no lo recuerda cuando se recompone) por eso uso un remeber
        mutableStateOf(" ")
    }

    var currentName by remember{
        mutableStateOf("Pedro")
    }

    Column {
        Text(text=currentName)
        //uso un rememeber porque onValueChange es el que toma el valor y se lo da a value para mmostrarlo.
        TextField(value = name, onValueChange ={
            name=it
        } )
        Button(onClick = {
            if(name.isNotBlank()){
                currentName=name
            }

        }) {
            Text(text="Click me")

        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    MainScreen()
}

