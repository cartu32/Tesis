package com.example.appbarscaffold.ui.ui.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Text
import com.example.comunicationwearmobile.utils.showToast

/*************************************************************
3° Optimizazión: Crea Composables que definan una panatalla
 **************************************************************
 */

@Composable
fun MainScreen() {
    WearNotification()
}

@Preview(showBackground = true, device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun WearNotification(){

    Column(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        //showTexto()
        ButtonExample()
    }
}


@Composable
fun ButtonExample() {
    val mcontext = LocalContext.current
    var contador  by remember { mutableIntStateOf(0) }

    Text(text = "Contador: $contador")

    Chip(
        onClick =
        {
            contador++
            showToast(mcontext,"Presiono")
        },
        colors = ChipDefaults.chipColors(Color.White),
        border = ChipDefaults.chipBorder()
    ) {
        Text(
            text = "Enviar msg a Mobile",
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 8.dp,vertical = 6.dp)
        )
    }

}


@Composable
fun showTexto(msg: String = "Recibido:"){
    Text(text =msg)
}