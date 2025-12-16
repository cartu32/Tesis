package com.example.comunicationwearmobile.ui.utils.Helpers.Geofences

import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.dto.PermissionsArea

object GeofencePermissions {

    fun getPermissionGrantes(listIdEventSelected: List<Int>): PermissionsArea {
        val permissions = PermissionsArea()

        permissions.enter = listIdEventSelected.contains(Definition.GEOFENCE_EVENT_ID_ENTER)
        permissions.exit  = listIdEventSelected.contains(Definition.GEOFENCE_EVENT_ID_EXIT)
        permissions.dwell = listIdEventSelected.contains(Definition.GEOFENCE_EVENT_ID_DWELL)

        return permissions
    }
}
