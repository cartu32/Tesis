package com.example.comunicationwearmobile.interfaces

import com.google.android.gms.wearable.MessageEvent

interface MsgHandlerFromWear {
    fun handleMessage(messageEvent: MessageEvent)
}