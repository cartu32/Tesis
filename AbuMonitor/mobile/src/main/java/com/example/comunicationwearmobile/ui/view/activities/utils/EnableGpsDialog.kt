package com.example.comunicationwearmobile.ui.view.activities.utils
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.util.Log
import com.example.abumonitor.constants.Definition


class EnableGpsDialog : Activity() {

    private val REQUEST_ENABLE_GPS =1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Recupera la excepción enviada desde el ForegroundService
        val pendingIntent = intent.getParcelableExtra<PendingIntent>(Definition.RESOLVABLE_API_EXCEPTION)

        try {
            startIntentSenderForResult(
                pendingIntent!!.intentSender ,
                REQUEST_ENABLE_GPS ,
                null ,
                0 ,
                0 ,
                0
            )
        } catch (e: SendIntentException) {
            // Ignore the error
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_GPS) {
            if (resultCode == Activity.RESULT_OK) {
                // GPS activado por el usuario
                Log.d("LocationDialogActivity", "GPS activado")
            } else {
                // El usuario no activó el GPS
                Log.d("LocationDialogActivity", "El usuario no activó el GPS")
            }
        }
        finish() // Cierra la actividad después de manejar la solicitud
    }
}
