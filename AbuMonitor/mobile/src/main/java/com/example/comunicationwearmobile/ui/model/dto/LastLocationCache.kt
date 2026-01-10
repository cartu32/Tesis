package com.example.comunicationwearmobile.ui.model.dto

data class CachedLocation(
    val lat: Double,
    val lon: Double,
    val timeMs: Long
)

//Este object se usa para almacenar la ultima ubicacion conocida del dispositivo
//que es actualizada cada vez que se obtiene una nueva ubicacion en geofencesServices
// (en la funcion startLocationUpdates). Esto se hace de manera segura con volatile

object LastLocationCache {
    // Una sola referencia: lectura/escritura atómica (por ser referencia + @Volatile)
    @Volatile var last: CachedLocation? = null
}