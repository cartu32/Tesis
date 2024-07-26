package com.example.comunicationwearmobile.common

import android.app.Application
import android.app.KeyguardManager
import android.content.Context
import android.os.PowerManager
import android.widget.Toast
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter


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