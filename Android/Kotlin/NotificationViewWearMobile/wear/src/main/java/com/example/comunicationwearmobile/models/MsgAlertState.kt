package com.example.comunicationwearmobile.models

import com.example.shared_library.SharedData

data class MsgAlertState(
    val alertsList: List<SharedData.MsgNotification>? = emptyList()
  )