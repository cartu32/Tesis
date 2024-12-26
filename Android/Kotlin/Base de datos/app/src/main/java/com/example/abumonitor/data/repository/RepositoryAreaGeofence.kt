package com.example.abumonitor.data.repository

import androidx.lifecycle.LiveData
import com.example.abumonitor.data.datasource.local.DaoAreaGeofence
import com.example.abumonitor.data.model.EntityAreaGeofence

class RepositoryAreaGeofence(private val daoAreaGeofence: DaoAreaGeofence?) {
    val listAreaGeofence:LiveData<List<EntityAreaGeofence>> = daoAreaGeofence!!.getAllAreas()

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