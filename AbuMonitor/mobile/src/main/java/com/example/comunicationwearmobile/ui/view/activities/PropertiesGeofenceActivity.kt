package com.example.comunicationwearmobile.ui.view.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.data.model.JoinAreaGeofence
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.model.extra.StateSpinner
import com.example.comunicationwearmobile.ui.view.adapter.SpinnerAdapter
import com.example.comunicationwearmobile.ui.view.fragment.ConfigGeofenceFragment

class PropertiesGeofenceActivity: AppCompatActivity() {
    private var spEvents:Spinner ?=null
    private var spPriority:Spinner ?=null
    private var txtDescription:EditText ?=null
    private var txtDwellTime: EditText ?=null
    private var chkSecurityZone: CheckBox ?=null
    private var cmdSavGeofence:Button ?=null
    private var cmdCancelGeofence:Button ?=null

    private var spEventsAdapter:SpinnerAdapter?=null
    private var listSpEvents:ArrayList<StateSpinner> = ArrayList()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_properties_geofence)

        //inicializo los elementos de la view
        configureInsets()
        initializeComponentsView()

        val param=intent.extras

        //me fijo si vienen parametros en el intent.
        if(param!=null) {
            //se Posterga la carga de datos para después de que se inicialicen los spinners

            spEvents?.post {
                spPriority?.post {
                    //si vienen quiere decir que debo mostrar los datos del area recibida
                    //por paremetro en pantalla
                    loadPropertiesInScreen(param)

                    disabledComponents()
                }
            }

        }
    }

    private fun disabledComponents() {
        txtDescription?.isEnabled = false
        txtDwellTime?.isEnabled = false
        spEvents?.isEnabled = false
        spPriority?.isEnabled = false
        chkSecurityZone?.isClickable = false
        cmdSavGeofence?.setVisibility(View.INVISIBLE)

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun loadPropertiesInScreen(param: Bundle) {
        val area: JoinAreaGeofence? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Definition.INTENT_DATA_NEW_AREA_GEOF, JoinAreaGeofence::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Definition.INTENT_DATA_NEW_AREA_GEOF)
        }

        txtDescription?.setText(area?.description_area)
        txtDwellTime?.setText("Tiempo de permanencia " + area?.dwell_time.toString() + " (min)")
        chkSecurityZone?.isChecked=area?.security_zone==true
        spEvents?.setSelection((area?.id_event?.toInt() ?: 1) - 1)
        spPriority?.setSelection((area?.id_priority?.toInt() ?: 1) - 1)
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

        initilizeSpinnerSpEvents()


        //inititlizeSpinner(spEvents,items)

        items = resources.getStringArray(R.array.spinner_priority)
        inititlizeSpinner(spPriority,items)

        //les asocio los listener a cada elemento
        cmdSavGeofence?.setOnClickListener{actionSave()}
        cmdCancelGeofence?.setOnClickListener{actionCancel()}

        txtDescription?.setScroller(Scroller(this))
        txtDescription?.isVerticalScrollBarEnabled = true
        txtDescription?.movementMethod = ScrollingMovementMethod()

    }

    private fun initilizeSpinnerSpEvents() {
        var items:Array<String>

        // Obtengo los valores del array de strings.xml
        items = resources.getStringArray(R.array.spinner_events)

        for (item in items) {
            listSpEvents.add(StateSpinner(item,false))
        }

        spEventsAdapter = SpinnerAdapter(this, 0, listSpEvents)
        spEvents?.adapter = spEventsAdapter

    }


    private fun inititlizeSpinner(spinner: Spinner?,items:Array<String>,) {

        // Crea el adaptador personalizado
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items) {
            override fun getView(position: Int , convertView: View? , parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).setTextColor(Color.BLACK) // Cambia el color del texto seleccionado
                (view as TextView).textSize = 20F
                return view
            }

            override fun getDropDownView(position: Int , convertView: View? , parent: ViewGroup): View {
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

                //Log.d(Definition.TAG_DEBUG,"Seleccionaste: $selectedItem")
            }


            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Acción cuando no se selecciona nada
            }
        }
    }

    private fun actionCancel() {

        //retorno al fragement ConfigGeofence que se presiono el boton cancelar
        val resultIntent= Intent()
        setResult(Activity.RESULT_CANCELED,resultIntent)

        //se cierra la activity
        finish()
    }


    private fun actionSave() {
        //por el momento hardcodeo estos el color y el tipo de area
        val color_blue=1
        val type_area_geof=1

        var entityAreaAux=EntityAreaGeofence()
        val resultIntent= Intent(this,ConfigGeofenceFragment::class.java)

        with(entityAreaAux) {
            //como la posicion seleccionada empieza en 0, entonces le sumo 1 para que coincida con el valor de la BD
            id_event = spEvents?.selectedItemPosition?.plus(1) ?: 0
            id_priority = spPriority?.selectedItemPosition?.plus(1) ?: 0
            security_zone = chkSecurityZone?.isChecked == true
            dwell_time = txtDwellTime?.text.toString().toIntOrNull() ?: 0
            description = txtDescription?.text.toString()

            id_color=color_blue
            id_type_area=type_area_geof
        }

        //retorno al fragement ConfigGeofence que se presiono el boton ok y
        //ademas le envio el objeto EntityAreaGeofence con los datos ingresados
        resultIntent.putExtra(Definition.INTENT_DATA_NEW_AREA_GEOF,entityAreaAux)
        setResult(Activity.RESULT_OK,resultIntent)

        //metodo que se ejecuta al presionar el boton guardar
        Log.d(Definition.TAG_DEBUG,"confirmado datos Geofence")

        //se cierra la activity
        finish()
    }


    override fun onDestroy() {
        super.onDestroy()


        spPriority?.onItemSelectedListener=null
        spEvents?.onItemSelectedListener=null

        spEvents=null
        spPriority=null

        txtDescription=null
        txtDwellTime=null
        chkSecurityZone=null
        cmdSavGeofence=null
        cmdSavGeofence=null
        cmdCancelGeofence=null

        Log.d(Definition.TAG_DEBUG,"Ondestroy PropertiesGeofenceActvity")

    }


}