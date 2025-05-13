@file:Suppress("DEPRECATION")

package com.example.comunicationwearmobile.utils

import android.app.Application
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.widget.Toast
import com.example.comunicationwearmobile.utils.services.MobileDataListenerService
import com.example.shared_library.SharedData
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

fun showToast(mcontext: Context, msg: String){
    Toast.makeText(mcontext,msg, Toast.LENGTH_SHORT).show()
}

fun sendMessageMobile(context: Context , path:String , body: ByteArray?){
    val serviceIntent = Intent(context , MobileDataListenerService::class.java).apply {
        putExtra(SharedData.ParamIntent.MESSAGE_PATH.name , path)
        putExtra(SharedData.ParamIntent.MESSAGE_BODY.name , body)
    }
    context.startService(serviceIntent)
}




fun isScreenOn(application: Application): Boolean {
    val powerManager = application.getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isInteractive
}

fun isScreenLock(application: Application): Boolean {
    val keyguardManager = application.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    return keyguardManager.isKeyguardLocked
}
