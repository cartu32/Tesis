/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.comunicationwearmobile

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.example.comunicationwearmobile.common.TypeMsg
import com.example.comunicationwearmobile.models.MsgAlertModel
import com.example.comunicationwearmobile.ui.WearApp
import com.example.comunicationwearmobile.ui.screen.main.HorizontalPagerWithDotsIndicatorScreen
import com.example.comunicationwearmobile.viewModels.AlertsViewModel


class MainActivity : ComponentActivity() {

    private var model: AlertsViewModel? = null

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProvider(this).get(AlertsViewModel::class.java)

        setContent {
            //Aplica el theme
            WearApp{
              
                //llama a la funcion que crea la pantalla
                HorizontalPagerWithDotsIndicatorScreen(model!!)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

