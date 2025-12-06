package com.example.abumonitor.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.datasource.local.AbuMonitorDatabase
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.comunicationwearmobile.ui.model.pojo.JoinAreaGeofence
import com.example.comunicationwearmobile.ui.model.dto.DataAreaGeofAux
import com.example.comunicationwearmobile.ui.model.entities.EntityAreaEventCrossRef
import com.example.comunicationwearmobile.ui.model.entities.EntityAreaRuntimeState
import com.example.comunicationwearmobile.ui.model.pojo.AreaGeofenceForMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
class RepositoryAreaDB (context: Context) {

    private val database = AbuMonitorDatabase.getDatabase(context.applicationContext)

    private val daoAreaGeofence = database.entityAreaGeofenceDao()
    private val daoSecurityZoneTimeRange = database.entitySecurityZoneTimeRangeDao()
    private val daoAreaRuntimeState = database.entityAreaRuntimeState()

    companion object {
        @Volatile private var INSTANCE: RepositoryAreaDB? = null

        fun getInstance(context: Context): RepositoryAreaDB {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RepositoryAreaDB(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    suspend fun getJoinAreaGeofence(idArea: Long): JoinAreaGeofence? {
        return withContext(Dispatchers.IO) {
            try {
                val areaGeof = daoAreaGeofence.getJoinAreaGeofence(idArea = idArea)
                areaGeof
            } catch (e: Exception) {
                e.printStackTrace()
                null  // Retorna null si hay un error
            }
        }
    }

    suspend fun getAreaGeofenceById(idArea: Long): EntityAreaGeofence? {
        return withContext(Dispatchers.IO) {
            try {
                val areaGeof = daoAreaGeofence.getAreaWithId(idArea = idArea)
                areaGeof
            } catch (e: Exception) {
                e.printStackTrace()
                null  // Retorna null si hay un error
            }
        }
    }

    suspend fun insertAreaGeofence(area: DataAreaGeofAux?,isActivate:Boolean=true): Long {
        return withContext(Dispatchers.IO) {
            try {
                //hago el insert atomico
                database.withTransaction {
                    insertArea(area,isActivate)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Definition.ERROR_INSERT_BD_GEOF
            }
        }
    }

    private suspend fun insertArea(data: DataAreaGeofAux?, isActivate: Boolean): Long {
        if (data == null)
            throw IllegalStateException("Error data es null")

        val newAreaId = data.entityAreaGeofence.let {
            daoAreaGeofence.insertAreaGeofence(it)
        }
        //si inserto bien el area, inserto el time range
        newAreaId.let {
            insertSecurityZone(data, newAreaId)

            //creo el registro asociado al estado del area
            insertAreaRuntimeState(newAreaId,isActivate)

            // Insertar relaciones N a N con eventos
            insertAreaEventCrossRef(data, newAreaId)
        }
        return newAreaId
    }

    private suspend fun insertAreaRuntimeState(newAreaId: Long, activate: Boolean) {
        val areaState= EntityAreaRuntimeState(newAreaId,activate)
        val result=daoAreaRuntimeState.insertAreaState(areaState)

        if (result == -1L)
            throw IllegalStateException("Error insertando AreaState")
    }

    private suspend fun insertAreaEventCrossRef(data: DataAreaGeofAux, newAreaId: Long) {
        for (eventId in data.listIdEventSelected) {
            val crossRef = EntityAreaEventCrossRef(id_area = newAreaId, id_event = eventId)

            val result = daoAreaGeofence.insertAreaEventCrossRef(crossRef)
            if (result == -1L)
                throw IllegalStateException("Error insertando AreaEventCrossRef")
        }
    }

    private fun insertSecurityZone(data: DataAreaGeofAux, newAreaId: Long) {
        //si el area no es una zona segura, entonces secZoneTimeRange es igual a null
        //esto se establece en PropertiesGeofenceActivity cuando se apreta el boton save
        data.secZoneTimeRange.let {
            //si es zona segura, se guarda el time range en la tabla de security zone
            it?.id_area = newAreaId
            if (it != null) {
                daoSecurityZoneTimeRange.insertSecurityZoneTimeRange(it)
            }
        }
    }


    suspend fun deleteAreaWithId(idArea: Long?): Int? {
        return withContext(Dispatchers.IO) {
            daoAreaGeofence.deleteAreaWithId(idArea)
        }
    }

    suspend fun updateAreaGeof(area: EntityAreaGeofence): Int {
        return withContext(Dispatchers.IO) {
            area.let {
                daoAreaGeofence.updateArea(area)
            }
        }
    }

    suspend fun updateAreaState(areaState:EntityAreaRuntimeState):Int{
        return withContext(Dispatchers.IO){
            daoAreaRuntimeState.updateAreaState(areaState)
        }
    }

    suspend fun getListAreasForMap(): List<AreaGeofenceForMap> {
        return withContext(Dispatchers.IO) {
            // obtenemos las áreas básicas
            val basicAreas = daoAreaGeofence.getBasicAreas()

            val areasForMap = basicAreas.map { area ->
                val eventsForArea = daoAreaGeofence.getEventByArea(area.id_area)

                AreaGeofenceForMap(
                    id_area = area.id_area,
                    latitude = area.latitude,
                    longitude = area.longitude,
                    meters = area.meters,
                    id_type_area = area.id_type_area,
                    type_area = area.type_area,
                    color = area.color,
                    events = eventsForArea
                )
            }

            // se retorna la lista construida
            areasForMap
        }
    }

    /**
    * Devuelve todas las áreas activas junto con sus eventos
    * mapeadas a DataAreaGeofAux (para pasárselas directo al activador).
    */
    suspend fun getAllActiveAreasWithEvents(): List<DataAreaGeofAux> {
        val rows = daoAreaGeofence.getAreasActivated()

        return rows.map { row ->

            val events: MutableList<Int> = row.list_id_event
                .split(",")
                .mapNotNull { it.toIntOrNull() }
                .toMutableList()


            val entity = EntityAreaGeofence(
                id_area = row.id_area,
                latitude = row.latitude,
                longitude = row.longitude,
                meters = row.meters,
                dwell_time = row.dwell_time,
                description = row.description,
                id_type_area = row.id_type_area,
                id_priority = row.id_priority
            )

            val entityAreaRuntimeState=EntityAreaRuntimeState(
                id_area = row.id_area,
                is_activated_geof = row.is_activated_geof,
                prev_state_machine = row.prev_state_machine,
                last_update_time = row.last_update_time
            )

            DataAreaGeofAux(
                entityAreaGeofence = entity,
                listIdEventSelected = events,
                secZoneTimeRange = null, // acá después podés traer la zona segura si querés
                entityAreaRuntimeState = entityAreaRuntimeState
            )
        }
    }

}
