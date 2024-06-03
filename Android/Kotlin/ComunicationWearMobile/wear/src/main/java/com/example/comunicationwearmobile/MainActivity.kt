/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.comunicationwearmobile

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.comunicationwearmobile.ui.WearApp
import com.example.comunicationwearmobile.ui.screen.main.UsuarioScreenLV
import com.example.comunicationwearmobile.viewModels.MobileViewModel


class MainActivity : ComponentActivity() {

    private var model: MobileViewModel? = null

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProvider(this).get(MobileViewModel::class.java)

        setContent {
            //Aplica el theme
            WearApp{
                //llama a la funcion que crea la pantalla
                UsuarioScreenLV(this, model!!)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        model?.onCleared()
    }
}

