package com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.FSM

import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.GeofenceEventFsmDetector
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.GeofencePermissions
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.GeofenceStatePersistence
import android.content.Context
import android.location.Location
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.comunicationwearmobile.ui.model.dto.DataAreaGeofAux
import com.example.comunicationwearmobile.ui.model.dto.ResultFsm
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.GeofenceFsmEngine

object
GeofenceFSM {

    suspend fun proccessFSM(
        dataArea: DataAreaGeofAux,
        appContext: Context,
        area: EntityAreaGeofence,
        location: Location,
        isFast: Boolean,
        speed: Float,
    ): ResultFsm {

        // 1) obtengo el estado previo de la FSM para esta área
        val prevState: String? = GeofenceStatePersistence.getPrevState(dataArea)

        // 2) Obtengo el evento actual de la fsm según posición + histeresis + filtros
        val currentEventArea =
            GeofenceEventFsmDetector.getEvent(appContext, area, location, prevState, isFast, speed)

        // 3)Obtengo los permisos configurados para esta área (ENTER / EXIT / DWELL)
        val permissions = GeofencePermissions.getPermissionGrantes(dataArea.listIdEventSelected)

        // si es CONTINUE no llamo a la FSM
        if (currentEventArea == com.example.abumonitor.constants.Definition.EVT_CONTINUE) {
            return ResultFsm(
                currentState = prevState,
                triggerEnter = false,
                triggerExit = false,
                triggerDwellStart = false,
                triggerDwellCancel = false,
                action = null
            )
        }

        // 4) Aplico la máquina de estados (FSM) solo cuando hay evento real
        val resultFsm = GeofenceFsmEngine.FSM(
            prevState = prevState,
            event = currentEventArea,
            permissions = permissions
        )

        // retorno si no hubo transición real en la fsm
        val noOp =
            (resultFsm.currentState == prevState) &&
                    (resultFsm.action == null) &&
                    !resultFsm.triggerEnter &&
                    !resultFsm.triggerExit &&
                    !resultFsm.triggerDwellStart &&
                    !resultFsm.triggerDwellCancel

        if (noOp) {
            return resultFsm
        }

        // Logueo solo si realmente paso algo
        GeofenceStatePersistence.logFsmTransition(appContext, resultFsm, currentEventArea)

        // 5) Solo si hay cambio potencial, aplico el filtro de tiempo mínimo + update DB
        val newState = GeofenceStatePersistence.updateInBdCurrentStateArea(
            currentStateArea = resultFsm.currentState,
            prevState = prevState,
            area = area,
            isFast = isFast,
            resultFsm = resultFsm,
            context = appContext,
            speed = speed
        )

        resultFsm.currentState = newState
        return resultFsm
    }
}
