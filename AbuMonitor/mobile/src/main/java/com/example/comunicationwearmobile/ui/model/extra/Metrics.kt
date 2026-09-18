package com.example.comunicationwearmobile.ui.model.extra

/** Calcula métricas base usadas por los filtros y la FSM (distancia, radio, accuracy, borde). */
data class Metrics(
    val distance: Float,
    val radiusMeters: Float,
    val accuracy: Float,
    val distanceToBorder: Float
)
