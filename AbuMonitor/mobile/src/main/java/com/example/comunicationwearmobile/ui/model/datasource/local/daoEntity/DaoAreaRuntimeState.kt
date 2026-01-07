package com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.comunicationwearmobile.ui.model.entities.EntityAreaRuntimeState

@Dao
interface DaoAreaRuntimeState {
    @Insert
    suspend fun insertAreaState(areaState:EntityAreaRuntimeState):Long

    // Obtener todas el estado de las areas
    @Query("SELECT * FROM Area_Runtime_State")
    fun getAllAreasState(): List<EntityAreaRuntimeState>

    @Query("SELECT * FROM Area_Runtime_State WHERE id_area = :idArea")
    suspend fun getAreaStateWithId(idArea: Long): EntityAreaRuntimeState

    @Query("""
        UPDATE Area_Runtime_State 
        SET prev_state_machine = :state
        WHERE id_area = :idArea
    """)
    suspend fun updateAreaStateFsm(idArea:Long,state:String):Int

    @Query("""
        UPDATE Area_Runtime_State 
        SET is_activated_geof = :isActivated
        WHERE id_area IN (:listAreasIds)
    """)
    fun updateAreaActivated(listAreasIds:List<Long>, isActivated: Boolean):Int
}