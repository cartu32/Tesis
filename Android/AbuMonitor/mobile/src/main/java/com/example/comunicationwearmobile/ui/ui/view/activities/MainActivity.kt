package com.example.comunicationwearmobile.ui.ui.view.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.ui.viewmodel.ViewModelFactory
import com.example.abumonitor.ui.viewmodel.ViewmodelAreaGeofence
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.ui.viewmodel.ViewmodelMainActivity
import java.sql.Time

class MainActivity : AppCompatActivity() {
    private lateinit var cmdDefineAreas: Button
    private lateinit var cmdDefineReminders: Button
    private lateinit var cmdDefineRoutes: Button
    private lateinit var cmdDefineContacts: Button


    private lateinit var viewmodelAreaGeofence: ViewmodelAreaGeofence
    private lateinit var viewmodelMainActivity: ViewmodelMainActivity

    private lateinit var factory: ViewModelFactory

    private val TAG = "ViewmodelMain"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configView()
        configObserverLivedata()

     }

    private fun configObserverLivedata() {
        //asoscio el viewmodelAreaGeofence usando el factory a la view del MainActivity
        factory=Definition.factory
        //viewmodelAreaGeofence=ViewModelProvider(this,factory)[ViewmodelAreaGeofence::class.java]
        viewmodelMainActivity=ViewModelProvider(this,factory)[ViewmodelMainActivity::class.java]


    }



    private fun configView(){
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left , systemBars.top , systemBars.right , systemBars.bottom)
            insets
        }
        cmdDefineAreas     = findViewById(R.id.cmdDefineAreas)
        cmdDefineRoutes    = findViewById(R.id.cmdDefineRoutes)
        cmdDefineReminders = findViewById(R.id.cmdDefineReminder)
        cmdDefineContacts  = findViewById(R.id.cmdDefineContacts)

        cmdDefineAreas.setOnClickListener(listenerButton)
        cmdDefineReminders.setOnClickListener(listenerButton)
        cmdDefineContacts.setOnClickListener(listenerButton)
        cmdDefineRoutes.setOnClickListener(listenerButton)

    }

    // Crear un listener compartido
    val listenerButton = View.OnClickListener { view ->
        //val idAreaDelete = 1
        when (view.id) {
            R.id.cmdDefineAreas -> {
                // Acción para el botón cmdSendAlert
                val intent = Intent(this , MapsActivity::class.java)
                startActivity(intent)
            }

            R.id.cmdDefineRoutes -> {
                // Acción para el botón cmdSendAlert
                /* val intent = Intent(this , MapsActivity::class.java)
                 startActivity(
                     intent
                 )*/
            }


            R.id.cmdDefineReminder -> {
                // Acción para el botón cmdSmSConfig
                /*      val intent = Intent(this , SmsConfigActivity::class.java)
                      startActivity(
                          intent
                      )*/
            }

        }

    }
}