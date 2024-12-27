package com.example.comunicationwearmobile.ui.Activities

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
    private lateinit var cmdDefineAreas: Button
    private lateinit var cmdDefineReminders: Button
    private lateinit var cmdDefineRoutes: Button
    private lateinit var cmdDefineContacts:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        cmdDefineAreas.setOnClickListener(botonesListeners)
        cmdDefineReminders.setOnClickListener(botonesListeners)
        cmdDefineContacts.setOnClickListener(botonesListeners)
        cmdDefineRoutes.setOnClickListener(botonesListeners)
    }

    private val botonesListeners = View.OnClickListener { v ->
        when (v?.id) {
            R.id.cmdDefineAreas -> {
                // Acción para el botón cmdSendAlert
                val intent = Intent(this , MapsActivity::class.java)
                startActivity(
                    intent
                )
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