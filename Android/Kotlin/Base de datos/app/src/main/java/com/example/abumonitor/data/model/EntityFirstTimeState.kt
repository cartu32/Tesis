package com.example.abumonitor.data.model


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "first_time_state")
data class EntityFirstTimeState(
    @PrimaryKey val id: Int = 1 ,
    val isFirstTime: Boolean = false
)