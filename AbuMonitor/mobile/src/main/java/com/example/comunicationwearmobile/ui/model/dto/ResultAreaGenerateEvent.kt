package com.example.comunicationwearmobile.ui.model.dto

data class ResultAreaGenerateEvent(
    var areaId: Long,
    val fireEnter: Boolean,
    val fireExit: Boolean,
    val fireDwellStart: Boolean,
    val fireDwellCancel: Boolean
)
