package com.example.comunicationwearmobile.models.entities

import com.example.shared_library.SharedData

data class DataClass_MsgAlertState(
    val alertsList: List<SharedData.MsgNotification>? = emptyList()
  )