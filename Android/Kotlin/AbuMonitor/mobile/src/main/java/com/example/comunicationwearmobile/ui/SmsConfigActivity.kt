package com.example.comunicationwearmobile.ui

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.common.InterfaceMainAct
import com.example.comunicationwearmobile.common.PermissionManager
import com.example.comunicationwearmobile.common.Utils
import com.example.comunicationwearmobile.models.ContactDataStore
import com.example.comunicationwearmobile.presenter.MsgPresenter
import com.example.shared_library.SharedData
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

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