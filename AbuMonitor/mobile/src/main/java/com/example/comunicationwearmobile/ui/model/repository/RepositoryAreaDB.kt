package com.example.abumonitor.data.repository

import android.content.Context
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.datasource.local.AbuMonitorDatabase
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.data.model.JoinAreaGeofence
import com.example.comunicationwearmobile.ui.model.dto.DataAreaGeofAux
import com.example.comunicationwearmobile.ui.model.entities.EntityAreaEventCrossRef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RepositoryAreaDB(context: Context, scope: CoroutineScope) {
    private val database = AbuMonitorDatabase.getDatabase(context, scope)

    private val daoAreaGeofence = database.entityAreaGeofenceDao()
    private val daoJoinAreaGeofence = database.joinAreaGeofenceDao()
    private val daoSecurityZoneTimeRange=database.entitySecurityZoneTimeRangeDao()

    companion object {
        @Volatile private var INSTANCE: RepositoryAreaDB? = null

        fun getInstance(context: Context,scope: CoroutineScope): RepositoryAreaDB {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RepositoryAreaDB(context,scope).also { INSTANCE = it }
            }
        }
    }

    suspend fun getListAllAreas(): List<EntityAreaGeofence> {
        return withContext(Dispatchers.IO) {
            val list=daoAreaGeofence.getAllAreas()
            list
        }
    }

    suspend fun getJoinAreaGeofence(idArea: Long): JoinAreaGeofence? {
        return withContext(Dispatchers.IO) {
            try {
                val areaGeof = daoJoinAreaGeofence.getJoinAreaGeofence(idArea = idArea)
                areaGeof
            } catch (e: Exception) {
                e.printStackTrace()
                null  // Retorna null si hay un error
            }
        }
    }

    suspend fun insertAreaGeofence(area: DataAreaGeofAux?): Long {
        return withContext(Dispatchers.IO) {
            try {
                insertArea(area)
            } catch (e: Exception) {
                e.printStackTrace()
                Definition.ERROR_INSERT_BD_GEOF
            }
        }
    }

    private suspend fun insertArea(data: DataAreaGeofAux?): Long {
        if (data == null)
            return Definition.ERROR_INSERT_BD_GEOF

        val newAreaId = data.entityAreaGeofence.let {
            daoAreaGeofence.insertAreaGeofence(it)
        }
        //si inserto bien el area, inserto el time range
        newAreaId.let {
            //si el area no es una zona segura, entonces secZoneTimeRange es igual a null
            //esto se establece en PropertiesGeofenceActivity cuando se apreta el boton save
            data.secZoneTimeRange.let {
                //si es zona segura, se guarda el time range en la tabla de security zone
                it?.id_area=newAreaId
                if (it != null) {
                    daoSecurityZoneTimeRange.insertSecurityZoneTimeRange(it)
                }
            }
        }

        // Insertar relaciones N a N con eventos
        for (eventId in data.listIdEventSelected) {
            val crossRef = EntityAreaEventCrossRef(id_area = newAreaId, id_event = eventId)

            val result = daoAreaGeofence.insertAreaEventCrossRef(crossRef)
            if (result == -1L)
                return Definition.ERROR_INSERT_BD_GEOF
        }

        return newAreaId
    }



    suspend fun deleteAreaWithId(idArea: Long?): Int? {
        return withContext(Dispatchers.IO) {
            daoAreaGeofence.deleteAreaWithId(idArea)
        }
    }

    suspend fun updateArea(area: EntityAreaGeofence): Int {
        return withContext(Dispatchers.IO) {
            area.let {
                daoAreaGeofence.updateArea(area)
            }
        }
    }

}