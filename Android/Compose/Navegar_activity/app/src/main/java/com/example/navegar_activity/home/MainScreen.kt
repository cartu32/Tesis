package com.example.navegar_activity.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(showBackground = true)
@Composable
fun MainScreen(
        onButtonClick: (String) -> Unit)//se pasa  como parametro una fc callback para retornar el string del texto ingresado al llamador
{
        var text by remember {
           mutableStateOf("")
        }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              )
        {
            TextField(value = text,onValueChange = { text = it }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { onButtonClick(text)}, modifier = Modifier.fillMaxWidth()) {
                Text(text="Enviar")

            }
        }

}
