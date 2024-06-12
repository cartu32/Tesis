package com.example.comunicationwearmobile.models

import com.example.comunicationwearmobile.common.TypeMsg
import java.time.LocalDateTime
import java.time.Month

data class MsgAlertModel(
    var title:String ="",
    var message: String="",
    var typeMsg: TypeMsg,
    var date: LocalDateTime = LocalDateTime.of(2000, Month.JANUARY, 1, 12, 0)
    )

