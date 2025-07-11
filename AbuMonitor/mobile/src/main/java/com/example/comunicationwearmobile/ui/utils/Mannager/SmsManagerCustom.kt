package com.example.comunicationwearmobile.ui.utils.Mannager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.repository.RepositoryContact
import com.example.shared_library.SharedData
import com.example.shared_library.fromByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class SmsManagerCustom {

    fun sendSMSFallDetection(context: Context, message: ByteArray){
        val repositoryContact= RepositoryContact(context, CoroutineScope(Dispatchers.IO))

        val listContact=repositoryContact.getAllContactList()
        val msgFallDetection: SharedData.MsgFallDetection = fromByteArray(message)

        val message = """
                ${msgFallDetection.title}
                ${msgFallDetection.message}
                ${msgFallDetection.fechaHora}
                """.trimIndent()

        for(contact in listContact){
            Log.d(Definition.TAG_DEBUG,"enviando sms de caidas al contacto: ${contact.name}")
            sendSMSFallToContact(context, message, contact.telephone)

        }

    }

    fun sendSMSFallToContact(context: Context, message: String, phoneNumber: String) {
        try {
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

    fun sendSMSNotifyGeofence(
        context: Context,
        msg: SharedData.MsgNotification,
        geofLatitude: String,
        geofLongitude: String
    ) {
        val repositoryContact= RepositoryContact(context, CoroutineScope(Dispatchers.IO))
        val listContact=repositoryContact.getAllContactList()

        val googelmapsURL =" https://maps.google.com/?q=${geofLatitude},${geofLongitude}"

        val rawMessage = """
                ${msg.title}
                ${msg.message}
                ${msg.date}  ${msg.hour}
                ${googelmapsURL}
            """.trimIndent()

        val message = limpiarTextoParaSMS(rawMessage)

        for(contact in listContact){
            Log.d(Definition.TAG_DEBUG,"enviando sms geofence al contacto: ${contact.name}")
            sendSMSNotifyGeofenceToContact(context, message, contact.telephone)

        }
    }

    fun sendSMSNotifyGeofenceToContact(context: Context, message: String,telephoneNumber:String) {
        try {


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



