package com.example.abumonitor.data.datasource.local

import android.content.Context
import android.graphics.Color
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.data.model.EntityContact
import com.example.abumonitor.data.model.EntityEvent
import com.example.abumonitor.data.model.EntityFirstTimeState
import com.example.abumonitor.data.model.EntityPriority
import com.example.abumonitor.data.model.EntityScheduledAssistance
import com.example.abumonitor.utils.Converters
import com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity.DaoSecurityZoneTimeRange
import com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity.DaoAreaGeofence
import com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity.DaoContact
import com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity.DaoEvent
import com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity.DaoFirstTimeState
import com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity.DaoPriority
import com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity.DaoScheduledAssistance
import com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity.DaoTypeArea
import com.example.comunicationwearmobile.ui.model.datasource.local.daoPojo.DaoJoinAreaGeofence
import com.example.comunicationwearmobile.ui.model.entities.EntityAreaEventCrossRef
import com.example.comunicationwearmobile.ui.model.entities.EntitySecurityZoneTimeRange
import com.example.comunicationwearmobile.ui.model.entities.EntityTypeArea
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@Database(
    entities =
    [
        EntityAreaGeofence::class ,EntityContact::class ,EntityTypeArea::class,
        EntityEvent::class, EntityPriority::class, EntityScheduledAssistance::class,
        EntityAreaEventCrossRef::class, EntityFirstTimeState::class,
        EntitySecurityZoneTimeRange::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AbuMonitorDatabase : RoomDatabase() {

    abstract fun entityAreaGeofenceDao(): DaoAreaGeofence
    abstract fun entityContactDao(): DaoContact
    abstract fun entityEventDao(): DaoEvent
    abstract fun entityPriorityDao(): DaoPriority
    abstract fun entityScheduledAssistanceDao(): DaoScheduledAssistance
    abstract fun firstTimeStateDao(): DaoFirstTimeState
    abstract fun joinAreaGeofenceDao(): DaoJoinAreaGeofence
    abstract fun entitySecurityZoneTimeRangeDao(): DaoSecurityZoneTimeRange
    abstract fun entityTypeAreaDao(): DaoTypeArea


    companion object {
        @Volatile
        private var INSTANCE: AbuMonitorDatabase? = null


        fun getDatabase(context: Context, scope: CoroutineScope): AbuMonitorDatabase {
            return INSTANCE ?: synchronized(this) {
                val callback = DatabaseCallback(scope)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AbuMonitorDatabase::class.java,
                    Definition.DATABASE_NAME
                )
                    .addCallback(callback)
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance

                // 🔹 Espera que se ejecute onCreate y se complete
                runBlocking {
                    callback.completion.await()
                }

                Log.d(Definition.TAG_DEBUG,"ejecuta paso 2")

                scope.launch(Dispatchers.IO) {
                    if (!getFirstState(instance)) {
                        saveFirstState(instance)
                    }
                }

                Log.d(Definition.TAG_DEBUG,"Ejecuta paso 3")
                instance
            }.also { database ->
                database.openHelper.writableDatabase
                Log.d(Definition.TAG_DEBUG,"ejecuta paso 4")
            }
        }


        private suspend fun getFirstState(instance: AbuMonitorDatabase?): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    instance?.firstTimeStateDao()?.getFirstTimeState()?.isFirstTime ?: false
                } catch (e: Exception) {
                    Log.e(Definition.TAG_DEBUG, "Error al leer el estado de la base de datos")
                    false
                }
            }
        }

        private suspend fun saveFirstState(instance: AbuMonitorDatabase) {
            try {
                val state = EntityFirstTimeState(isFirstTime = true)
                instance.firstTimeStateDao().insert(state)
            } catch (e: Exception) {
                Log.e(Definition.TAG_DEBUG,"Error al guardar el estado de la base de datos")
            }
        }


        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }


    private class DatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {

        internal val completion = CompletableDeferred<Unit>()
        private var wasInitializedinOnCreate = false

        companion object {

            private const val ID_INITIAL = 1

        }

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            Log.d(Definition.TAG_DEBUG,"SE EJECUTA EN ON CREATE DE DATABASE")
            INSTANCE?.let { database ->
                wasInitializedinOnCreate=true
                scope.launch(Dispatchers.IO){
                    insertDataInDataBase(database)
                    Log.d(Definition.TAG_DEBUG,"Ejecuta paso 1")
                    Log.d(Definition.TAG_DEBUG,"Complete en onCreate")
                    completion.complete(Unit)
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)

            if(!wasInitializedinOnCreate){
                Log.d(Definition.TAG_DEBUG,"Complete en onOPen")
                completion.complete(Unit)
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
}
