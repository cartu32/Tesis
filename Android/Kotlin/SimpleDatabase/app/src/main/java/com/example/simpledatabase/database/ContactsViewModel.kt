package com.example.simpledatabase.database

import android.app.Application
import android.arch.lifecycle.AndroidViewModel

class ContactsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ContactsRepository(application)
    val contacts = repository.getContacts()

    fun saveContact(contact: Contact) {
        repository.insert(contact)
    }
}