package com.example.simpledatabase.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ContactDao {
    @Insert
    fun insert(contact: Contact)

    @Update
    fun update(vararg contact: Contact)

    @Delete
    fun delete(vararg contact: Contact)

    @Query("SELECT * FROM " + Contact.TABLE_NAME + " ORDER BY last_name, first_name")
    fun getOrderedAgenda(): LiveData<List<Contact>>
}