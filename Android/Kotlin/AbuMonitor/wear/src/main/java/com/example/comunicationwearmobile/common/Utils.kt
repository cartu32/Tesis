@file:Suppress("DEPRECATION")

package com.example.comunicationwearmobile.common

import android.app.Application
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.comunicationwearmobile.models.MobileDataListenerService
import com.example.shared_library.SharedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val mutex = Mutex()

fun showToast(mcontext: Context, msg: String){
    Toast.makeText(mcontext,msg, Toast.LENGTH_SHORT).show()
}

fun dateToString(dateTime: LocalDateTime):String{
    val formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val formatterDate=dateTime.format(formatter)

    return formatterDate
}

fun getHour(dateTime: LocalDate):String{
    val formatter=DateTimeFormatter.ofPattern("HH:mm")
    val formatterDate=dateTime.format(formatter)

    return formatterDate
}

suspend fun sendMessageMobile(context: Context,path:String,body:ByteArray){
    mutex.withLock {
        val serviceIntent = Intent(context , MobileDataListenerService::class.java).apply {
            putExtra(SharedData.ParamIntent.MESSAGE_PATH.name , path)
            putExtra(SharedData.ParamIntent.MESSAGE_BODY.name , body)
        }
        context.startService(serviceIntent)
    }
}

fun getDate(dateTime: LocalTime):String{
    val formatter=DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val formatterDate=dateTime.format(formatter)

    return formatterDate
}


fun isScreenOn(application: Application): Boolean {
    val powerManager = application.getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isInteractive
}

fun isScreenLock(application: Application): Boolean {
    val keyguardManager = application.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    return keyguardManager.isKeyguardLocked
}

@RequiresApi(Build.VERSION_CODES.S)
fun generateVibration(app: Application) {

    CoroutineScope(Dispatchers.IO).launch {
        if (Build.VERSION.SDK_INT >= 31) {
            val vibratorManager =
                app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            val v = app.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
}
