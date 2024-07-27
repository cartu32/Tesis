/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.comunicationwearmobile

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.comunicationwearmobile.ui.WearApp
import com.example.comunicationwearmobile.ui.screen.main.HorizontalPagerWithDotsIndicatorScreen
import com.example.comunicationwearmobile.viewModels.AlertsViewModel


class MainActivity : ComponentActivity() {

    private val TAG: String ="MainActivvity"
    private val model: AlertsViewModel by viewModels()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        model.setLifecycleOwner(this)
        Log.d(TAG," Ejecuta  OnCreate")

        setContent {

            //Aplica el theme
            WearApp{
                //llama a la funcion que crea la pantalla
                HorizontalPagerWithDotsIndicatorScreen(model)

            }
        }
    }

    override fun onStart() {
        super.onStart()

        Log.d(TAG," Ejecuta  OnStart")
    }
    override fun onDestroy() {
        super.onDestroy()

        model.removeAllMsg()
        Log.d(TAG," Ejecuta  Ondestroy")
    }

    override fun onStop() {
        super.onStop()

        Log.d(TAG," Ejecuta  OnStop")
    }

    override fun onPause() {
        super.onPause()

        Log.d(TAG," Ejecuta  OnPause")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG," Ejecuta  OnResume")
    }

}

