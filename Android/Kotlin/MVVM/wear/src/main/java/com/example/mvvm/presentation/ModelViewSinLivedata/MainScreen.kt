/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.mvvm.presentation.ModelViewSinLivedata

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.Text
import androidx.lifecycle.viewmodel.compose.viewModel
@Composable
fun UsuarioScreen(model: UsuarioViewModel =viewModel<UsuarioViewModel>()) {

    var usuario = model.usuarioModel
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Edad  de usuario: ${model.usuarioModel.edad}")

        FilledTonalButton(
            colors = ButtonDefaults.buttonColors( Color.Red),
            onClick = {model.incrementarEdad()
        }
        ) {
            Text(text = "Actualizar Usuario")
        }
    }
}

@Preview(showBackground = true, device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun PreviewUsuarioScreen() {
    UsuarioScreen()
}