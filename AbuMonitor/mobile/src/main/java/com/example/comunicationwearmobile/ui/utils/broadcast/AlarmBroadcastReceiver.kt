package com.example.comunicationwearmobile.ui.utils.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.repository.RepositoryConfigAppSPref
import com.example.comunicationwearmobile.ui.utils.Helpers.Alarm.AlarmHelper
import com.example.comunicationwearmobile.ui.utils.Tools
import com.example.comunicationwearmobile.ui.utils.services.GeofencesServices

class AlarmBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(Definition.TAG_DEBUG, "Alarmas de chequeos ejecutada")

        val serviceIntent = Intent(context, GeofencesServices::class.java).apply {
            action = intent.action
            intent.extras?.let { putExtras(it) }
        }

        ContextCompat.startForegroundService(context, serviceIntent)
    }


}

