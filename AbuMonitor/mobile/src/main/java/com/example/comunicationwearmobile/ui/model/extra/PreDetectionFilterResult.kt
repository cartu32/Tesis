package com.example.comunicationwearmobile.ui.model.extra

data class PreDetectionFilterResult (
    val passed: Boolean,
    val meterForEnter: Float=0f,
    val meterForExit: Float=0f
)