package com.example.comunicationwearmobile.ui.utils.Helpers.Notification

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.repository.RepositoryContact
import com.example.shared_library.SharedData
import com.example.shared_library.fromByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Locale

object SmsHelper {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var smsMutex= Mutex()

    const val ACTION_SENT = "com.example.abumonitor.SMS_SENT"
    const val ACTION_DELIVERED = "com.example.abumonitor.SMS_DELIVERED"

    // -------------------- API pública --------------------

    fun sendSMSFallDetection(context: Context, msg: ByteArray, geofLatitude: String, geofLongitude: String) {
        var rawMessage=""
        val msgFallDetection: SharedData.MsgFallDetection = fromByteArray(msg)

        val googleMapsUrl = "https://maps.google.com/?q=${geofLatitude},${geofLongitude}"

        //me fijo que se haya pasado como parametro la ubicacion de la caida
        val hasValidLocation =  geofLatitude != "0.0" &&
                                geofLongitude!= "0.0" &&
                                geofLatitude.isNotBlank() &&
                                geofLongitude.isNotBlank()

        //creo el mensaje
        rawMessage = """
                    ${msgFallDetection.title.uppercase(Locale.getDefault())}
                    ${msgFallDetection.message}
                    ${msgFallDetection.fechaHora}                    
                    """.trimIndent()

        //me fijo si se paso una ubicacion para mostrar el mapa o no en el SMS
        if (hasValidLocation) {
            rawMessage+="\n\nUbicación:\n$googleMapsUrl"
        }else{
            rawMessage+="\n\nUbicación no disponible"
        }

        val message = limpiarTextoParaSMS(rawMessage)
        sendSMSToAllContact(context, message)
    }

    fun sendSMSFallContinue(context: Context, msg: ByteArray) {
        val msgFallDetection: SharedData.MsgFallDetection = fromByteArray(msg)

        val rawMessage = """
            🚨${msgFallDetection.title.uppercase(Locale.getDefault())}

            ${msgFallDetection.message}

            📅 ${msgFallDetection.fechaHora}
        """.trimIndent()

        val message = limpiarTextoParaSMS(rawMessage)
        sendSMSToAllContact(context, message)
    }


    fun sendSMSPlainText(context: Context, message: String) {
        sendSMSToAllContact(context, limpiarTextoParaSMS(message))
    }

    fun sendSMSNotifyGeofence(
        context: Context,
        msg: SharedData.MsgNotification,
        geofLatitude: String,
        geofLongitude: String
    ) {
        val googleMapsUrl = "https://maps.google.com/?q=${geofLatitude},${geofLongitude}"

        val rawMessage = """
            ${msg.title.uppercase(Locale.getDefault())}
            ${msg.message}
            ${msg.date} ${msg.hour}

            Ubicación:
            $googleMapsUrl
        """.trimIndent()

        val message = limpiarTextoParaSMS(rawMessage)
        sendSMSToAllContact(context, message)
    }

    /**
     * Enviar a todos los contactos en background, sin bloquear al que llama.
     */
    fun sendSMSToAllContact(context: Context, message: String) {
        val appContext = context.applicationContext

        scope.launch {
            smsMutex.withLock {
                val repositoryContact = RepositoryContact(appContext)
                val listContact = repositoryContact.getAllContactList()

                for (contact in listContact) {
                    // Dejamos respirar al módem / operadora
                    delay(5_000)
                    Log.d(Definition.TAG_DEBUG, "Enviando SMS al contacto: ${contact.name}")
                    sendSMSToContact(appContext, message, contact.telephone)


                }
            }
        }
    }

    /**
     * Enviar un SMS (posiblemente multipart) a un solo número.
     */
    fun sendSMSToContact(context: Context, message: String, telephoneNumber: String) {
        val appContext = context.applicationContext

        // Intent base con info del número (por si querés loguear en el receiver)
        val sentIntentBase = Intent(ACTION_SENT).apply {
            putExtra("phone", telephoneNumber)
        }
        val deliveredIntentBase = Intent(ACTION_DELIVERED).apply {
            putExtra("phone", telephoneNumber)
        }

        // requestCode distinto por número, para no reutilizar siempre el mismo PendingIntent
        val requestCode = telephoneNumber.hashCode()

        val sentIntent = PendingIntent.getBroadcast(
            appContext,
            requestCode,
            sentIntentBase,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val deliveredIntent = PendingIntent.getBroadcast(
            appContext,
            requestCode,
            deliveredIntentBase,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val smsManager = getSmsManager(appContext)

        val parts: ArrayList<String> = smsManager.divideMessage(message)
        val sentIntents = ArrayList<PendingIntent>(parts.size).apply {
            repeat(parts.size) { add(sentIntent) }
        }
        val deliveredIntents = ArrayList<PendingIntent>(parts.size).apply {
            repeat(parts.size) { add(deliveredIntent) }
        }

        Log.d(Definition.TAG_DEBUG, "Mensaje a enviar: $message")
        Log.d(Definition.TAG_DEBUG, "Caracteres: ${message.length}, partes: ${parts.size}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            sendSmsAndroid35Plus(
                telephoneNumber,
                smsManager,
                message,
                parts,
                sentIntents,
                deliveredIntents
            )
        } else {
            /*
            sendSmsPreAndroid35(
                telephoneNumber,
                smsManager,
                parts,
                sentIntents,
                deliveredIntents
            )*/
            sendSmsAndroid35Plus(
                telephoneNumber,
                smsManager,
                message,
                parts,
                sentIntents,
                deliveredIntents
            )
        }
    }

    // -------------------- Implementación por versión --------------------

    private fun sendSmsPreAndroid35(
        telephoneNumber: String,
        smsManager: SmsManager,
        parts: ArrayList<String>,
        sentIntents: ArrayList<PendingIntent>,
        deliveredIntents: ArrayList<PendingIntent>
    ) {
        try {
            smsManager.sendMultipartTextMessage(
                telephoneNumber,
                null,
                parts,
                sentIntents,
                deliveredIntents
            )
        } catch (e: Exception) {
            Log.e(Definition.TAG_DEBUG, "Error al enviar SMS (pre-35): ${e.message}", e)
        }
    }

    /**
     * En Android 15 (API 35) algunas operadoras/dispositivos se ponen quisquillosos con URLs
     * dentro de mensajes multipart. Estrategia:
     * - Si el mensaje tiene URL y se parte en 2+ → enviar el texto sin la URL como multipart
     *   y la URL sola en un SMS aparte.
     * - Si no hay URL (o una sola parte) → enviar como multipart normal.
     */

    private fun sendSmsAndroid35Plus(
        telephoneNumber: String,
        smsManager: SmsManager,
        longMessage: String,
        parts: ArrayList<String>,
        sentIntents: ArrayList<PendingIntent>,
        deliveredIntents: ArrayList<PendingIntent>
    ) {
        try {
            val urlRegex = Regex("\\bhttps?://\\S+")
            val hasUrl = urlRegex.containsMatchIn(longMessage)

            if (hasUrl && parts.size >= 2) {
                val url = urlRegex.find(longMessage)?.value
                if (url != null) {
                    var bodySinUrl = longMessage.replace(url, "").trim()
                    bodySinUrl+="\n"

                    val partsSinUrl: ArrayList<String> = smsManager.divideMessage(bodySinUrl)

                    Log.d(Definition.TAG_DEBUG, "URL detectada: $url")
                    Log.d(
                        Definition.TAG_DEBUG,
                        "Texto sin URL dividido en ${partsSinUrl.size} parte(s)"
                    )

                    val allParts = ArrayList<String>().apply {
                        addAll(partsSinUrl)  // texto sin URL (puede ser 1..N partes)
                        add(url)             // URL como última “parte” del mismo multipart
                    }

                    val sentAll = ArrayList<PendingIntent>(allParts.size).apply {
                        repeat(allParts.size) { add(sentIntents.first()) }
                    }
                    val deliveredAll = ArrayList<PendingIntent>(allParts.size).apply {
                        repeat(allParts.size) { add(deliveredIntents.first()) }
                    }

                    smsManager.sendMultipartTextMessage(
                        telephoneNumber,
                        null,
                        allParts,
                        sentAll,
                        deliveredAll
                    )

                    Log.d(
                        Definition.TAG_DEBUG,
                        "Enviado: texto (${partsSinUrl.size} partes) + URL separada"
                    )
                    return
                }
            }

            // MANUAL_STRATEGY: envío multipart normal
            smsManager.sendMultipartTextMessage(
                telephoneNumber,
                null,
                parts,
                sentIntents,
                deliveredIntents
            )
        } catch (e: Exception) {
            Log.e(Definition.TAG_DEBUG, "Error al enviar SMS (35+): ${e.message}", e)
        }
    }


    // -------------------- Utilidades --------------------

    private fun getSmsManager(context: Context): SmsManager {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val subId = SubscriptionManager.getDefaultSmsSubscriptionId()
                if (subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                    SmsManager.getSmsManagerForSubscriptionId(subId)
                } else {
                    SmsManager.getDefault()
                }
            } else {
                SmsManager.getDefault()
            }
        } catch (_: Exception) {
            SmsManager.getDefault()
        }
    }

    private fun limpiarTextoParaSMS(texto: String): String {
        return texto
            .replace("[áàäâã]".toRegex(), "a")
            .replace("[éèëê]".toRegex(), "e")
            .replace("[íìïî]".toRegex(), "i")
            .replace("[óòöôõ]".toRegex(), "o")
            .replace("[úùüû]".toRegex(), "u")
            .replace("ñ", "n")
            .replace("¡|¿".toRegex(), "")
            .replace("[^\\p{Print}\n\r]".toRegex(), "")
            .trim()
    }

    /**
     * Registrar receivers UNA sola vez (por ejemplo en Application).
     */
    fun registerSMSReceivers(context: Context) {
        val appContext = context.applicationContext

        // Receiver para envío (SENT)
        ContextCompat.registerReceiver(
            appContext,
            object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: Intent?) {
                    val phone = intent?.getStringExtra("phone")

                    when (resultCode) {
                        Activity.RESULT_OK ->
                            Log.d(Definition.TAG_DEBUG, "SMS enviado correctamente a $phone")

                        SmsManager.RESULT_ERROR_GENERIC_FAILURE ->
                            Log.e(Definition.TAG_DEBUG, "Fallo genérico al enviar SMS a $phone")

                        SmsManager.RESULT_ERROR_NO_SERVICE ->
                            Log.e(Definition.TAG_DEBUG, "Sin servicio al enviar SMS a $phone")

                        SmsManager.RESULT_ERROR_NULL_PDU ->
                            Log.e(Definition.TAG_DEBUG, "PDU nulo al enviar SMS a $phone")

                        SmsManager.RESULT_ERROR_RADIO_OFF ->
                            Log.e(Definition.TAG_DEBUG, "Radio apagada al enviar SMS a $phone")

                        SmsManager.RESULT_MODEM_ERROR ->
                            Log.e(Definition.TAG_DEBUG, "Error de módem (16) al enviar SMS a $phone")

                        else ->
                            Log.w(
                                Definition.TAG_DEBUG,
                                "Estado de envío desconocido: $resultCode para $phone"
                            )
                    }
                }
            },
            IntentFilter(ACTION_SENT),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        // Receiver para entrega (DELIVERED)
        ContextCompat.registerReceiver(
            appContext,
            object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: Intent?) {
                    val phone = intent?.getStringExtra("phone")

                    when (resultCode) {
                        Activity.RESULT_OK ->
                            Log.d(Definition.TAG_DEBUG, "SMS entregado correctamente a $phone")

                        else ->
                            Log.e(
                                Definition.TAG_DEBUG,
                                "SMS no fue entregado a $phone (code=$resultCode)"
                            )
                    }
                }
            },
            IntentFilter(ACTION_DELIVERED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    fun onDestroy() {
        scope.cancel()
    }

}
