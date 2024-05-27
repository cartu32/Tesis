package com.example.comunicationwearmobile.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.comunicationwearmobile.models.MobileMsgModel

class MobileViewModel : ViewModel() {
    public val mobileMsgModel = MutableLiveData(MobileMsgModel())


    fun setMessage(msg:String) {
        val dataMobile = mobileMsgModel.value ?: return
        // Modificar la propiedad edad
        dataMobile.message = msg
        // Notificar el cambio al observer con setValue
        mobileMsgModel.value = dataMobile
    }
}