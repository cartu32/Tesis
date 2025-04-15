package com.example.comunicationwearmobile.ui.utils.Mannager

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.shared_library.SharedData
import com.example.shared_library.fromByteArray

class SmsManager {
    private val telephoneNumber = "1134926279"

    fun sendSMS(context: Context, message: ByteArray) {
        try {
            val msgFallDetection: SharedData.MsgFallDetection = fromByteArray(message)
            val phoneNumber = telephoneNumber

            if (phoneNumber.isNotBlank()) {
                val smsManager = context.getSystemService(SmsManager::class.java)
                smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    msgFallDetection.message + msgFallDetection.fechaHora,
                    null,
                    null
                )
                Log.d(Definition.TAG_DEBUG, "SMS enviado exitosamente.")
            } else {
                Log.w(Definition.TAG_DEBUG, "Número de teléfono no disponible o vacío.")
            }
        } catch (e: Exception) {
          Log.e(Definition.TAG_DEBUG, "Error al enviar el SMS: ${e.message}", e)
        }
    }



}
