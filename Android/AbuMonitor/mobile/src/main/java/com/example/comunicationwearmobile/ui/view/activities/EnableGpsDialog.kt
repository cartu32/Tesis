package com.example.comunicationwearmobile.ui.view.activities
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.util.Log


class EnableGpsDialog : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Recupera la excepción enviada desde el ForegroundService
        val pendingIntent = intent.getParcelableExtra<PendingIntent>("resolution")

        try {
            startIntentSenderForResult(
                pendingIntent!!.intentSender ,
                1001 ,
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
        if (requestCode == 1001) {
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
