package com.example.comunicationwearmobile.ui.view.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.abumonitor.data.model.EntityContact
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.view.adapter.ContactTelAdapter
import com.example.comunicationwearmobile.ui.view.adapter.SavedContactAdapter
import com.example.comunicationwearmobile.ui.viewmodel.ViewmodelContacts

class ContactsActivity : AppCompatActivity() {

    private var txtFindContact: EditText? = null
    private var recyclerPhone:RecyclerView?=null
    private var recyclerSaved:RecyclerView?=null

    private lateinit var viewModel: ViewmodelContacts
    private lateinit var phoneContactsAdapter: ContactTelAdapter
    private lateinit var savedContactsAdapter: SavedContactAdapter

    private val phoneContacts = mutableListOf<Pair<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        viewModel = ViewModelProvider(this)[ViewmodelContacts::class.java]

        txtFindContact= findViewById<EditText>(R.id.txtFindContact)
        recyclerPhone = findViewById<RecyclerView>(R.id.recyclerPhoneContacts)
        recyclerSaved = findViewById<RecyclerView>(R.id.recyclerSavedContacts)

        recyclerPhone?.layoutManager = LinearLayoutManager(this)
        recyclerSaved?.layoutManager = LinearLayoutManager(this)

        configAdapters()
        configListeners()
        configObservers()

        viewModel.loadPhoneContacts(contentResolver)
    }

    private fun configListeners() {
        txtFindContact?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterPhoneContacts(s.toString())
            }
        })
    }

    private fun configAdapters(){
        phoneContactsAdapter = ContactTelAdapter(phoneContacts) { name, phone ->
            viewModel.saveContact(name, phone)
            Toast.makeText(this, "$name guardado", Toast.LENGTH_SHORT).show()
        }

        savedContactsAdapter = SavedContactAdapter(emptyList()) { contact ->
            showAlertDialog(contact)
        }

        recyclerPhone?.adapter = phoneContactsAdapter
        recyclerSaved?.adapter = savedContactsAdapter


    }
    private fun configObservers() {
        viewModel.savedContacts.observe(this) { savedList ->
            savedContactsAdapter.updateList(savedList)
        }

        viewModel.phoneContacts.observe(this) { list ->
            phoneContacts.clear()
            phoneContacts.addAll(list)
            phoneContactsAdapter.notifyDataSetChanged()
        }
    }

    private fun showAlertDialog(contact: EntityContact) {
        AlertDialog.Builder(this).apply {
            setTitle("Eliminar contacto")
            setMessage("¿Seguro que querés eliminar a ${contact.name}?")
            setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteContact(contact)
                Toast.makeText(this@ContactsActivity, "Contacto eliminado", Toast.LENGTH_SHORT).show()
            }
            setNegativeButton("Cancelar", null)
            show()
        }
    }

    private fun filterPhoneContacts(query: String) {
        val filteredList = viewModel.phoneContacts.value?.filter {
            it.first.contains(query, ignoreCase = true)
        } ?: emptyList()

        phoneContacts.clear()
        phoneContacts.addAll(filteredList)
        phoneContactsAdapter.notifyDataSetChanged()
    }
}
