package com.example.comunicationwearmobile.ui.model.dto

// AreaTrack actúa como un estado temporal por área.
// Mantiene historial en RAM para filtrar ruido del GPS y aplicar
// debounce, hysteresis y confirmación temporal de eventos.
data class AreaTrack(
    var lastEventAt: Long = 0L,        // Último ENTER/EXIT emitido (anti-spam)
    var lastFlipAt: Long = 0L,         // Último cambio INSIDE↔OUTSIDE aceptado
    var lastLocAt: Long = 0L,          // Timestamp del último fix procesado
    var lastLat: Double = 0.0,         // Latitud del último fix válido
    var lastLon: Double = 0.0,         // Longitud del último fix válido
    var stationarySince: Long = 0L,    // Desde cuándo está considerado quieto
    var stationaryAccumMove: Float = 0f,// Movimiento acumulado estando quieto
    var insideStreak: Int = 0,         // Lecturas consecutivas INSIDE
    var outsideStreak: Int = 0,        // Lecturas consecutivas OUTSIDE
    var lastDistToCenter: Float = -1f  // Última distancia al centro del área
)
