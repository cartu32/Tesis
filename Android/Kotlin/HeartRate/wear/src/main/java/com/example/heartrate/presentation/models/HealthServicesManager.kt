package com.example.heartrate.presentation.models

import android.content.Context
import android.util.Log
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeAvailability
import androidx.health.services.client.data.DeltaDataType
import androidx.health.services.client.data.SampleDataPoint
import androidx.health.services.client.getCapabilities
import androidx.health.services.client.unregisterMeasureCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private const val TAG = "WATCHMAIN"


class HealthServicesManager(context: Context) {
    private val measureClient = HealthServices.getClient(context).measureClient

    suspend fun hasHeartRateCapability() = runCatching {
        val capabilities = measureClient.getCapabilities()
        (DataType.HEART_RATE_BPM in capabilities.supportedDataTypesMeasure)
    }.getOrDefault(false)

    companion object {
        fun Any.log(message: String = "") {
            Log.e("ADAMX", "$this $message")
        }
    }
    /**
     * Returns a cold flow. When activated, the flow will register a callback for heart rate data
     * and start to emit messages. When the consuming coroutine is canceled, the measure callback
     * is unregistered.
     *
     * [callbackFlow] creates a  bridge between a callback-based API and Kotlin flows.
     */
    @ExperimentalCoroutinesApi
    fun heartRateMeasureFlow(): Flow<MeasureMessage> = callbackFlow {
        val callback = object : MeasureCallback {
            override fun onAvailabilityChanged(dataType: DeltaDataType<* , *> , availability: Availability) {
                // Only send back DataTypeAvailability (not LocationAvailability)
                if (availability is DataTypeAvailability) {
                    trySendBlocking(MeasureMessage.MeasureAvailability(availability))
                }
            }

            override fun onDataReceived(data: DataPointContainer) {
                val heartRateBpm = data.getData(DataType.HEART_RATE_BPM)
                Log.d(TAG, "💓 Received heart rate: ${heartRateBpm.first().value}")
                trySendBlocking(MeasureMessage.MeasureData(heartRateBpm))
            }
        }

        Log.d(TAG, "⌛ Registering for data...")
        measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, callback)

        awaitClose {
            Log.d(TAG, "👋 Unregistering for data")
            runBlocking {
                measureClient.unregisterMeasureCallback(DataType.HEART_RATE_BPM, callback)
            }
        }
    }
    private fun checkHeartRateAvailable() {
        CoroutineScope(Dispatchers.IO).launch {
            val measureCapabilities = measureClient.getCapabilities()
          //  val exerciseCapabilities = exerciseClient.getCapabilities()
            val supportsHeartRate =
                DataType.HEART_RATE_BPM in measureCapabilities.supportedDataTypesMeasure
            "Heart rate supported: $supportsHeartRate".log()
            //"Exercise supported: $exerciseCapabilities".log()
            "Measure supported: $measureCapabilities".log()
        }
    }
}

sealed class MeasureMessage {
    class MeasureAvailability(val availability: DataTypeAvailability) : MeasureMessage()
    class MeasureData(val data: List<SampleDataPoint<Double>>) : MeasureMessage()
}
