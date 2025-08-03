package com.example.comunicationwearmobile.ui.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.provider.ContactsContract
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.abumonitor.data.model.EntityContact
import com.example.comunicationwearmobile.ui.model.repository.RepositoryContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
class ViewmodelContacts(application: Application) : AndroidViewModel(application) {
    private val repositoryContact = RepositoryContact(application.applicationContext)

    val savedContacts: LiveData<List<EntityContact>> = repositoryContact.getAllContactsLiveData()

    // LiveData para los contactos del teléfono
    private val _phoneContacts = MutableLiveData<List<Pair<String, String>>>()
    val phoneContacts: LiveData<List<Pair<String, String>>> = _phoneContacts

    fun saveContact(name: String, phone: String) {
        viewModelScope.launch {
            repositoryContact.insertContact(EntityContact(name = name, telephone = phone))
        }
    }

    fun deleteContact(contact: EntityContact) {
        viewModelScope.launch {
            repositoryContact.deleteContact(contact)
        }
    }

    fun loadPhoneContacts(contentResolver: ContentResolver) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = mutableListOf<Pair<String, String>>()
            val cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null
            )
            cursor?.use {
                while (it.moveToNext()) {
                    val name = it.getString(
                        it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    )
                    val phone = it.getString(
                        it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    )
                    list.add(Pair(name, phone))
                }
            }
            _phoneContacts.postValue(list.sortedBy { it.first.lowercase() })        }
    }
}
