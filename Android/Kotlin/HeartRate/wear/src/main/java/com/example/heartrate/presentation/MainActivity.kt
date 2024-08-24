/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.heartrate.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.heartrate.presentation.common.PermissionManager
import com.example.heartrate.presentation.models.HealthServicesManager
import com.example.heartrate.presentation.ui.screen.MainScreen
import kotlinx.coroutines.ExperimentalCoroutinesApi

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val mainScreen: MainScreen = MainScreen()
        val permissionManager:PermissionManager =PermissionManager(this)
        val healthServicesManager:HealthServicesManager= HealthServicesManager(this)
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)
        setContent {

            permissionManager.checkPermissionGiven()
            healthServicesManager.heartRateMeasureFlow()
            mainScreen.WearApp(0)
            }
    }
}
