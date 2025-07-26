package com.example.comunicationwearmobile.ui.model.datasource.local

import android.content.Context
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.datasource.local.AbuMonitorDatabase
import com.example.abumonitor.data.model.EntityContact
import com.example.abumonitor.data.model.EntityEvent
import com.example.abumonitor.data.model.EntityFirstTimeState
import com.example.abumonitor.data.model.EntityPriority
import com.example.comunicationwearmobile.ui.model.entities.EntityTypeArea
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class dbInitializer {
    private val ID_INITIAL = 1

    suspend fun checkAndInitDatabase(context: Context) {
        val db = AbuMonitorDatabase.getDatabase(context)

        withContext(Dispatchers.IO) {
            val isFirstTime = db.firstTimeStateDao().getFirstTimeState()?.isFirstTime != true

            if (isFirstTime) {
                // Insertar datos iniciales
                insertDataInDataBase(db)

                // Guardar que ya se hizo
                db.firstTimeStateDao().insert(EntityFirstTimeState(isFirstTime = true))

                Log.d(Definition.TAG_DEBUG, "Datos iniciales insertados correctamente")
            } else {
                Log.d(Definition.TAG_DEBUG, "La base ya estaba inicializada")
            }
        }
    }

    /**
     * Inserta los datos iniciales solo cuando se crea la base de datos.
     */
    private suspend fun insertDataInDataBase(database: AbuMonitorDatabase) {


        insertEventInDatabase(database)
        insertPriorityInDataBase(database)
        insertContactInDataBase(database)
        insertTypeAreaInDataBase(database)

        Log.d(Definition.TAG_DEBUG,"Inserto registros")

    }

    private suspend fun insertTypeAreaInDataBase(database: AbuMonitorDatabase) {
        with(Definition) {
            val entityTypeArea1 = EntityTypeArea(TYPE_AREA_ID_NORMAL, TYPE_AREA_DESC_NORMAL, TYPE_AREA_COLOR_NORMAL)
            val entityTypeArea2 = EntityTypeArea(TYPE_AREA_ID_SECURITY_ZONE, TYPE_AREA_DESC_SECURITY_ZONE, TYPE_AREA_COLOR_SECURITY_ZONE)
            val entityTypeArea3 = EntityTypeArea(TYPE_AREA_ID_ASSISTANCE, TYPE_AREA_DESC_ASSISTANCE, TYPE_AREA_COLOR_ASSISTANCE)
            val daoTypeArea = database.entityTypeAreaDao()

            daoTypeArea.insertTypeArea(entityTypeArea1)
            daoTypeArea.insertTypeArea(entityTypeArea2)
            daoTypeArea.insertTypeArea(entityTypeArea3)
        }
    }

    private suspend fun insertContactInDataBase(database: AbuMonitorDatabase) {
        val entityContact1 = EntityContact(ID_INITIAL , "Esteban" , "1134926279")
        val daoContact = database.entityContactDao()

        daoContact.insertContact(entityContact1)

    }

    private suspend fun insertPriorityInDataBase(database: AbuMonitorDatabase) {
        with(Definition) {
            val entityPriority1 = EntityPriority(PRIORITY_ID_LOW, PRIORITY_DESC_LOW)
            val entityPriority2 = EntityPriority(PRIORITY_ID_MEDIUM, PRIORITY_DESC_MEDIUM)
            val entityPriority3 = EntityPriority(PRIORITY_ID_HIGH, PRIORITY_DESC_HIGH)
            val daoPririty = database.entityPriorityDao()

            daoPririty.insertPriority(entityPriority1)
            daoPririty.insertPriority(entityPriority2)
            daoPririty.insertPriority(entityPriority3)
        }
    }

    private suspend fun insertEventInDatabase(database: AbuMonitorDatabase) {
        with(Definition) {
            val entityEvent1 = EntityEvent(GEOFENCE_EVENT_ID_ENTER, GEOFENCE_EVENT_DESC_ENTER)
            val entityEvent2 = EntityEvent(GEOFENCE_EVENT_ID_EXIT, GEOFENCE_EVENT_DESC_EXIT)
            val entityEvent3 = EntityEvent(GEOFENCE_EVENT_ID_DWELL, GEOFENCE_EVENT_DESC_DWELL)
            val daoEntity = database.entityEventDao()

            daoEntity.insertEven(entityEvent1)
            daoEntity.insertEven(entityEvent2)
            daoEntity.insertEven(entityEvent3)
        }

    }


}


