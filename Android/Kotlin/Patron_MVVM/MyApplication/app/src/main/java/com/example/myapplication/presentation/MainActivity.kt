/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.myapplication.presentation
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.wear.compose.material.Text

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            MyApp()
        }
    }
}

class MyViewModel : ViewModel() {
    private val _isTrue = MutableLiveData<Boolean>()
    val isTrue: LiveData<Boolean> get() = _isTrue

    init {
        // Inicializar la variable booleana
        _isTrue.value = false
    }

    fun toggleBoolean() {
        _isTrue.value = _isTrue.value?.not()
    }

    fun setBoolean(value: Boolean) {
        _isTrue.value = value
    }
}

@Composable
fun MyScreen(viewModel: MyViewModel) {
    val isTrue by viewModel.isTrue.observeAsState(initial = false)

    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // No se necesita el Observer, se usa isTrue directamente
        if (isTrue) {
            Text(text = "El valor es verdadero")
        } else {
            Text(text = "El valor es falso")
        }
    }
}

@Composable
fun MyApp() {
    val viewModel = remember { MyViewModel() }
    MyScreen(viewModel)
    viewModel.setBoolean(false)
}
