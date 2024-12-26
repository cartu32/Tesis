package com.example.abumonitor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Contact")
data class EntityContact(
    @PrimaryKey(autoGenerate = true) var id_contact: Int =0 ,
    var name: String,
    var telephone:String
)
