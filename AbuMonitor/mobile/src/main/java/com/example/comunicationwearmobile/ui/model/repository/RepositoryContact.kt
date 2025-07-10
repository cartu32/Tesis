package com.example.comunicationwearmobile.ui.model.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.datasource.local.AbuMonitorDatabase
import com.example.abumonitor.data.model.EntityContact
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RepositoryContact(context: Context, scope: CoroutineScope) {
    private val database = AbuMonitorDatabase.getDatabase(context, scope)
    private val daoContact = database.entityContactDao()

    suspend fun insertContact(contact: EntityContact): Long {
        return withContext(Dispatchers.IO) {
            try {
                daoContact.insertContact(contact)
            } catch (e: Exception) {
                e.printStackTrace()
                Definition.ERROR_INSERT_CONTACT
            }
        }
    }

    fun getAllContacts(): LiveData<List<EntityContact>> {
        return daoContact.getAllContact()
    }

    suspend fun deleteContact(contact: EntityContact): Int {
        return withContext(Dispatchers.IO) {
            daoContact.deleteContact(contact)
        }
    }
}
