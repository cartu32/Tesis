package com.example.simpledatabase

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import com.example.simpledatabase.database.Contact
import com.example.simpledatabase.database.ContactsViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var contactsViewModel: ContactsViewModel

    private lateinit var addContact_button: Button
    private lateinit var contacts_textView: TextView
    private lateinit var fistName_editText: EditText
    private lateinit var lastName_editText: EditText
    private lateinit var phone_editText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addContact_button = findViewById(R.id.addContact_button)
        contacts_textView = findViewById(R.id.contacts_textView)
        fistName_editText = findViewById(R.id.fistName_editText)
        lastName_editText = findViewById(R.id.lastName_editText)
        phone_editText = findViewById(R.id.phone_editText)

        // Usamos ViewModelProvider con la factory adecuada para AndroidViewModel
        contactsViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(ContactsViewModel::class.java)

        addContact_button.setOnClickListener { addContact() }
        addObserver()
    }

    private fun addObserver() {
        val observer = Observer<List<Contact>> { contacts ->
            if (contacts != null) {
                var text = ""
                for (contact in contacts) {
                    text += "${contact.lastName} ${contact.firstName} - ${contact.phoneNumber}\n"
                }
                contacts_textView.text = text
            }
        }
        contactsViewModel.contacts.observe(this, observer)
    }

    private fun addContact() {
        val phone = phone_editText.text.toString()
        val name = fistName_editText.text.toString()
        val lastName =
            if (lastName_editText.text.toString().isNotEmpty()) lastName_editText.text.toString()
            else null

        if (name.isNotEmpty() && phone.isNotEmpty()) {
            contactsViewModel.saveContact(Contact(phone, name, lastName))
        }
    }
}
