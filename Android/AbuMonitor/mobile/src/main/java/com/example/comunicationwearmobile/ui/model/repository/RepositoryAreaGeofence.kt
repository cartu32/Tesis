package com.example.abumonitor.data.repository

import androidx.lifecycle.LiveData
import com.example.abumonitor.data.datasource.local.DaoAreaGeofence
import com.example.abumonitor.data.datasource.local.DaoJoinAreaGeofence
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.data.model.JoinAreaGeofence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RepositoryAreaGeofence(
    val daoAreaGeofence: DaoAreaGeofence? ,
    val daoJoinAreaGeofence: DaoJoinAreaGeofence? ,
) {
    val listAreaGeofence:LiveData<List<EntityAreaGeofence>> = daoAreaGeofence!!.getAllAreas()
    val listJoinAreaGeofence:LiveData<List<JoinAreaGeofence>> = daoJoinAreaGeofence!!.getJoinAreaGeofence()

    suspend fun insertAreaGeofence(area: EntityAreaGeofence?): Long? {
        return withContext(Dispatchers.IO) {
            try {
                val resultado = area?.let {
                    daoAreaGeofence?.insertAreaGeofence(it)
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
            daoAreaGeofence?.getAreaWithId(areaId)
        }
    }

    suspend fun deleteArea(area: EntityAreaGeofence){
        daoAreaGeofence?.deleteArea(area)
    }

    suspend fun deleteAreaWithId(idArea: Long?): Int? {
        return withContext(Dispatchers.IO) {
            daoAreaGeofence?.deleteAreaWithId(idArea)
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