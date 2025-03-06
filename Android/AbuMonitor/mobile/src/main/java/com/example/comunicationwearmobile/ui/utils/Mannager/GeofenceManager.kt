package com.example.comunicationwearmobile.ui.utils.Mannager

import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.data.repository.RepositoryAreaGeofence
import com.example.comunicationwearmobile.ui.model.maps.AreaGeofenceInMap
import com.example.comunicationwearmobile.ui.utils.Tools
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class GeofenceManager(private val repositoryArea: RepositoryAreaGeofence) {
    //defino un objeto areaTemporary para ir almacenando temporalmentelos datos de la nueva area
    //que va ir agregando el usuario a traves del mapa. Luego estos datos se agregan al listado
    //de areas en el mapa
    private var areaTemporary: AreaGeofenceInMap? = AreaGeofenceInMap()

    //se crea este listado de areas en el map, para poder mostrar graficamente
    //las areas de geofencing en el mapa
    private var listAreaGeofence: ArrayList<AreaGeofenceInMap>? = ArrayList()


    fun storeInTemporaryArea(latLng: LatLng, circle: Circle?, marker: Marker?) {
        areaTemporary?.apply {
            this.marker = marker
            this.radius = Definition.GEOFENCE_RADIUS_DEFAULT
            this.circle = circle
            this.latLng = latLng
        }
    }


    suspend fun saveGeofenceAreaInBD(
        itemEvent: Int?,
        itemColor: Int = 1,
        idContact: Int? = null,
        itemPriority: Int?,
        isSecurityZone: Boolean?,
        dwellTime: Int?,
        description: String?,
        meters: String
    ): Long? {
        var idNewArea: Long? = null
        var entityAreaGeofence: EntityAreaGeofence? = null

        storeNewAreaInListAreas()
        entityAreaGeofence = createObjEntityAreaGeofence(
            itemEvent,
            itemPriority,
            isSecurityZone,
            dwellTime,
            description,
            meters,
            idContact,
            itemColor
        )

        idNewArea = saveInDatabase(entityAreaGeofence)
        storeNewIdAreaInListAreas(idNewArea)

        return idNewArea
    }

    private fun storeNewIdAreaInListAreas(idNewArea: Long?) {
        if (idNewArea != null) {
            listAreaGeofence?.last()?.id_area = idNewArea
        } else {
            Log.e(Definition.TAG_DEBUG, "Error idNewArea is null")
        }
    }

    private fun createObjEntityAreaGeofence(
        itemEvent: Int?,
        itemPriority: Int?,
        isSecurityZone: Boolean?,
        dwellTime: Int?,
        description: String?,
        meters: String,
        idContact: Int?,
        itemColor: Int?
    ): EntityAreaGeofence? {

        val idTypeAreaGeofence: Int = 1

        val lastArea = listAreaGeofence?.lastOrNull() ?: return null

        return if (isSecurityZone != null && dwellTime != null && description != null
            && itemEvent != null && itemPriority != null && itemColor != null
        ) {
            EntityAreaGeofence(
                latitude = lastArea.latLng?.latitude?.toString() ?: "0",
                longitude = lastArea.latLng?.longitude?.toString() ?: "0",
                meters = meters.toIntOrNull() ?: 0,
                security_zone = isSecurityZone,
                dwell_time = dwellTime,
                description = description,
                id_color = itemColor,
                id_event = itemEvent,
                id_priority = itemPriority,
                id_contact = idContact,
                id_type_area = idTypeAreaGeofence
            )
        } else {
            null
        }
    }

    private suspend fun saveInDatabase(entityAreaGeofence: EntityAreaGeofence?): Long? {
        var lastIdArea: Long? = null
        lastIdArea = repositoryArea.insertAreaGeofence(entityAreaGeofence)

        return lastIdArea
    }

    private fun storeNewAreaInListAreas() {
        //agrego en la lista el area geofen que se fue llenando anterirormente en areaTemporary
        areaTemporary?.let {
            //le asigno el id al area
            listAreaGeofence?.add(it)
        }

        // Reinicia el estado de areaTemporary para el siguiente uso
        areaTemporary = AreaGeofenceInMap()
    }

    fun cancelInMap() {
        //como el usuario cancela la opcion se borra el circulo y el marcador en el mapa mapa
        areaTemporary?.marker?.remove()
        areaTemporary?.circle?.remove()
    }

    fun updateCircleRadius(radius: Double) {
        areaTemporary?.circle?.radius = radius.toDouble()
        areaTemporary?.radius = radius.toDouble()
    }

    fun deleteAreaInListAreasAndMap(idAreaSelected: Long?) {
        listAreaGeofence?.find { it.id_area == idAreaSelected }?.let { areaData ->
            areaData.circle?.remove()
            areaData.marker?.remove()
            listAreaGeofence?.remove(areaData)
        }
    }

    suspend fun deleteAreaGeofence(idAreaSelected: Long): Int {
        val rowEliminated = repositoryArea.deleteAreaWithId(idAreaSelected) ?: 0
        return rowEliminated
    }


    suspend fun determineWithinAnyCircle(latLng: LatLng): Long? {
        var idAreaSlected:Long?=null

        listAreaGeofence?.forEach { areaData ->
            val circleRadius = areaData.radius
            val areaLatLng = areaData.latLng

            if (circleRadius != null && areaLatLng != null) {
                // Realizar la verificación si los valores necesarios no son nulos
                val isInside = Tools.isPointInsideCircle(latLng, areaLatLng, circleRadius)

                if (isInside) {
                    idAreaSlected=areaData.id_area
                    Log.d(Definition.TAG_DEBUG, "Está dentro del área: ${areaData.id_area}")
                } else {
                    Log.d(Definition.TAG_DEBUG, "No se encuentra en el área: ${areaData.id_area}")
                }
            } else {
                Log.w(Definition.TAG_DEBUG, "Datos incompletos para evaluar el área: ${areaData.id_area}")
            }
        }

        return idAreaSlected
    }

    suspend fun clear(){
        listAreaGeofence?.forEach { areaData ->
            areaData.circle?.remove()
            areaData.marker?.remove()
        }

        //limpio el listado de area geofncing
        listAreaGeofence?.clear()
        listAreaGeofence = null
        areaTemporary=null


    }
}
