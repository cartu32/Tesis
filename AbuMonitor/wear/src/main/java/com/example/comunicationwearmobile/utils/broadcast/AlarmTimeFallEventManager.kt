package com.example.comunicationwearmobile.utils.broadcast

import androidx.lifecycle.MutableLiveData


object AlarmTimeFallEventManager {
    val NOTIFYING_ALARM_FALL = 1
    val RESETING_ALARM_FALL = 2
    val CANCEL_ALARM_FALL = 3

    private val _eventAlarm = MutableLiveData<Int>()
    val eventAlarm: MutableLiveData<Int> get() = _eventAlarm

    fun notifyAlarm(){
        _eventAlarm.postValue(NOTIFYING_ALARM_FALL)
    }

    fun resetAlarm(){
        _eventAlarm.postValue(RESETING_ALARM_FALL)
    }

    fun cancelAlarm(){
        _eventAlarm.postValue(CANCEL_ALARM_FALL)
    }
}