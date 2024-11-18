package com.example.comunicationwearmobile.ui.Activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.common.Utils
import com.example.comunicationwearmobile.models.contacts.ContactDataStore
import kotlinx.coroutines.launch

class SmsConfigActivity : AppCompatActivity() {

    private var cmdSaveNumber: Button? = null
    private var txtTelNumber: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //aca va el codigo de la activity
        setContentView(R.layout.activity_sms_config)

        cmdSaveNumber = findViewById<Button>(R.id.cmdSaveNumber)
        txtTelNumber = findViewById<TextView>(R.id.txtTelNumber)

        cmdSaveNumber?.setOnClickListener(listenerButton)

    }

    private val listenerButton = View.OnClickListener {
        lifecycleScope.launch {
            ContactDataStore.saveTelephoneNumber(applicationContext , txtTelNumber?.text.toString())
        }
        Utils.showToast(this,"Se registro el numero de telefono")
    }

}