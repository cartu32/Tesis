package com.example.simpledatabase.database

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ContactsRepository(application: Application) {
    private val contactDao: ContactDao? = ContactsDatabase.getInstance(application)?.contactDao()

    fun insert(contact: Contact) {
        if (contactDao != null) InsertAsyncTask(contactDao).execute(contact)
    }

    fun getContacts(): LiveData<List<Contact>> {
        return contactDao?.getOrderedAgenda() ?: MutableLiveData<List<Contact>>()
    }

    private class InsertAsyncTask(private val contactDao: ContactDao) :
        AsyncTask<Contact , Void , Void>() {
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg contacts: Contact?): Void? {
            for (contact in contacts) {
                if (contact != null) contactDao.insert(contact)
            }
            return null
        }
    }
}