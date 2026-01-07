package com.example.comunicationwearmobile.ui.common

import kotlinx.coroutines.sync.Mutex

object SharedVariables {
    const val USER_ADMIN = "admin"
    var user=""
    var password=""
    var timeAlarmChecksFirstTime:Long=0
    var isOpenAppFirsTime = false

    var mutexAssistanceDateAlarm= Mutex()
}