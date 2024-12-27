package com.example.abumonitor.data.repository

import androidx.lifecycle.LiveData
import com.example.abumonitor.data.datasource.local.DaoAreaGeofence
import com.example.abumonitor.data.datasource.local.DaoJoinAreaGeofence
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.data.model.JoinAreaGeofence

class RepositoryAreaGeofence(
    val daoAreaGeofence: DaoAreaGeofence? ,
    val daoJoinAreaGeofence: DaoJoinAreaGeofence? ,
) {
    val listAreaGeofence:LiveData<List<EntityAreaGeofence>> = daoAreaGeofence!!.getAllAreas()
    val listJoinAreaGeofence:LiveData<List<JoinAreaGeofence>> = daoJoinAreaGeofence!!.getJoinAreaGeofence()

    suspend fun insertAreaGeonfence(area:EntityAreaGeofence ) {
      daoAreaGeofence?.insertAreaGeofence(area)
    }

    suspend fun getAreaWithId(areaId:Int):EntityAreaGeofence{
        return daoAreaGeofence?.getAreaWithId(areaId) ?:
        throw NoSuchElementException("Id de Area No Encontrada $areaId")
    }

    suspend fun deleteArea(area: EntityAreaGeofence){
        daoAreaGeofence?.deleteArea(area)
    }

    suspend fun deleteAreaWithId(idArea:Int): Int? {
        return daoAreaGeofence?.deleteAreaWithId(idArea)
    }

    suspend fun updateArea(area: EntityAreaGeofence): Int? {
        return daoAreaGeofence?.updateArea(area)
    }

}