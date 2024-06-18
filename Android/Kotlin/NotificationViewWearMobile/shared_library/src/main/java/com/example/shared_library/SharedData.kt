package com.example.shared_library

import kotlinx.serialization.Serializable

object SharedData
{
    //constante que se utilizan para que el wearabledatalistener le avise ala view de los datos
    //recibidios
    const val BROADCAST_WEAR_DATA:String="WearDataListenerService.MessageReceived"
    const val BROADCAST_MOBILE_DATA:String="MobileDataListenerService.MessageReceived"

    //Constantes que se utilizan para los intent que se envian a la clase WearableDataListner
    const val INTENT_TYPE_MSG:String     = "Type_MSG"
    const val INTENT_BODY_MESSAGE:String = "BODY_MESSAGE"

    //constante que indican el tipo de notificacion que se le va a enviar al smartwartch
    enum class TypeNotification{
        Reminder,
        Alert,
        WithoutNotifications,
    }

    //constantantes que se utiliza para indicar si el starservice de WearableDatableListener se lamo
    //enviar un msg al wearable o es para cancelar las courutinas creadas en el service.
    enum class TypeMsg{
        messageDevice,
        cancelCoroutines,
    }

    @Serializable
    data class MsgResponseNotification(
        var idNotification:Int =-1,
        var activate:Boolean=false
    )
    @Serializable
    data class MsgNotification(
        var title:String ="",
        var message: String="",
        var typeNotification: TypeNotification = TypeNotification.WithoutNotifications,
        var date:String ="01/01/2000",
        var hour:String = "00:00",
        var idNotification:Int =-1,
    )
}