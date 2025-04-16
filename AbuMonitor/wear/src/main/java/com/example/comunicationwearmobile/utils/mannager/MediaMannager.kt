package com.example.comunicationwearmobile.utils.mannager

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.VibratorManager
import androidx.annotation.RequiresApi
import com.example.comunicationwearmobile.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object MediaMannager {
    private var mediaPlayer: MediaPlayer? = null

    @RequiresApi(Build.VERSION_CODES.S)
    fun generateVibration(app: Application) {
        CoroutineScope(Dispatchers.IO).launch {
            val vibratorManager =
                app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    500,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        }
    }

    fun playSoundAlarm(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        val hasSpeaker = devices.any { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }

        if (hasSpeaker) {
            // Libera el anterior si existe
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }

            // Crea uno nuevo
            val player = MediaPlayer.create(context, R.raw.siren)
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )

            // Libera automáticamente cuando termine de sonar
            player.setOnCompletionListener {
                it.release()
                mediaPlayer = null
            }

            player.start()
            mediaPlayer = player
        }
    }

    fun stopSoundAlarm(context: Context){
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()

        }
    }
}
