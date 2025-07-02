package com.example.comunicationwearmobile.ui.utils.Mannager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.shared_library.SharedData
import com.example.shared_library.fromByteArray

class SmsManagerCustom {
    private val telephoneNumber = "1134926279"

    fun sendSMSFallDetection(context: Context, message: ByteArray) {
        try {
            val msgFallDetection: SharedData.MsgFallDetection = fromByteArray(message)
            val phoneNumber = telephoneNumber

            val message = """
                ${msgFallDetection.title}
                ${msgFallDetection.message}
                ${msgFallDetection.fechaHora}
                """.trimIndent()

            if (phoneNumber.isNotBlank()) {
                val smsManager = context.getSystemService(SmsManager::class.java)
                smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    message,
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

    fun sendSMSNotifyGeofence(context: Context, msg: SharedData.MsgNotification) {
        try {
            val rawMessage = """
                ${msg.title}
                ${msg.message}
                ${msg.date}  ${msg.hour}
            """.trimIndent()

            val message = limpiarTextoParaSMS(rawMessage)

            val sentIntent = PendingIntent.getBroadcast(
                context,
                0,
                Intent("SMS_SENT"),
                PendingIntent.FLAG_IMMUTABLE
            )

            val deliveredIntent = PendingIntent.getBroadcast(
                context,
                0,
                Intent("SMS_DELIVERED"),
                PendingIntent.FLAG_IMMUTABLE
            )

            val smsManager = SmsManager.getDefault()
            val parts = smsManager.divideMessage(message)

            val sentIntents = ArrayList<PendingIntent>().apply {
                repeat(parts.size) { add(sentIntent) }
            }

            val deliveredIntents = ArrayList<PendingIntent>().apply {
                repeat(parts.size) { add(deliveredIntent) }
            }

            Log.d(Definition.TAG_DEBUG, "Mensaje a enviar: $message")
            Log.d(Definition.TAG_DEBUG, "Caracteres: ${message.length}, partes: ${parts.size}")

            smsManager.sendMultipartTextMessage(
                telephoneNumber,
                null,
                parts,
                sentIntents,
                deliveredIntents
            )

        } catch (e: Exception) {
            Log.e("SMS", "Error al enviar el SMS: ${e.message}", e)
        }
    }

    private fun limpiarTextoParaSMS(texto: String): String {
        return texto
            .replace("[áàäâã]".toRegex(), "a")
            .replace("[éèëê]".toRegex(), "e")
            .replace("[íìïî]".toRegex(), "i")
            .replace("[óòöôõö]".toRegex(), "o")
            .replace("[úùüû]".toRegex(), "u")
            .replace("ñ", "n")
            .replace("¡", "")
            .replace("¿", "")
            .replace("[^\\p{Print}\n\r]".toRegex(), "")
    }
}



