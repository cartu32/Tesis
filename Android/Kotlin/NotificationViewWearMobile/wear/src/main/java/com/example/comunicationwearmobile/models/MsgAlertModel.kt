package com.example.comunicationwearmobile.models

import com.example.shared_library.SharedData
import java.time.LocalDateTime
import java.time.Month

data class MsgAlertModel(
    var title:String ="",
    var message: String="",
    var typeMsg: SharedData.TypeNotification,
    var date: LocalDateTime = LocalDateTime.of(2000, Month.JANUARY, 1, 12, 0)
    )

