package com.example.comunicationwearmobile.ui.utils.Helpers.Geofences

import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.dto.PermissionsArea
import com.example.comunicationwearmobile.ui.model.dto.ResultFsm

object GeofenceFsmEngine {

    fun FSM(
        prevState: String?,
        event: String,
        permissions: PermissionsArea
    ): ResultFsm {

        var newState       = prevState
        var fireEnter      = false
        var fireExit       = false
        var fireDwellStart = false
        var fireDwellCancel= false
        var action: String?= null

        with(Definition) {
            when (prevState) {
                ST_INIT -> {
                    when (event) {
                        EVT_ENTER -> {
                            newState = ST_INSIDE
                            if (permissions.enter && permissions.dwell) {
                                fireEnter = true
                                fireDwellStart = true
                                action = ACT_ENTER_AND_DWELL
                            } else if (permissions.enter) {
                                fireEnter = true
                                action = ACT_ENTER_ONLY
                            } else if (permissions.dwell) {
                                fireDwellStart = true
                                action = ACT_DWELL_ONLY
                            } else {
                                action = ACT_SILENT_ENTER
                            }
                        }

                        EVT_EXIT -> {
                            newState = ST_OUTSIDE
                            if (permissions.exit) {
                                action = ACT_EXIT
                            } else {
                                action = ACT_SILENT_EXIT
                            }
                        }

                        EVT_CONTINUE -> {
                            // No cambio de estado ni acción
                        }
                    }
                }

                ST_INSIDE -> {
                    when (event) {
                        EVT_EXIT -> {
                            newState = ST_OUTSIDE
                            fireDwellCancel=true
                            if (permissions.exit) {
                                fireExit = true
                                action = ACT_EXIT
                            } else {
                                action = ACT_SILENT_EXIT
                            }
                        }

                        EVT_ENTER, EVT_CONTINUE -> {
                            // Sigue adentro, no hay cambio de estado
                        }
                    }
                }

                ST_OUTSIDE -> {
                    when (event) {
                        EVT_ENTER -> {
                            // Entra al área
                            newState = ST_INSIDE
                            if (permissions.enter && permissions.dwell) {
                                fireEnter = true
                                fireDwellStart = true
                                action = ACT_ENTER_AND_DWELL
                            } else if (permissions.enter) {
                                fireEnter = true
                                action = ACT_ENTER_ONLY
                            } else if (permissions.dwell) {
                                fireDwellStart = true
                                action = ACT_DWELL_ONLY
                            } else {
                                action = ACT_SILENT_ENTER
                            }
                        }

                        EVT_EXIT, EVT_CONTINUE -> {
                            // Sigue afuera, no cambio de estado
                        }
                    }
                }
            }
        }

        return ResultFsm(
            currentState      = newState,
            triggerEnter      = fireEnter,
            triggerExit       = fireExit,
            triggerDwellStart = fireDwellStart,
            triggerDwellCancel= fireDwellCancel,
            action            = action
        )
    }
}
