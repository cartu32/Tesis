package com.example.comunicationwearmobile.ui.utils.Mannager

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.model.repository.RepositoryIDNotificationSPref
import com.example.comunicationwearmobile.ui.utils.broadcast.NotificationCancelReceiver
import com.example.shared_library.SharedData
import com.example.shared_library.fromByteArray
import java.util.concurrent.locks.ReentrantLock

class NotificationManagerHelper(context: Context) : ContextWrapper(context) {

    private val lock=ReentrantLock()

    private var manager:NotificationManager?=null
    private val appContext: Context = context.applicationContext


    //ID de la primera notificacion generada
    val ID_NOTIFICATION_FOREGROUND_SERVICE          = 1001


    init {
        //creo un canal de notificaciones exclusivo para el foregroundservices
        initConfiguration()
    }


    private fun initConfiguration() {
        //cuando se inicia por primera vez la aplicacion se borra el contenido
        //del shared preferences con los id de las notificaciones
        val preferences = RepositoryIDNotificationSPref.getInstance(appContext)
        preferences.clearSharedPreferences()
        manager=getSystemService(NotificationManager::class.java)
    }



    fun notificationViewedOnWearable(msgBytes: ByteArray) {
        val indexList:Int = fromByteArray(msgBytes)

        cancelNotificationID(indexList)
    }

    private fun cancelNotificationID(indexList: Int){
        // Obtener el NotificationManager del sistema

        val (notificationId,allNotificationsCanceled)=deleteNewNotificationByPositionId(indexList)

        //borro la nostificacion de la bandeja de la notificaciones del S.O
        cancelNotification(notificationId,allNotificationsCanceled)
    }

    private fun cancelNotification(notificationId: Int, allNotificationsCanceled: Boolean){

        // Cancelar la notificación con el ID especificado
        manager?.cancel(notificationId)

        //si todas las notificaciones del smrtphone fueron canceladas, entonces se cancela la notificacion del grupo
        if(allNotificationsCanceled){
            manager?.cancel(GROUP_ID)
        }

    }


    private fun getNewIdNotification():Int{
        lock.lock()
        try {
            val preferences = RepositoryIDNotificationSPref.getInstance(appContext)
            val listNotification = preferences.getArrayList(KEY_LIST_NOTIFICATION_SP)
            var newId = 1

            if (listNotification.isNotEmpty()) {
                newId = listNotification.last()
                newId++
            }
            listNotification.add(newId)
            preferences.saveArrayList(listNotification, KEY_LIST_NOTIFICATION_SP)
            return newId
        }finally {
            lock.unlock()
        }
    }

    //esta funcion elimina el notification id del shared preference. Atencion la eliminacion de la
    //bandeja de entrada se hace automaticamente con el pending intent, ya esta implicito
    fun deleteNewNotificationById(idNotification:Int):Int{
        lock.lock()
        try {
            val preferences = RepositoryIDNotificationSPref.getInstance(appContext)
            val listNotification = preferences.getArrayList(KEY_LIST_NOTIFICATION_SP)

            val posList = listNotification.indexOf(idNotification)
            //compruebo que el indice este en la lista
            if (posList == -1)
            //si no esta retorno -1
                return posList

            listNotification.removeAt(posList)

            //borro la notificacion de la bandeja de notificaciones del S.O
            cancelNotification(idNotification, listNotification.isEmpty())

            preferences.saveArrayList(listNotification, KEY_LIST_NOTIFICATION_SP)

            return posList
        }finally {
            lock.unlock()
        }
    }

    private fun deleteNewNotificationByPositionId(indexList:Int):Pair<Int,Boolean> {
        lock.lock()
        try {
            val preferences = RepositoryIDNotificationSPref.getInstance(appContext)
            val listNotification = preferences.getArrayList(KEY_LIST_NOTIFICATION_SP)

            val notificationId = listNotification[indexList]
            listNotification.removeAt(indexList)

            val isListEmpty: Boolean = listNotification.isEmpty()

            preferences.saveArrayList(listNotification, KEY_LIST_NOTIFICATION_SP)

            return Pair(notificationId, isListEmpty)
        }finally {
            lock.unlock()
        }
    }



    private fun createChannelForegroundServices() {
        val notificationChannel =
            NotificationChannel(
                CHANNEL_ID_FOREGROUND_SERVICE ,
                CHANNEL_FOREGROUND_SERVICE ,
                NotificationManager.IMPORTANCE_DEFAULT
            )
        notificationChannel.description = "Canal de Notificacion del Foregroundservice"

        manager?.createNotificationChannel(notificationChannel)
    }


    private fun createChannelAlerts(): NotificationCompat.Builder {
        val notificationChannel = NotificationChannel(
            CHANNEL_ID_ALERTS, CHANNEL_ALERTS, NotificationManager.IMPORTANCE_HIGH
        )

        notificationChannel.description = "Canal de Alerta"
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(true)
        notificationChannel.setShowBadge(true)

        manager?.createNotificationChannel(notificationChannel)

        return NotificationCompat.Builder(appContext, CHANNEL_ID_ALERTS)
    }

    // Crea la notifcación del foregroundservice
    private fun getNewNotificationForegroundService(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID_FOREGROUND_SERVICE)
            .setContentTitle("AbuMonitor")
            .setContentText("Ejecutando AbuMonitor en primer plano...")
            .setSmallIcon(R.drawable.ic_old_person)
            .setOngoing(true) // Esto hace que no pueda eliminarse
            .build()
    }

    private fun createGroupNotification(): NotificationCompat.Builder {
        var noti: NotificationCompat.Builder?=null
        noti= appContext.let {
            NotificationCompat.Builder(it, CHANNEL_ID_ALERTS)
                .setSmallIcon(R.drawable.ic_old_person)
                .setLargeIcon(BitmapFactory.decodeResource(appContext.resources, R.drawable.ic_old_person))
                .setContentTitle("Grupo de notificaciones")
                .setContentText("Usted tiene algunas alertas pendientes por leer")
                .setGroup(GROUP_KEY_NOTIFICATION)
                .setGroupSummary(true)
                .setAutoCancel(true)
                //.setDeleteIntent(getCancelIntent(GROUP_ID)) // Agregar esta línea

        }
        return noti
    }

    private fun getCancelIntent(notificationId: Int): PendingIntent {
        val cancelNotificationIntent = Intent(appContext, NotificationCancelReceiver::class.java).apply {
            putExtra(SharedData.PARAM_PENDING_INTENT_NOTIFICATION_ID, notificationId)
        }
        return PendingIntent.getBroadcast(
            appContext, notificationId, cancelNotificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    @SuppressLint("LaunchActivityFromNotification")
    private fun createAlertNotification(
        notificationBuilder: NotificationCompat.Builder ,
        msg: SharedData.MsgNotification ,
        notificationId: Int
    ) {

        val cancelPendingIntent = getCancelIntent(notificationId)

        notificationBuilder
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_old_person)
            .setLargeIcon(BitmapFactory.decodeResource(appContext.resources, R.drawable.ic_old_person))
            .setContentIntent(cancelPendingIntent)
            .setDeleteIntent(cancelPendingIntent)
            .setGroup(GROUP_KEY_NOTIFICATION)
            .setLocalOnly(true)
            .setOngoing(false)
            .setTicker("Mensajes")
            .setContentTitle(msg.title)
            .setStyle(NotificationCompat.InboxStyle()
                .addLine(msg.message)
                .addLine("")
                .addLine("Fecha: "+msg.date+"    Hora: " +msg.hour))

    }

    fun createNotificationForegroundService(): Notification {
        val notification:Notification

        //Se crea el canal de notificaciones del ForegroundService
        createChannelForegroundServices()

        //Se crea la notificacion del ForegroundService
        notification= getNewNotificationForegroundService()

        //En este caso no se muestra la notificacion aca con el notify, sino que hace en la funcion
        //llamadora cuando ejecuta startForeground. Ahi recien se muestra la notificacion
        return notification
    }

    fun showNotificationGeneral(msg:SharedData.MsgNotification){
        // Crear el canal de notificaciones
        val notificationBuilder = createChannelAlerts()

        // crear y mostrar(muestra como una notificacion) el grupo de notificaciones
        //En este caso al crear grupo se configuro para que no se muestra una notificacion
        //sobre el grupo sino que se muestra solamente la  notificacion del msg al final
        val groupNotificationBuilder = createGroupNotification()
        manager?.notify(GROUP_ID, groupNotificationBuilder.build())

        //se obtiene el numero de notificacion existente del shared preference
        val notificationId=getNewIdNotification()

        // Crear y muestra la notificación del msg recibido. ESta se agrupa en el grupo de notificaciones
        createAlertNotification(notificationBuilder,msg,notificationId)
        manager?.notify(notificationId, notificationBuilder.build())


    }



    companion object {
        const val CHANNEL_FOREGROUND_SERVICE = "Foregroundservice"
        const val CHANNEL_ID_FOREGROUND_SERVICE = "Channel_ID_ForegroundService"

        const val GROUP_ID: Int = 1000
        const val GROUP_KEY_NOTIFICATION = "GROUP_NOTIFICATION"

        const val CHANNEL_ALERTS = "Alertas"
        const val CHANNEL_ID_ALERTS = "Channel_ID_Alerts"

        const val KEY_LIST_NOTIFICATION_SP = "KEY_LIST_NOTIFICATION_SP"

        var instance: NotificationManagerHelper? = null

        @Synchronized
        fun getInstance(base: Context): NotificationManagerHelper? {
            if (instance == null) {
                instance = NotificationManagerHelper(base)
            }
            return instance
        }
    }

}
