package com.example.abumonitor.data.datasource.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.data.model.EntityColor
import com.example.abumonitor.data.model.EntityContact
import com.example.abumonitor.data.model.EntityEvent
import com.example.abumonitor.data.model.EntityFirstTimeState
import com.example.abumonitor.data.model.EntityPriority
import com.example.abumonitor.data.model.EntityScheduledAssistance
import com.example.abumonitor.utils.Converters
import com.example.comunicationwearmobile.ui.model.datasource.local.DaoSecurityZoneTimeRange
import com.example.comunicationwearmobile.ui.model.entities.EntityAreaEventCrossRef
import com.example.comunicationwearmobile.ui.model.entities.EntitySecurityZoneTimeRange
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@Database(
    entities =
    [
        EntityAreaGeofence::class , EntityColor::class , EntityContact::class ,
        EntityEvent::class, EntityPriority::class, EntityScheduledAssistance::class,
        EntityAreaEventCrossRef::class, EntityFirstTimeState::class,
        EntitySecurityZoneTimeRange::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AbuMonitorDatabase : RoomDatabase() {

    abstract fun entityAreaGeofenceDao(): DaoAreaGeofence
    abstract fun entityColorDao(): DaoColor
    abstract fun entityContactDao(): DaoContact
    abstract fun entityEventDao(): DaoEvent
    abstract fun entityPriorityDao(): DaoPriority
    abstract fun entityScheduledAssistanceDao(): DaoScheduledAssistance
    abstract fun firstTimeStateDao(): DaoFirstTimeState
    abstract fun joinAreaGeofenceDao(): DaoJoinAreaGeofence
    abstract fun entitySecurityZoneTimeRangeDao(): DaoSecurityZoneTimeRange

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

            private const val COLOR_BLUE = "Azul"
            private const val COLOR_GREEN = "Verde"
            private const val COLOR_RED = "Rojo"
            private const val COLOR_GRIS = "Gris"

            private const val EVENT_ENTER = "Entrar"
            private const val EVENT_EXIT = "Salir"
            private const val EVENT_STAY = "Permanecer"

            private const val PRIORITY_LOW = "Baja"
            private const val PRIORITY_MEDIUM = "Media"
            private const val PRIORITY_HIGH = "Alta"


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


            insertColorInDataBase(database)
            insertEventInDatabase(database)
            insertPriorityInDataBase(database)
            insertContactInDataBase(database)
            Log.d(Definition.TAG_DEBUG,"Inserto registros")



        }

        private suspend fun insertContactInDataBase(database: AbuMonitorDatabase) {
            val entityContact1 = EntityContact(ID_INITIAL , "Esteban" , "1134926279")
            val entityContact2 = EntityContact(ID_INITIAL + 1 , "Ramon" , "1134926279")
            val daoContact = database.entityContactDao()

            daoContact.insertContact(entityContact1)
            daoContact.insertContact(entityContact2)

        }

        private suspend fun insertPriorityInDataBase(database: AbuMonitorDatabase) {
            val entityPriority1 = EntityPriority(ID_INITIAL , description = PRIORITY_LOW)
            val entityPriority2 =
                EntityPriority(ID_INITIAL + 1 , description = PRIORITY_MEDIUM)
            val entityPriority3 =
                EntityPriority(ID_INITIAL + 2 , description = PRIORITY_HIGH)
            val daoPririty = database.entityPriorityDao()

            daoPririty.insertPriority(entityPriority1)
            daoPririty.insertPriority(entityPriority2)
            daoPririty.insertPriority(entityPriority3)

        }

        private suspend fun insertEventInDatabase(database: AbuMonitorDatabase) {
            val entityEvent1 = EntityEvent(ID_INITIAL , description = EVENT_ENTER)
            val entityEvent2 = EntityEvent(ID_INITIAL + 1 , description = EVENT_EXIT)
            val entityEvent3 = EntityEvent(ID_INITIAL + 2 , description = EVENT_STAY)
            val daoEntity = database.entityEventDao()

            daoEntity.insertEven(entityEvent1)
            daoEntity.insertEven(entityEvent2)
            daoEntity.insertEven(entityEvent3)

        }

        private suspend fun insertColorInDataBase(database: AbuMonitorDatabase) {
            val daoColor = database.entityColorDao()
            val entityColor1 = EntityColor(ID_INITIAL , description = COLOR_BLUE)
            val entityColor2 = EntityColor(ID_INITIAL + 1 , description = COLOR_GREEN)
            val entityColor3 = EntityColor(ID_INITIAL + 2 , description = COLOR_RED)
            val entityColor4 = EntityColor(ID_INITIAL + 3 , description = COLOR_GRIS)


            daoColor.insertColor(entityColor1)
            daoColor.insertColor(entityColor2)
            daoColor.insertColor(entityColor3)
            daoColor.insertColor(entityColor4)

        }



    }
}
