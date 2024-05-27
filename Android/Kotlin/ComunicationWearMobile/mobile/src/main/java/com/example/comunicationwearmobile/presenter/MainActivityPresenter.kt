
package com.example.comunicationwearmobile.presenter

import com.example.comunicationwearmobile.common.InterfaceMainAct
import com.example.comunicationwearmobile.common.InterfaceMainPre
import com.example.comunicationwearmobile.models.WearableDataListenerService


class MainActivityPresenter(interMainView: InterfaceMainAct,listenerService: WearableDataListenerService):InterfaceMainPre {

    private var interMainView: InterfaceMainAct? = interMainView
    private var listenerService: WearableDataListenerService? = listenerService
    override fun onDataReceived(data: String) {
        interMainView?.showToast(data)
        interMainView?.updateTextBox(data)
    }

    public fun sendDataWearable(path:String,msg:String){
        listenerService?.sendDataWearable(path,msg)
    }

    fun onCleared() {
        listenerService?.onCleared()
    }


}