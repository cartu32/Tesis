package com.example.abumonitor.data.repository

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.abumonitor.data.datasource.local.AbuMonitorDatabase
import com.example.abumonitor.data.datasource.local.DaoAreaGeofence
import com.example.abumonitor.data.datasource.local.DaoJoinAreaGeofence
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.data.model.JoinAreaGeofence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RepositoryAreaDB(context: Context, scope: CoroutineScope) {
    private val database = AbuMonitorDatabase.getDatabase(context, scope)

    private val daoAreaGeofence = database.entityAreaGeofenceDao()
    private val daoJoinAreaGeofence = database.joinAreaGeofence()

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

    suspend fun insertAreaGeofence(area: EntityAreaGeofence?): Long? {
        return withContext(Dispatchers.IO) {
            try {
                val resultado = area?.let {
                    daoAreaGeofence.insertAreaGeofence(it)
                }
                if (resultado != -1L) resultado else null
            } catch (e: Exception) {
                e.printStackTrace()
                null  // Retorna null si hay un error
            }
        }
    }


    suspend fun getAreaWithId(areaId:Long):EntityAreaGeofence?{
        return withContext(Dispatchers.IO) {
            daoAreaGeofence.getAreaWithId(areaId)
        }
    }

    suspend fun deleteArea(area: EntityAreaGeofence){
        daoAreaGeofence.deleteArea(area)
    }

    suspend fun deleteAreaWithId(idArea: Long?): Int? {
        return withContext(Dispatchers.IO) {
            daoAreaGeofence.deleteAreaWithId(idArea)
        }
    }

    suspend fun updateArea(area: EntityAreaGeofence): Int? {
        return withContext(Dispatchers.IO) {
            area.let {
                daoAreaGeofence?.updateArea(area)
            }
        }
    }

}