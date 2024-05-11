/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.wearcomposexml.presentation

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.example.jetpackwithxml.ui.ui.screen.Main.MainActBackground
import com.example.wearcomposexml.R


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            CodeLayoutXML()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CodeLayoutXML(){
    AndroidView(

        factory = { View.inflate(it, R.layout.main_activity, null)},/*en factory se asocia el layout a la activity*/
        modifier=Modifier.fillMaxSize(),
        update ={ MainActBackground.Background(it)} /*En update todo el codigo del que las realizan las acciones de la acivity*/
    )

}

