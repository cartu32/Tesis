package com.example.comunicationwearmobile.viewModels

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.comunicationwearmobile.models.MobileMsgModel

class MobileViewModel(application: Application) : AndroidViewModel(application) {
    public val mobileMsgModel = MutableLiveData(MobileMsgModel())

    init {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val message = intent?.getStringExtra("message") ?: return
                setMessage(message)
            }
        }

        LocalBroadcastManager.getInstance(application).registerReceiver(
            receiver, IntentFilter("MobileDataListenerService.MessageReceived")
        )
    }
    fun setMessage(msg: String) {
        val dataMobile = mobileMsgModel.value ?: return

        // Crear una nueva instancia de MobileMsgModel con los nuevos valores
        mobileMsgModel.value = dataMobile.copy(message = msg)
    }

    fun incrementNumberMessage() {
        val dataMobile = mobileMsgModel.value ?: return

        // Crear una nueva instancia de MobileMsgModel con los nuevos valores
         mobileMsgModel.value = dataMobile.copy(numberMsg = dataMobile.numberMsg + 1)
    }

}

@Composable
fun observeMobileMessage(activity: ComponentActivity, model: MobileViewModel): State<MobileMsgModel> {
    var messageMobile = remember { mutableStateOf(MobileMsgModel()) }

    //con esto se crea el observer en el viewmodel
    DisposableEffect(activity)
    {
        val observer = Observer<MobileMsgModel> { newMessage ->
            messageMobile.value = newMessage
        }
        model.mobileMsgModel.observe(activity, observer)

        // Cleanup observer when no longer needed
        onDispose {
            model.mobileMsgModel.removeObserver(observer)
        }
    }
    return messageMobile

}