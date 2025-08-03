package com.example.comunicationwearmobile.utils.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.view.activities.MainActivity

class ForegroundService : Service() {
    companion object {
        const val NOTIF_ID = 123
        const val CANAL_ID = "canal_mi_servicio"
        var isForegroundRunning = false
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isForegroundRunning) {
            startForegroundServiceProperly()
        }
        return START_STICKY
    }

    private fun createChannel() {
        val canal = NotificationChannel(CANAL_ID, "Mi Servicio FGS", NotificationManager.IMPORTANCE_LOW)
        NotificationManagerCompat.from(this).createNotificationChannel(canal)
    }

    private fun startForegroundServiceProperly() {
        val notif = buildNotification("Servicio activo")
        ServiceCompat.startForeground(
            this,
            NOTIF_ID,
            notif,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC // Alternativa segura
        )
        isForegroundRunning = true
        Log.d("ABU_MONITOR", "ForegroundService activo")
    }

    private fun buildNotification(texto: String): Notification {
        val intent = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, CANAL_ID)
            .setContentTitle("Mi FGS especial")
            .setContentText(texto)
            .setSmallIcon(R.drawable.ic_old_people)
            .setOngoing(true)
            .setContentIntent(pIntent)

        val status = Status.Builder().addTemplate(texto).build()
        val ongoing = OngoingActivity.Builder(applicationContext, NOTIF_ID, builder)
            .setAnimatedIcon(R.drawable.ic_old_people)
            .setStaticIcon(R.drawable.ic_old_people)
            .setTouchIntent(pIntent)
            .setStatus(status)
            .build()
        ongoing.apply(applicationContext)

        return builder.build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isForegroundRunning = false
    }
}
