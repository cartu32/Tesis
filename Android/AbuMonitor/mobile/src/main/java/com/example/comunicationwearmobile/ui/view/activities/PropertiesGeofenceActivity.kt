package com.example.comunicationwearmobile.ui.view.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.icu.util.TimeZone
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
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
import androidx.lifecycle.ViewModelProvider
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.ui.viewmodel.ViewModelFactory
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.viewmodel.ViewModelManager
import com.example.comunicationwearmobile.ui.viewmodel.ViewmodelMapsActivity
import com.google.android.gms.location.Priority
import java.security.Security
import java.sql.Time

class PropertiesGeofenceActivity: AppCompatActivity() {
    private lateinit var spEvents:Spinner
    private lateinit var spPriority:Spinner
    private lateinit var txtDescription:EditText
    private lateinit var txtDwellTime: EditText
    private lateinit var chkSecurityZone: CheckBox
    private lateinit var cmdSavGeofence:Button
    private lateinit var cmdCancelGeofence:Button
 //   private lateinit var viewmodelMapsActivity: ViewmodelMapsActivity
    private lateinit var factory: ViewModelFactory
    private lateinit var activity: AppCompatActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_properties_geofence)

        configureInsets()
        initializeComponentsView()

    }


    private fun initializeComponentsView(){
        var items:Array<String>

        spEvents=findViewById<Spinner>(R.id.spEvents)
        spPriority=findViewById<Spinner>(R.id.spPriority)
        txtDwellTime=findViewById<EditText>(R.id.txtDwellTime)
        chkSecurityZone=findViewById<CheckBox>(R.id.chkSecurityZone)
        txtDescription=findViewById<EditText>(R.id.txtDescription)

        cmdSavGeofence = findViewById<Button>(R.id.cmdSaveGeofences)
        cmdCancelGeofence = findViewById<Button>(R.id.cmdCancelGeofence)


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

        ViewModelManager.sharedViewmodelMapsActivity.cancelInMap()
        finish()
    }

    private fun actionSave() {
        //si se presiono el boton guardar
        Log.d(Definition.TAG_DEBUG,"confirmado datos Geofence")

        //cargo en el
        ViewModelManager.sharedViewmodelMapsActivity.saveAreaGeofence(
            spEvents.selectedItemPosition ,
            spPriority.selectedItemPosition,
            chkSecurityZone.isActivated,
            txtDwellTime.text.toString().toIntOrNull() ?:0,
            txtDescription.text.toString()
        )

        ViewModelManager.sharedViewmodelMapsActivity.confirmInMap()

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
