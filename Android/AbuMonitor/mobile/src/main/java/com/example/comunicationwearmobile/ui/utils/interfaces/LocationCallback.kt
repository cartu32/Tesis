package com.example.comunicationwearmobile.ui.utils.interfaces

import android.content.Context
import android.location.Location

interface LocationCallback {
    fun alertNoGps()
    fun positionUpdate(location: Location)
    fun getApplicationContext(): Context
}
