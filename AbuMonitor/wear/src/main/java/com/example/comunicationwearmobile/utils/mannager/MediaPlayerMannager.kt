package com.example.comunicationwearmobile.utils.mannager

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.VibratorManager
import androidx.annotation.RequiresApi
import com.example.comunicationwearmobile.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException


object MediaPlayerManager {

    private var mediaPlayer: MediaPlayer? = null

    private fun hasBuiltInSpeaker(context: Context): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        return devices.any { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
    }

    fun playAlarmSound(context: Context, resId: Int) {
        if (!hasBuiltInSpeaker(context)) return

        stopAndRelease()

        try {
            val afd = context.resources.openRawResourceFd(resId) ?: return

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )

                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()

                setOnCompletionListener {
                    stopAndRelease()
                }

                setOnErrorListener { _, _, _ ->
                    stopAndRelease()
                    true
                }

                prepare()
                start()
            }

        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    fun stopAlarmSound() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } finally {
            stopAndRelease()
        }
    }

    private fun stopAndRelease() {
        try {
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
        }
    }

    fun playSoundSystem(context: Context, soundId: Int) {
        val notification: Uri = RingtoneManager.getDefaultUri(soundId)
        val ringtone: Ringtone = RingtoneManager.getRingtone(context, notification)
        ringtone.play()
    }
}
