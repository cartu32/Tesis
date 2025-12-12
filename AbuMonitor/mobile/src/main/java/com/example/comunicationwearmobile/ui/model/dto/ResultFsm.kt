package com.example.comunicationwearmobile.ui.model.dto

data class ResultFsm(
    var currentState: String?=null,

    var triggerEnter: Boolean=false,
    var triggerExit: Boolean=false,
    var triggerDwellStart: Boolean=false,
    var triggerDwellCancel: Boolean=false,

    var action: String?=null
)
