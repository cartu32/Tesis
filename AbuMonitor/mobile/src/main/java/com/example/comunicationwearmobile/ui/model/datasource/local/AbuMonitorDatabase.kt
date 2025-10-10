package com.example.abumonitor.data.datasource.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.data.model.EntityContact
import com.example.abumonitor.data.model.EntityEvent
import com.example.abumonitor.data.model.EntityFirstTimeState
import com.example.abumonitor.data.model.EntityPriority
import com.example.abumonitor.data.model.EntityScheduledAssistance
import com.example.abumonitor.utils.Converters
import com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity.DaoAreaGeofence
import com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity.DaoContact
import com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity.DaoEvent
import com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity.DaoFirstTimeState
import com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity.DaoPriority
import com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity.DaoScheduledAssistance
import com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity.DaoSecurityZoneTimeRange
import com.example.comunicationwearmobile.ui.model.datasource.local.daoEntity.DaoTypeArea
import com.example.comunicationwearmobile.ui.model.datasource.local.daoPojo.DaoJoinAreaGeofence
import com.example.comunicationwearmobile.ui.model.entities.EntityAreaEventCrossRef
import com.example.comunicationwearmobile.ui.model.entities.EntitySecurityZoneTimeRange
import com.example.comunicationwearmobile.ui.model.entities.EntityTypeArea

@Database(
    entities = [
        EntityAreaGeofence::class, EntityContact::class, EntityTypeArea::class,
        EntityEvent::class, EntityPriority::class, EntityScheduledAssistance::class,
        EntityAreaEventCrossRef::class, EntityFirstTimeState::class,
        EntitySecurityZoneTimeRange::class
    ],
    version = 9,
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

        fun getDatabase(context: Context): AbuMonitorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AbuMonitorDatabase::class.java,
                    Definition.DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }

        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
