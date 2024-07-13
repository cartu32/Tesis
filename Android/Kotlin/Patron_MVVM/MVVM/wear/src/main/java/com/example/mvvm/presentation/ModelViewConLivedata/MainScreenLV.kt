/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.mvvm.presentation.ModelViewConLivedata

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.wear.compose.material.Text
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mvvm.presentation.ModelViewSinLivedata.UsuarioModel

@Composable
fun UsuarioScreenLV(model: UsuarioViewModelLV =viewModel<UsuarioViewModelLV>()) {


    val lifecycleOwner = LocalLifecycleOwner.current

    var edad by remember { mutableStateOf(0) }
    var nombre by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // se genera el observer para el usuario
        model.usuarioModel.observe(lifecycleOwner, Observer{
            edad=it.edad
            nombre=it.nombre
        })

        Text(text = "Nombre: ${nombre}")
        Text(text = "Edad: ${edad}")


        FilledTonalButton(
            colors = ButtonDefaults.buttonColors( Color.Red),
            onClick = {
                model.incrementarEdad()
                model.asignName("Esteban")
        }
        ) {
            Text(text = "Actualizar Usuario")
        }
    }
}

@Preview(showBackground = true, device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun PreviewUsuarioScreenLV() {
    UsuarioScreenLV()
}