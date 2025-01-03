package com.example.abumonitor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date
import java.sql.Time

@Entity(tableName = "Reminder")
data class EntityReminder(
    @PrimaryKey(autoGenerate = true) var id_reminder: Int =0,
    var date:Date,
    var hour:Time
)
