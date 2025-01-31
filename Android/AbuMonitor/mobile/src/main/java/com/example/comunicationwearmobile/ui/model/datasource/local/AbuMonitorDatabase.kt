package com.example.abumonitor.data.datasource.local

import android.content.ContentValues.TAG
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
import com.example.abumonitor.data.model.EntityReminder
import com.example.abumonitor.data.model.EntityTypeArea
import com.example.abumonitor.utils.Converters
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Database(
    entities = [EntityAreaGeofence::class , EntityColor::class , EntityContact::class ,
        EntityEvent::class, EntityPriority::class, EntityReminder::class,
        EntityTypeArea::class, EntityFirstTimeState::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AbuMonitorDatabase : RoomDatabase() {

    abstract fun entityAreaGeofenceDao(): DaoAreaGeofence
    abstract fun entityColorDao(): DaoColor
    abstract fun entityContactDao(): DaoContact
    abstract fun entityEventDao(): DaoEvent
    abstract fun entityPriorityDao(): DaoPriority
    abstract fun entityReminderDao(): DaoReminder
    abstract fun entityTypeAreaDao(): DaoTypeArea
    abstract fun firstTimeStateDao(): DaoFirstTimeState
    abstract fun joinAreaGeofence(): DaoJoinAreaGeofence

    companion object {
        @Volatile
        private var INSTANCE: AbuMonitorDatabase? = null


        suspend fun getDatabase(context: Context, scope: CoroutineScope): AbuMonitorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AbuMonitorDatabase::class.java,
                    Definition.DATABASE_NAME
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance

                scope.launch {
                    saveFirstState(instance)
                }

                instance // Devuelve la instancia creada
            }.also { database ->
                if (!getFirstState(database)) {
                    database.openHelper.writableDatabase // Asegura que la BD se inicialice antes de continuar
                }
            }
        }

        private suspend fun getFirstState(instance: AbuMonitorDatabase?): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    instance?.firstTimeStateDao()?.getFirstTimeState()?.isFirstTime ?: false
                } catch (e: Exception) {
                    Log.e(TAG, "Error al leer el estado de la base de datos")
                    false
                }
            }
        }

        private suspend fun saveFirstState(instance: AbuMonitorDatabase) {
            try {
                val state = EntityFirstTimeState(isFirstTime = true)
                instance.firstTimeStateDao().insert(state)
            } catch (e: Exception) {
                Log.e(TAG,"Error al guardar el estado de la base de datos")
            }
        }

     
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }


    private class DatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {

        internal val completion = CompletableDeferred<Unit>()

        companion object{

            private const val ID_INITIAL = 1

            private const val COLOR_BLUE = "Azul"
            private const val COLOR_GREEN = "Verde"
            private const val COLOR_RED = "Rojo"

            private const val EVENT_ENTER = "Entrar"
            private const val EVENT_EXIT = "Salir"
            private const val EVENT_STAY = "Permanecer"

            private const val PRIORITY_LOW = "Baja"
            private const val PRIORITY_MEDIUM = "Media"
            private const val PRIORITY_HIGH = "Alta"

            private const val TYPE_AREA_GOEFENCE = "Geofence"
            private const val TYPE_AREA_START_ROUTE = "Inicio_Ruta"
            private const val TYPE_AREA_END_ROUTE = "Fin_Ruta"




        }
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            Log.d(TAG,"SE EJECUTA EN ON CREATE DE DATABASE")
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO){
                    insertDataInDataBase(database)

                    completion.complete(Unit)
                }
            }
        }

        suspend fun awaitCompletion() {
            completion.await()

        }


        /**
         * Inserta los datos iniciales solo cuando se crea la base de datos.
         */
        private suspend fun insertDataInDataBase(database: AbuMonitorDatabase) {


            insertColorInDataBase(database)
            insertEventInDatabase(database)
            insertPriorityInDataBase(database)
            insetTypeAreaInDataBase(database)
            insertContactInDataBase(database)
            Log.d(TAG,"Inserto registros")



        }

        private suspend fun insertContactInDataBase(database: AbuMonitorDatabase) {
            val entityContact1 = EntityContact(ID_INITIAL , "Esteban" , "1134926279")
            val entityContact2 = EntityContact(ID_INITIAL + 1 , "Ramon" , "1134926279")
            val daoContact = database.entityContactDao()

            daoContact.insertContact(entityContact1)
            daoContact.insertContact(entityContact2)

        }

        private suspend fun insetTypeAreaInDataBase(database: AbuMonitorDatabase) {
            val entityTypeArea1 =
                EntityTypeArea(ID_INITIAL , description = TYPE_AREA_GOEFENCE)
            val entityTypeArea2 =
                EntityTypeArea(ID_INITIAL + 1 , description = TYPE_AREA_START_ROUTE)
            val entityTypeArea3 =
                EntityTypeArea(ID_INITIAL + 2 , description = TYPE_AREA_END_ROUTE)
            val daoTypeArea = database.entityTypeAreaDao()

            daoTypeArea.insertTypeArea(entityTypeArea1)
            daoTypeArea.insertTypeArea(entityTypeArea2)
            daoTypeArea.insertTypeArea(entityTypeArea3)
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


            daoColor.insertColor(entityColor1)
            daoColor.insertColor(entityColor2)
            daoColor.insertColor(entityColor3)
        }



    }
}
