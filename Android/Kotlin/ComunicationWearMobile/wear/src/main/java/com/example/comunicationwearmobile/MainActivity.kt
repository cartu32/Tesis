/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.comunicationwearmobile

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.OutlinedButton
import androidx.wear.compose.material.Text
import com.example.appbarscaffold.ui.ui.screen.main.MainScreen
import com.example.comunicationwearmobile.ui.WearApp


class MainActivity : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            //Aplica el theme
            WearApp{
                //llama a la funcion que crea la pantalla
                MainScreen()
            }
        }
    }
}




/*
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearNotification()
        }
    }

    private fun showToast(mcontext: Context, msg: String){
        Toast.makeText(mcontext,msg,Toast.LENGTH_SHORT).show()
    }

    @Preview(showBackground = true, device = Devices.WEAR_OS_SMALL_ROUND)
    @Composable
    fun WearNotification(){

        Column(
            modifier = Modifier
                .background(Color.Black)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            showTexto()
            ButtonExample()
        }
    }


    @Composable
    fun ButtonExample() {
        val mcontext = LocalContext.current

        Chip(
            onClick = { showToast(mcontext,"Presiono") },
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
        Text(text = msg)
    }


}
*/