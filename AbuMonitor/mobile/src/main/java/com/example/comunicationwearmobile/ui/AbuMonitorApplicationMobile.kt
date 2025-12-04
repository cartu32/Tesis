package com.example.abumonitor


import android.app.Application
import android.util.Log
import android.widget.Toast
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.common.SharedVariables
import com.example.comunicationwearmobile.ui.model.datasource.local.dbInitializer
import com.example.comunicationwearmobile.ui.model.repository.RepositoryConfigAppSPref
import com.example.comunicationwearmobile.ui.utils.Helpers.Alarm.AlarmHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.GeofenceEventProcessorHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.InitializerApplication
import com.example.comunicationwearmobile.ui.utils.Helpers.Notification.NotificationHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.Notification.SmsHelper
import com.example.comunicationwearmobile.ui.utils.Tools
import com.example.comunicationwearmobile.ui.utils.broadcast.AlarmBroadcastReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//import leakcanary.LeakCanary


class AbuMonitorApplicationMobile : Application() {
    override fun onCreate() {
        super.onCreate()
        InitializerApplication.init(this)
      }


    override fun onTerminate() {
        super.onTerminate()
        InitializerApplication.onTerminate()
    }

}

