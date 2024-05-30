package com.example.comunicationwearmobile.ui.screen.main


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.wear.compose.material.Text
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.comunicationwearmobile.MainActivity
import com.example.comunicationwearmobile.viewModels.MobileViewModel
import com.example.comunicationwearmobile.viewModels.observeMobileMessage

@Composable
fun UsuarioScreenLV(activity: MainActivity) {

    val model: MobileViewModel = ViewModelProvider(activity).get(MobileViewModel::class.java)

    val messageMobile = observeMobileMessage(activity, model)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Msg: $messageMobile")


        FilledTonalButton(
            colors = ButtonDefaults.buttonColors( Color.Red),
            onClick = {
                model.setMessage("pepipto")
            }
        ) {
            Text(text = "Actualizar Usuario")
        }
    }
}

@Preview(showBackground = true, device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun PreviewUsuarioScreenLV() {
    UsuarioScreenLV(MainActivity())
}