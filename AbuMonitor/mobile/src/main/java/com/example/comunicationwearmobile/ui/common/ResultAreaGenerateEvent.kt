package com.example.comunicationwearmobile.ui.common

data class ResultAreaGenerateEvent(
    var areaId: Long,
    val fireEnter: Boolean,
    val fireExit: Boolean,
    // en el futuro podés agregar:
    // val fireDwellStart: Boolean,
    // val fireDwellCancel: Boolean
)
