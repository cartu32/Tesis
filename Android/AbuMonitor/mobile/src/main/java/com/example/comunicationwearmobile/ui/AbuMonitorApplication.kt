package com.example.abumonitor


import android.app.Application
import android.content.Intent
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.utils.services.GeofencesServices


class AbuMonitorApplication: Application() {
    private lateinit var geofencesServices: GeofencesServices

    override fun onCreate() {
        super.onCreate()
        Definition.application = this


    }


}