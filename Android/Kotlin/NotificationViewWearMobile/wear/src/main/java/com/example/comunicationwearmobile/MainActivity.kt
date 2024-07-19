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
import androidx.lifecycle.ViewModelProvider
import com.example.comunicationwearmobile.common.ST_ACTIVITY_CREATED
import com.example.comunicationwearmobile.common.ST_ACTIVITY_NO_CREATED
import com.example.comunicationwearmobile.common.ST_ACTIVITY_PAUSED
import com.example.comunicationwearmobile.common.ST_ACTIVITY_RESUMED
import com.example.comunicationwearmobile.ui.WearApp
import com.example.comunicationwearmobile.ui.screen.main.HorizontalPagerWithDotsIndicatorScreen
import com.example.comunicationwearmobile.viewModels.AlertsViewModel


class MainActivity : ComponentActivity() {

    private val TAG: String? ="MainActivvity"
    private var model: AlertsViewModel? = null

    companion object{
        var stateActivity:Int= ST_ACTIVITY_NO_CREATED
    }
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProvider(this).get(AlertsViewModel::class.java)
        Log.d(TAG," Ejecuta  OnCreate")
        setContent {
            //Aplica el theme
            WearApp{
                stateActivity= ST_ACTIVITY_CREATED

                //llama a la funcion que crea la pantalla
                HorizontalPagerWithDotsIndicatorScreen(model!!)

            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG," Ejecuta  OnStart")
    }
    override fun onDestroy() {
        super.onDestroy()

        stateActivity = ST_ACTIVITY_NO_CREATED
        model?.removeAllMsg()
        Log.d(TAG," Ejecuta  Ondestroy")
    }

    override fun onStop() {
        super.onStop()

        Log.d(TAG," Ejecuta  OnStop")
    }

    override fun onPause() {
        super.onPause()
        stateActivity = ST_ACTIVITY_PAUSED
        Log.d(TAG," Ejecuta  OnPause")
    }

    override fun onResume() {
        super.onResume()

        stateActivity = ST_ACTIVITY_RESUMED
        Log.d(TAG," Ejecuta  OnResume")
    }

}

