package com.example.comunicationwearmobile.utils.mannager

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.VibratorManager
import androidx.annotation.RequiresApi

object VibrateMannager {

    @RequiresApi(Build.VERSION_CODES.S)
    fun generateVibration(app: Application,millisecond:Long) {
            val vibratorManager =
                app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    millisecond,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
    }
}