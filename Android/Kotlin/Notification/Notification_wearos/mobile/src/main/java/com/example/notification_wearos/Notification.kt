package com.example.notification_wearos

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Intent


class Notification(context: Context) {
    var notificationId=0
    val canalId="canalId"
    val canalNombre= "canalNombre"
    var mcontext: Context = context

    public fun createChannelNotification(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val canalImportancia=NotificationManager.IMPORTANCE_HIGH
            val canal=NotificationChannel(canalId,canalNombre,canalImportancia)

            val manager= mcontext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            //val manager= getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(canal)

            manager.createNotificationChannel(canal)
        }
    }

    public fun createNotification(title: String, content: String) {
        val resultInt= Intent (mcontext.applicationContext, MainActivity2::class.java)
        val resultPendingIntent=TaskStackBuilder.create(mcontext.applicationContext).run {
            addNextIntentWithParentStack(resultInt)
            getPendingIntent(0,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notification=NotificationCompat.Builder(mcontext,canalId).also {
            it.setContentTitle(title)
            it.setContentText(content)
            it.setSmallIcon(R.drawable.ic_launcher_foreground)
            it.priority=NotificationCompat.PRIORITY_HIGH
            it.setContentIntent(resultPendingIntent)
            it.setAutoCancel(true)
        }.build()

        val notificationManager=NotificationManagerCompat.from(mcontext)
        if (ActivityCompat.checkSelfPermission(mcontext,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){
            return;
        }

        notificationId++;
        notificationManager.notify(notificationId,notification)
    }


    public fun showNotification(title:String,content:String){
        createChannelNotification()
        createNotification(title,content)
    }

}