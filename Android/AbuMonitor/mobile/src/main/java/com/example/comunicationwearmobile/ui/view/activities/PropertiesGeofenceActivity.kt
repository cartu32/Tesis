package com.example.comunicationwearmobile.ui.view.activities

import android.graphics.Color
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Scroller
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.R
import com.google.android.gms.location.Priority

class PropertiesGeofenceActivity: AppCompatActivity() {
    private lateinit var spEvents:Spinner
    private lateinit var spPriority:Spinner
    private lateinit var txtDescription:EditText
    private lateinit var cmdSavGeofence:Button
    private lateinit var cmdCancelGeofence:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_properties_geofence)
        configureInsets()
        initializeComponentsView()

    }

    private fun initializeComponentsView(){
        var items:Array<String>

        cmdSavGeofence = findViewById<Button>(R.id.cmdSaveGeofences)
        cmdCancelGeofence = findViewById<Button>(R.id.cmdCancelGeofence)
        spEvents=findViewById<Spinner>(R.id.spEvents)
        spPriority=findViewById<Spinner>(R.id.spPriority)
        txtDescription=findViewById<EditText>(R.id.txtDescription)

        // Obtengo los valores del array de strings.xml
        items = resources.getStringArray(R.array.spinner_events)
        inititlizeSpinner(spEvents,items)

        items = resources.getStringArray(R.array.spinner_priority)
        inititlizeSpinner(spPriority,items)

        cmdSavGeofence.setOnClickListener{actionSave()}
        cmdCancelGeofence.setOnClickListener{actionCancel()}

        txtDescription.setScroller(Scroller(this))
        txtDescription.isVerticalScrollBarEnabled = true
        txtDescription.movementMethod = ScrollingMovementMethod()

    }

    private fun actionCancel() {
        Log.d(Definition.TAG_DEBUG,"Cancelando Geofence")
        finish()
    }

    private fun actionSave() {
        Log.d(Definition.TAG_DEBUG,"Guardando Geofence")
        finish()
    }

    private fun inititlizeSpinner(spinner: Spinner,items:Array<String>,) {

        // Crea el adaptador personalizado
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).setTextColor(Color.BLACK) // Cambia el color del texto seleccionado
                (view as TextView).textSize = 20F
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).setTextColor(Color.WHITE) // Cambia el color del texto del desplegable
                (view as TextView).textSize = 20F
                (view as TextView).setPadding(15)
                return view
            }
        }

        // Asigna el adaptador al Spinner
        spinner.adapter = adapter

        // Escuchar selección (opcional)
        spEvents.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>? , view: View? , position: Int , id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                spEvents.setSelection(position)

                Log.d(Definition.TAG_DEBUG,"Seleccionaste: $selectedItem")
            }


            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Acción cuando no se selecciona nada
            }
        }  }

    private fun configureInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.properties_geofence)) { v , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
