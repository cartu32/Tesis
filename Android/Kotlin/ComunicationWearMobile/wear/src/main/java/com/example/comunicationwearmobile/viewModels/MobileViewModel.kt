package com.example.comunicationwearmobile.viewModels

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.comunicationwearmobile.models.MobileDataListenerService
import com.example.comunicationwearmobile.models.MobileMsgModel
import com.example.shared_library.SharedData

/*
Los viewmodels se usan para mantener el estado de las variables. Esto sirve mas que nada para cuando
se gira la pantalla, o no se quiere guardar el estado de las mismas sin la necesidad de usar un
objeto bundle
 */
class MobileViewModel(application: Application) : AndroidViewModel(application) {
    private val mobileMsgModel = MutableLiveData(MobileMsgModel())
    private val appContext:Context =application.applicationContext

    /*
    En el constructor se definen los broadcast que va a usar el service de WearableListenerService
     para enviar datos a la view. Como es un sevice se usan para ello los braodcast receiver
     */
    init {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val message = intent?.getStringExtra("message") ?: return
                setMessage(message)
            }

        }

        LocalBroadcastManager.getInstance(application).registerReceiver(
            receiver, IntentFilter(SharedData.broadcast_mobile_data)
        )

        val serviceIntent = Intent(appContext, MobileDataListenerService::class.java)
        appContext.startService(serviceIntent)
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

        sendMessageToService(SharedData.msg_wear_to_mobile, mobileMsgModel.value!!.numberMsg.toString())

    }


    private fun sendMessageToService(path:String, message:String){
        val serviceIntent = Intent(appContext, MobileDataListenerService::class.java).apply {
            putExtra("path", path)
            putExtra("message", message)
        }
        appContext.startService(serviceIntent)
    }


    public override fun onCleared() {
        sendMessageToService(SharedData.cancel_path," ")
    }

    /*
     Se generan los observer del livedata para notificarle a la vista que se produjo un cambio en
     el modelo. Este cambio se debe hacer efectivo dentro de DisposableEffect,
      sino se hace asi se va a modificar el modelo pero no se le notifica a la view.
     */
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


}
