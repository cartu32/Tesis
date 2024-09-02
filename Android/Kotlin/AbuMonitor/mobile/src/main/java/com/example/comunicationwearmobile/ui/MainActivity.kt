package com.example.comunicationwearmobile.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.comunicationwearmobile.R

class MainActivity : AppCompatActivity() {
    private lateinit var cmdSendAlert: Button
    private lateinit var cmdShowGeofence: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left , systemBars.top , systemBars.right , systemBars.bottom)
            insets
        }
        cmdSendAlert = findViewById(R.id.cmdSendAlerts)
        cmdShowGeofence = findViewById(R.id.cmdShowGeofences)

        cmdSendAlert.setOnClickListener(botonesListeners)
        cmdShowGeofence.setOnClickListener(botonesListeners)
    }

    private val botonesListeners = View.OnClickListener { v ->
        when (v?.id) {
            R.id.cmdSendAlerts -> {
                // Acción para el botón cmdSendAlert
                val intent =Intent(this, MsgConfigActivity::class.java)
                startActivity(intent
                )
            }
            R.id.cmdShowGeofences -> {
                // Acción para el botón cmdShowGeofence
            }
        }
    }
}