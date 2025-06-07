/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.comunicationwearmobile.view.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.comunicationwearmobile.utils.mannager.PermissionManager
import com.example.comunicationwearmobile.utils.showToast
import com.example.comunicationwearmobile.view.jetpackCompose.WearApp
import com.example.comunicationwearmobile.view.jetpackCompose.main.HorizontalPagerWithDotsIndicatorScreen
import com.example.comunicationwearmobile.viewModels.AlertsViewModel
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private val TAG: String ="MainActivvity"
    private val model: AlertsViewModel by viewModels()
    private var permissionManager: PermissionManager? =null

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTurnScreenOn(true)

        val context = this
        permissionManager= PermissionManager(context)
        model.setLifecycleOwner(context)

        lifecycleScope.launch{
            val hasPermissionsAndCapabilities = model.checkPermissionsAndCapabilities(permissionManager!!)

            if(!hasPermissionsAndCapabilities ){
                showToast(context, "Permisos no otorgados o el reloj no puede detectar caídas")
            }
        }

        setContent {
            //Aplica el theme
            WearApp{
                //llama a la funcion que crea la pantalla
                HorizontalPagerWithDotsIndicatorScreen(model)
            }
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        Log.d("ABU_MONITOR", "Main onDestroy")
       // model.removeAllMsgInAllDevices()
    }
}

