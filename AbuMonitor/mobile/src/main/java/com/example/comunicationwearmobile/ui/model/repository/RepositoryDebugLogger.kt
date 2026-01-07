package com.example.comunicationwearmobile.ui.model.repository

import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.locks.ReentrantLock

object RepositoryDebugLogger {

    private const val TAG = "ABUMONITOR_DEBUG_LOGGER"
    private const val FILE_NAME = "geofence_debug_log.txt"

    // SimpleDateFormat NO es thread-safe, lo vamos a usar adentro del lock
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    // Lock para sincronizar acceso al formato de fecha y al archivo
    private val lock = ReentrantLock()

    fun log(context: Context, msg: String) {
        lock.lock()
        try {
            val timestamp = sdf.format(Date())
            val fullMsg = "$timestamp  |  $msg\n"

            // También lo mandamos a Logcat
            Log.d(TAG, fullMsg)

            // Escribir de forma segura al archivo
            val file = context.getFileStreamPath(FILE_NAME)
            file.appendText(fullMsg)
        } catch (e: Exception) {
            Log.e(TAG, "Error writing debug log: ${e.message}")
        } finally {
            lock.unlock()
        }
    }
}
