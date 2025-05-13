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
    const val PATH_FALL_DETECTION:String        = "/fall_detection"

    //indica el grupo de las notificaciones que se muestran en la bandeja de notificaciones
    const val GROUP_ID_NOTIFICATION = 9999

    const val PARAM_PENDING_INTENT_NOTIFICATION_ID:String = "PARAM_NOTIFICATION_ID"
    //constante que se utilizan para que el wearabledatalistener le avise ala view de los datos
    //recibidios
    enum class Broadcast{
        alertGeofence,
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
        FallDetection,
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

    @Serializable
    data class MsgFallDetection(
        var title:String ="",
        var message: String="",
        var fechaHora:String =""
    )
}
