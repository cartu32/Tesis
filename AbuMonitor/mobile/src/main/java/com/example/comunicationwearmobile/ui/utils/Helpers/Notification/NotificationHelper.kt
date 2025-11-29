package com.example.comunicationwearmobile.ui.utils.Helpers.Notification

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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.model.repository.RepositoryIDNotificationSPref
import com.example.comunicationwearmobile.ui.utils.broadcast.NotificationCancelReceiver
import com.example.shared_library.SharedData
import com.example.shared_library.fromByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.locks.ReentrantLock

class NotificationHelper(context: Context) : ContextWrapper(context.applicationContext) {

    private val scope= CoroutineScope(Dispatchers.IO+ SupervisorJob())

    private val lock=ReentrantLock()
    private var manager:NotificationManager?=null
    private val appContext: Context = context


    //ID de la primera notificacion generada
    val ID_NOTIFICATION_FOREGROUND_SERVICE          = 1001


    fun initConfiguration() {
        scope.launch {

            initListSharedPreference()

            manager = getSystemService(NotificationManager::class.java)
        }
    }

    private fun initListSharedPreference() {
        //cuando se inicia por primera vez la aplicacion se borra el contenido
        //del shared preferences con los id de las notificaciones
        val preferences = RepositoryIDNotificationSPref.getInstance(appContext)
        //siempre dejo 1elemento en la lista para que coincida el id con los indices
        var listNotification= arrayListOf(FIRST_ITEM_LIST_NOTIF)

        preferences.clearSharedPreferences()
        preferences.saveArrayList(listNotification, KEY_LIST_NOTIFICATION_SP)
    }

    fun cancelCorutineInit(){
        scope.cancel()
    }


    fun notificationViewedOnWearable(msgBytes: ByteArray) {
        val idMsgMobile:Int = fromByteArray(msgBytes)

        cancelNotificationID(idMsgMobile)
    }

    private fun cancelNotificationID(idMsgMobile: Int){
        //me fijo si la notificacion existe en el shared preference
        if(deleteNewNotificationById(idMsgMobile)==false){
            return
        }

    }

    private fun cancelNotification(notificationId: Int, allNotificationsCanceled: Boolean){

        // Cancelar la notificación con el ID especificado
        manager?.cancel(notificationId)

        //si todas las notificaciones del smrtphone fueron canceladas, entonces se cancela la notificacion del grupo
        if(allNotificationsCanceled){
            manager?.cancel(SharedData.GROUP_ID_NOTIFICATION)
        }
        Log.d(Definition.TAG_DEBUG,"notificacion cancelada")

    }


    private fun getNewIdNotification():Int{
        lock.lock()
        try {
            val preferences = RepositoryIDNotificationSPref.getInstance(appContext)
            val listNotification = preferences.getArrayList(KEY_LIST_NOTIFICATION_SP)
            var newId = 1

            if (listNotification.size!=1) {
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
    fun deleteNewNotificationById(idNotification:Int):Boolean{
        lock.lock()
        try {
            var listIsEmpty =false

            val preferences = RepositoryIDNotificationSPref.getInstance(appContext)
            val listNotification = preferences.getArrayList(KEY_LIST_NOTIFICATION_SP)

            val posList = listNotification.indexOf(idNotification)

            //compruebo que el indice este en la lista
            //y que tengan el primer elemnto dummy
            if ((posList == -1)||(listNotification.size==1))
                return false

            listNotification.removeAt(posList)

            //si solo le queda el elemento dummy en la lista entonces se considera
            //que esta vacia la lista
            if(listNotification.size==1)
                listIsEmpty=true

            //borro la notificacion de la bandeja de notificaciones del S.O
            cancelNotification(idNotification,listIsEmpty)

            preferences.saveArrayList(listNotification, KEY_LIST_NOTIFICATION_SP)

            return true
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


    private fun createChannelAlerts(
        channelId: String ,
        channelName: String,
        channelDescription: String
    ): NotificationCompat.Builder {
        val notificationChannel = NotificationChannel(
            channelId, channelName, NotificationManager.IMPORTANCE_HIGH
        )

        notificationChannel.description = channelDescription
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(true)
        notificationChannel.setShowBadge(true)

        manager?.createNotificationChannel(notificationChannel)

        return NotificationCompat.Builder(appContext, channelId)
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
                .setDeleteIntent(getCancelIntent(SharedData.GROUP_ID_NOTIFICATION)) // Agregar esta línea

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

    //Estas notificaciones se agrupan todas en el mismo grupo de notificaciones
    //son utilizadas principalmente para notificaciones de alerta
    fun showNotificationGeneral(msg:SharedData.MsgNotification):Int{
        // Crear el canal de notificaciones
        val notificationBuilder = createChannelAlerts(
            CHANNEL_ID_ALERTS,
            CHANNEL_ALERTS,
            CHANNEL_DESCRIPTION_ALERTS
        )

        // crear y mostrar(muestra como una notificacion) el grupo de notificaciones
        //En este caso al crear grupo se configuro para que no se muestra una notificacion
        //sobre el grupo sino que se muestra solamente la  notificacion del msg al final
        val groupNotificationBuilder = createGroupNotification()
        manager?.notify(SharedData.GROUP_ID_NOTIFICATION, groupNotificationBuilder.build())

        //se obtiene el numero de notificacion existente del shared preference
        val notificationId = getNewIdNotification()

        // Crear y muestra la notificación del msg recibido. ESta se agrupa en el grupo de notificaciones
        createAlertNotification(notificationBuilder, msg, notificationId)
        manager?.notify(notificationId, notificationBuilder.build())

        return notificationId
    }

    @SuppressLint("MissingPermission")
    fun showNotificationIndependent(context:Context, title:String, msg:String){
        val notificationBuilder = createChannelAlerts(
            CHANNEL_ID_INDEPENDENT,
            CHANNEL_INDEPENDENT,
            CHANNEL_DESCRIPTION_INDEPENDENT
        ).setLocalOnly(true)

        notificationBuilder
            .setSmallIcon(R.drawable.ic_old_person)
            .setContentTitle(title)
            .setContentText(msg)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        //idNotificationIndependent++

        with(NotificationManagerCompat.from(context)) {
            notify(idNotificationIndependent, notificationBuilder.build())
        }
    }

    companion object {
        const val CHANNEL_FOREGROUND_SERVICE = "Foregroundservice"
        const val CHANNEL_ID_FOREGROUND_SERVICE = "Channel_ID_ForegroundService"

        const val GROUP_KEY_NOTIFICATION = "GROUP_NOTIFICATION"

        const val CHANNEL_ALERTS = "Alertas"
        const val CHANNEL_ID_ALERTS = "Channel_ID_Alerts"
        const val CHANNEL_DESCRIPTION_ALERTS = "canal de alertas"

        const val CHANNEL_INDEPENDENT = "Independiente"
        const val CHANNEL_ID_INDEPENDENT="Channel_ID_Independent"
        const val CHANNEL_DESCRIPTION_INDEPENDENT = "canal independiente"

        const val KEY_LIST_NOTIFICATION_SP = "KEY_LIST_NOTIFICATION_SP"

        const val FIRST_ITEM_LIST_NOTIF = 255

        //inicializo las notificaciones independientes con un valor alto para que
        //no se repitan y choquen con las demás notificaciones
        var idNotificationIndependent=800

        var instance: NotificationHelper? = null

        @Synchronized
        fun getInstance(base: Context): NotificationHelper? {
            val appContext = base.applicationContext
            if (instance == null) {
                instance = NotificationHelper(appContext)
            }
            return instance
        }
    }

}
