package com.example.shared_library

import kotlinx.serialization.Serializable

object SharedData
{
    //constantes que indican el tipo de mensaje que es
    //este tipo de mensaje se usa para agregar una notificacion a la  view
    const val PATH_ADD_NOTIFICATION:String      = "/add_notificaction"

    //este tipo de mensaje se usa para avisar que el usuario vio la notificacion
    //y se debe eliminar de la view
    const val PATH_VIEWED_NOTIFICATION:String   = "/viewed_notification"

    //constante que se utilizan para que el wearabledatalistener le avise ala view de los datos
    //recibidios
    enum class Broadcast{
        fromWearData,
        fromMobileData,
    }
    //Constantes que se utilizan para los intent que se envian a la clase WearableDataListner
    enum class ParamIntent{
        MESSAGE_BODY,
        MESSAGE_PATH,
    }
    //constante que indican el tipo de notificacion que se le va a enviar al smartwartch
    enum class TypeNotification{
        Reminder,
        Alert,
        WithoutNotifications,
    }

    @Serializable
    data class MsgViewNotification(
        var numberNotification:Int=-1
    )
    @Serializable
    data class MsgNotification(
        var title:String ="",
        var message: String="",
        var typeNotification: TypeNotification = TypeNotification.WithoutNotifications,
        var date:String ="01/01/2000",
        var hour:String = "00:00"
    )
}
