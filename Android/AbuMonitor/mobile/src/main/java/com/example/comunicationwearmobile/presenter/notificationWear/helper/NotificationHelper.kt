package com.example.comunicationwearmobile.presenter.notificationWear.helper

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.common.Utils
import com.example.comunicationwearmobile.models.contacts.ContactDataStore
import com.example.comunicationwearmobile.models.wearable.NotificationIDCounterFile
import com.example.shared_library.SharedData
import com.example.shared_library.fromByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class NotificationPresenter private constructor(context: Context) {


    private val GROUP_ID: Int            = 0
    private val KEY_LIST_NOTIFICATION_SP = "KEY_LIST_NOTIFICATION_SP"
    private val GROUP_KEY_NOTIFICATION   = "GROUP_NOTIFICATION"
    private val CHANNEL_ID               = "CHANNEL_ID_NOTIFICATION"
    private val CHANNEL_NAME             = "CHANNEL_NAME_NOTIFICATION"
    private val CHANNEL_DESCRIPTION      = "CHANNEL_DESCRIPTION_NOTIFICATION"
    private val PATTERN_VIBRATION: LongArray = longArrayOf(0, 1000, 500, 1000)
    private val appContext: Context = context.applicationContext

    init {

        val receiver = createBroadcastReceiver()

        LocalBroadcastManager.getInstance(appContext).registerReceiver(
            receiver , IntentFilter(SharedData.Broadcast.fromWearData.name)
        )
    }
    companion object {
        @Volatile
        private var INSTANCE: NotificationPresenter? = null

        fun getInstance(context: Context): NotificationPresenter {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NotificationPresenter(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // Función para crear el BroadcastReceiver
    private fun createBroadcastReceiver(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val path:String = intent?.getStringExtra(SharedData.ParamIntent.MESSAGE_PATH.name)!!

                val msgBytes: ByteArray = intent.getByteArrayExtra(SharedData.ParamIntent.MESSAGE_BODY.name)!!



                when(path){
                    SharedData.PATH_VIEWED_NOTIFICATION -> onDataReceived(msgBytes)
                    SharedData.PATH_FALL_DETECTION -> sendSMS(msgBytes)
                }
            }
        }
    }
    fun sendSMS(message: ByteArray) {
        try {
            val msgFallDetection: SharedData.MsgFallDetection = fromByteArray(message)

            // Necesitas correr esto en una corutina para poder recolectar el valor del Flow
            CoroutineScope(Dispatchers.IO).launch {
                val phoneNumber = ContactDataStore.getTelephoneNumber(context = appContext).firstOrNull()

                if (phoneNumber != null && phoneNumber.isNotBlank()) {
                    val smsManager = appContext.getSystemService(SmsManager::class.java)
                    smsManager.sendTextMessage(phoneNumber, null, msgFallDetection.message + msgFallDetection.fechaHora, null, null)
                    println("SMS enviado exitosamente.")
                } else {
                    println("Número de teléfono no disponible o vacío.")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error al enviar el SMS: ${e.message}")
        }
    }


    fun onDataReceived(msgBytes: ByteArray) {
        val indexList:Int = fromByteArray(msgBytes)

        cancelNotificationID(indexList)
    }


    fun showNotification(msg: SharedData.MsgNotification) {
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationBuilder = createChannel(notificationManager)

        // Crear y notificar la notificación del grupo
        val groupNotificationBuilder = createGroupNotification()
        notificationManager.notify(GROUP_ID, groupNotificationBuilder.build())

        val notificationId=getNotificationId()

        // Crear y notificar una notificación individual
        createNotification(notificationBuilder,msg,notificationId)
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun getNotificationId():Int{
        val preferences= NotificationIDCounterFile.getInstance(appContext)
        val listNotification=preferences.getArrayList(KEY_LIST_NOTIFICATION_SP)
        var newId=1

        if (listNotification.isNotEmpty()) {
            newId = listNotification.last()
            newId++
        }
        listNotification.add(newId)
        preferences.saveArrayList(listNotification,KEY_LIST_NOTIFICATION_SP)
        return newId
    }


    //esta funcion elimina el notification id del shared preference. Atencion la eliminacion de la
    //bandeja de entrada se hace automaticamente con el pending intent, ya esta implicito
    fun removeByNotificationId(idNotification:Int):Int{
        val preferences= NotificationIDCounterFile.getInstance(appContext)
        val listNotification=preferences.getArrayList(KEY_LIST_NOTIFICATION_SP)

        val posList= listNotification.indexOf(idNotification)
        listNotification.removeAt(posList)

        preferences.saveArrayList(listNotification,KEY_LIST_NOTIFICATION_SP)

        return posList
    }

    private fun removeByPositionId(indexList:Int):Pair<Int,Boolean>{
        val preferences= NotificationIDCounterFile.getInstance(appContext)
        val listNotification=preferences.getArrayList(KEY_LIST_NOTIFICATION_SP)

        val notificationId= listNotification[indexList]
        listNotification.removeAt(indexList)

        val isListEmpty:Boolean=listNotification.isEmpty()

        preferences.saveArrayList(listNotification,KEY_LIST_NOTIFICATION_SP)

        return Pair(notificationId,isListEmpty)
    }

    private fun cancelNotificationID(indexList: Int){
        // Obtener el NotificationManager del sistema

        val (notificationId,allNotificationsCanceled)=removeByPositionId(indexList)
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Cancelar la notificación con el ID especificado
        notificationManager.cancel(notificationId)

        //si todas las notificaciones del smrtphone fueron canceladas, entonces se cancela la notificacion del grupo
        if(allNotificationsCanceled){
            notificationManager.cancel(GROUP_ID)
        }

    }
    private fun createGroupNotification(): NotificationCompat.Builder {
        return NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.old_person)
            .setLargeIcon(BitmapFactory.decodeResource(appContext.resources, R.drawable.old_person))
            .setContentTitle("Grupo de notificaciones")
            .setContentText("Usted tiene algunas alertas pendientes por leer")
            .setGroup(GROUP_KEY_NOTIFICATION)
            .setGroupSummary(true)
            .setAutoCancel(true)
    }

    @SuppressLint("LaunchActivityFromNotification")
    private fun createNotification(
        notificationBuilder: NotificationCompat.Builder ,
        msg: SharedData.MsgNotification ,
        notificationId: Int
    ) {

       val cancelNotificationIntent = Intent(appContext, NotificationCancelReceiver::class.java).apply {
            putExtra(SharedData.PARAM_PENDING_INTENT_NOTIFICATION_ID,notificationId)
       }


        val cancelPendingIntent = PendingIntent.getBroadcast(appContext, notificationId, cancelNotificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        notificationBuilder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.old_person)
            .setLargeIcon(BitmapFactory.decodeResource(appContext.resources, R.drawable.old_person))
            .setContentIntent(cancelPendingIntent)
            .setDeleteIntent(cancelPendingIntent)
            .setGroup(GROUP_KEY_NOTIFICATION)
            .setLocalOnly(true)
            .setTicker("Mensajes")
            .setContentTitle(msg.title)
            .setStyle(NotificationCompat.InboxStyle()
                .addLine(msg.message)
                .addLine("")
                .addLine("Fecha: "+msg.date+"    Hora: " +msg.hour))

    }

    private fun createChannel(notificationManager: NotificationManager): NotificationCompat.Builder {
        val notificationChannel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.description = CHANNEL_DESCRIPTION
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.vibrationPattern = PATTERN_VIBRATION
        notificationChannel.enableVibration(true)
        notificationChannel.setShowBadge(true)

        notificationManager.createNotificationChannel(notificationChannel)

        return NotificationCompat.Builder(appContext, CHANNEL_ID)
    }


}

// BroadcastReceiver para manejar la cancelación de notificaciones
// Esto se hace aca dentro porque sino no puedo decrementar el contador de notificaciones en
// el pending intent
class NotificationCancelReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val notificationId= intent.getIntExtra(SharedData.PARAM_PENDING_INTENT_NOTIFICATION_ID,0    )
        val notificationPresenter = NotificationPresenter.getInstance(context)
        val posNotificationId = notificationPresenter.removeByNotificationId(notificationId)

        // Decrementar el contador de notificaciones activas
        Utils.sendMessageToService(context,SharedData.PATH_VIEWED_NOTIFICATION,posNotificationId)
     }
}

