package com.example.comunicationwearmobile.ui.utils.Helpers.Network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.model.repository.RepositoryDebugLogger
import com.example.comunicationwearmobile.ui.model.repository.RepositoryLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object NetWorkHelper {

     // Mínimo intervalo entre pedidos de ubicación provocados por cambios de red
    private const val MIN_NETWORK_REFRESH_INTERVAL_MS = 30_000L // 30s, ajustable
    private var lastNetworkRefreshTimeMs: Long = 0L

    private var networkCallback: ConnectivityManager.NetworkCallback? = null


    private lateinit var connectivityManager: ConnectivityManager


    fun initNetworkHelper(context: Context, serviceScope: CoroutineScope){
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        registerNetworkCallback(context,serviceScope)
    }

    private fun registerNetworkCallback(context: Context, serviceScope: CoroutineScope) {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            // Si quisieras solo WiFi, podrías agregar:
            // .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                RepositoryDebugLogger.log(
                    context,
                    "NETWORK_CHANGE: onAvailable net=${network.hashCode()} -> pido ubicación puntual"
                )
                refreshLocationAfterNetworkChange(context, serviceScope)
            }

            override fun onLost(network: Network) {
                RepositoryDebugLogger.log(
                    context,
                    "NETWORK_CHANGE: onLost net=${network.hashCode()} -> pido ubicación puntual"
                )
                refreshLocationAfterNetworkChange(context,serviceScope)
            }
        }

        connectivityManager.registerNetworkCallback(request, networkCallback!!)
    }

    fun unregisterNetworkCallback() {
        try {
            networkCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
        } catch (_: Exception) {
            // por si ya estaba unregister
        }
    }
    private fun refreshLocationAfterNetworkChange(context: Context, serviceScope: CoroutineScope) {

        // --- 0) Anti-spam por flapping de red ---
        val now = System.currentTimeMillis()
        val elapsed = now - lastNetworkRefreshTimeMs

        if (elapsed < MIN_NETWORK_REFRESH_INTERVAL_MS) {
            RepositoryDebugLogger.log(
                context,
                "NETWORK_CHANGE: ignorado (solo pasaron ${elapsed}ms; min=$MIN_NETWORK_REFRESH_INTERVAL_MS)"
            )
            return
        }
        lastNetworkRefreshTimeMs = now

        // --- 1) Lógica original ---
        serviceScope?.launch {
            try {
                // 1) Ver si tiene sentido hacer algo (que haya áreas activas)
                val repoAreas = RepositoryAreaDB.getInstance(context)
                val activeAreas = repoAreas.getAllActiveAreasWithEvents()

                if (activeAreas.isEmpty()) {
                    RepositoryDebugLogger.log(
                        context,
                        "NETWORK_CHANGE: no hay áreas activas, no pido ubicación"
                    )
                    return@launch
                }

                // 2) Pedir UNA sola ubicación liviana
                val repoLoc = RepositoryLocation.getInstance(context)
                val loc = repoLoc.getSingleBalancedLocation()

                if (loc != null) {
                    RepositoryDebugLogger.log(
                        context,
                        "NETWORK_CHANGE: ubicación puntual -> " +
                                "lat=${loc.latitude}, lon=${loc.longitude}, acc=${loc.accuracy}"
                    )
                    // Con esto ya "despertás" el proveedor y refrescás geofences
                } else {
                    RepositoryDebugLogger.log(
                        context,
                        "NETWORK_CHANGE: no se pudo obtener ubicación puntual"
                    )
                }

            } catch (e: Exception) {
                RepositoryDebugLogger.log(
                    context,
                    "NETWORK_CHANGE: excepción al pedir ubicación puntual: ${e.message}"
                )
            }
        }
    }

}