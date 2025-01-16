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
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Scroller
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.viewmodel.ViewModelManager

class PropertiesGeofenceActivity: AppCompatActivity() {
    private var spEvents:Spinner ?=null
    private var spPriority:Spinner ?=null
    private var txtDescription:EditText ?=null
    private var txtDwellTime: EditText ?=null
    private var chkSecurityZone: CheckBox ?=null
    private var cmdSavGeofence:Button ?=null
    private var cmdCancelGeofence:Button ?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_properties_geofence)

        //inicializo los elementos de la view
        configureInsets()
        initializeComponentsView()

    }

    private fun configureInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.properties_geofence)) { v , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initializeComponentsView(){
        var items:Array<String>

        //asocio con los objetos de con los elementos del layout
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

        //les asocio los listener a cada elemento
        cmdSavGeofence?.setOnClickListener{actionSave()}
        cmdCancelGeofence?.setOnClickListener{actionCancel()}

        txtDescription?.setScroller(Scroller(this))
        txtDescription?.isVerticalScrollBarEnabled = true
        txtDescription?.movementMethod = ScrollingMovementMethod()

    }

    private fun actionCancel() {
        //metodo que se ejecuta al presionar el boton cancelar
        Log.d(Definition.TAG_DEBUG,"Cancelando Geofence")

        ViewModelManager.sharedViewmodelMapsActivity.cancelInMap()
        //se cierra la activity
        finish()
    }

    private fun actionSave() {

        //metodo que se ejecuta al presionar el boton guardar
        Log.d(Definition.TAG_DEBUG,"confirmado datos Geofence")

        //cargo en el viewmodel los valores de los elementos ingresados en la view
        ViewModelManager.sharedViewmodelMapsActivity.saveAreaGeofence(
            spEvents?.selectedItemPosition ,
            spPriority?.selectedItemPosition,
            chkSecurityZone?.isActivated,
            txtDwellTime?.text.toString().toIntOrNull() ?:0,
            txtDescription?.text.toString()
        )

        //le notifico a mapsactivty que se confirmaron
        ViewModelManager.sharedViewmodelMapsActivity.confirmInMap()

        //se cierra la activity
        finish()
    }

    private fun inititlizeSpinner(spinner: Spinner?,items:Array<String>,) {

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
        spinner?.adapter = adapter

        // Escuchar selección (opcional)
        spEvents?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>? , view: View? , position: Int , id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                spEvents?.setSelection(position)

                Log.d(Definition.TAG_DEBUG,"Seleccionaste: $selectedItem")
            }


            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Acción cuando no se selecciona nada
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        spEvents=null
        spPriority=null
        txtDescription=null
        txtDwellTime=null
        chkSecurityZone=null
        cmdSavGeofence=null
        cmdSavGeofence=null
        cmdCancelGeofence=null

    }


}
