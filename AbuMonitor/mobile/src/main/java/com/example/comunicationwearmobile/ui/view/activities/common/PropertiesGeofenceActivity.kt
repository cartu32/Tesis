package com.example.comunicationwearmobile.ui.view.activities.common

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Scroller
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.pojo.JoinAreaGeofence
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.model.dto.DataAreaGeofAux
import com.example.comunicationwearmobile.ui.model.extra.StateSpinner
import com.example.comunicationwearmobile.ui.utils.Tools
import com.example.comunicationwearmobile.ui.utils.interfaces.OnCheckboxClickListener
import com.example.comunicationwearmobile.ui.view.adapter.SpinnerMultipleAdapter
import com.example.comunicationwearmobile.ui.view.adapter.SpinnerSimpleAdapter
import com.example.comunicationwearmobile.ui.view.fragment.ConfigDefineAreasFragment
import java.text.SimpleDateFormat
import java.util.Calendar

class PropertiesGeofenceActivity: AppCompatActivity(), OnCheckboxClickListener {
    private var spEvents:Spinner ?=null
    private var spPriority:Spinner ?=null
    private var txtDescription:EditText ?=null
    private var txtDwellTime: EditText ?=null
    private var chkSecurityZone: CheckBox ?=null
    private var cmdSavGeofence:Button ?=null
    private var cmdCancelGeofence:Button ?=null
    private var titleGroupSecurityZone:TextView?=null

    private var spEventsAdapter:SpinnerMultipleAdapter?=null
    private var spPriorityAdapter:SpinnerSimpleAdapter?=null
    private var listSpEvents:ArrayList<StateSpinner> = ArrayList()


    private var groupSecurityZone:TableLayout ?= null
    private var txtMinHourSecureZone:TextView ?= null
    private var txtMaxHourSecureZone:TextView ?= null
    private var lblMinHour:TextView?=null
    private var lblMaxHour:TextView?=null

    private var isDwellTime=false

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_geofence_properties)

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
        spEventsAdapter?.modifyVisibilityCheckBox(false)
        spPriority?.isEnabled = false
        chkSecurityZone?.isClickable = false
        cmdSavGeofence?.setVisibility(View.INVISIBLE)

        if(chkSecurityZone?.isChecked == true){
            lblMinHour?.visibility=View.VISIBLE
            lblMaxHour?.visibility=View.VISIBLE
            txtMinHourSecureZone?.visibility=View.VISIBLE
            txtMaxHourSecureZone?.visibility=View.VISIBLE

            groupSecurityZone?.isEnabled=false
            txtMinHourSecureZone?.isEnabled=false
            txtMaxHourSecureZone?.isEnabled=false
        }
        else {
            lblMinHour?.visibility = View.INVISIBLE
            lblMaxHour?.visibility = View.INVISIBLE
            txtMinHourSecureZone?.visibility = View.INVISIBLE
            txtMaxHourSecureZone?.visibility = View.INVISIBLE
        }
    }

    private fun getJoinAreaGeofence(): JoinAreaGeofence? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Definition.INTENT_DATA_NEW_AREA_GEOF, JoinAreaGeofence::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Definition.INTENT_DATA_NEW_AREA_GEOF)
        }
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun loadPropertiesInScreen(param: Bundle) {
        val dato=getJoinAreaGeofence()

        with(dato?.areaGeofence) {
            txtDescription?.setText(this?.description)
            spPriority?.setSelection((this?.id_priority?.toInt() ?: 1) - 1)

            if (this?.id_type_area == Definition.TYPE_AREA_ID_SECURITY_ZONE) {
                chkSecurityZone?.isChecked=true
                txtMinHourSecureZone?.text = dato?.securityZoneTimeRange?.min_hour.toString()
                txtMaxHourSecureZone?.text = dato?.securityZoneTimeRange?.max_hour.toString()
            }else{
                chkSecurityZone?.isChecked=false
            }
        }
        val dwellTimeInMinute=Tools.convertMillisToMinutes(dato?.secDwellTimeZone?.dwell_time ?: 0)
        txtDwellTime?.setText("Tiempo de permanencia " + dwellTimeInMinute + " (min)")

        val listIdEventSelected=dato?.events?.map{it.id_event}
        listIdEventSelected?.let {
            spEventsAdapter?.setSelectedItemsByPositions(it)
        }


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
        titleGroupSecurityZone=findViewById(R.id.groupbox_title2)

        cmdSavGeofence = findViewById<Button>(R.id.cmdSaveGeofences)
        cmdCancelGeofence = findViewById<Button>(R.id.cmdCancelGeofence)

        groupSecurityZone = findViewById<TableLayout>(R.id.groupSecurityZone)
        txtMinHourSecureZone = findViewById<EditText>(R.id.txtMinHourSecureZone)
        txtMaxHourSecureZone = findViewById<EditText>(R.id.txtMaxHourSecureZone)
        lblMinHour = findViewById<TextView>(R.id.lblMinHour)
        lblMaxHour = findViewById<TextView>(R.id.lblMaxHour)

        initilizeSpinnerSpEvents()

        inititlizeSpinnerSpPriority()

        //les asocio los listener a cada elemento
        cmdSavGeofence?.setOnClickListener{actionSave()}
        cmdCancelGeofence?.setOnClickListener{actionCancel()}
        chkSecurityZone?.setOnClickListener(){changevisiblityGroup()}
        txtMinHourSecureZone?.setOnClickListener(){listenerMinHourSecurityZone()}
        txtMaxHourSecureZone?.setOnClickListener(){listenerMaxHourSecurityZone()}
        txtDescription?.setScroller(Scroller(this))
        txtDescription?.isVerticalScrollBarEnabled = true
      //  groupSecurityZone?.visibility =View.INVISIBLE
      //  titleGroupSecurityZone?.visibility=View.INVISIBLE
        changevisiblityGroup()
        txtDescription?.movementMethod = ScrollingMovementMethod()


    }

    private fun listenerMaxHourSecurityZone() {
        showTimePicker { selectedTime ->
            txtMaxHourSecureZone?.text = Tools.toEditable(selectedTime)
        }
    }

    private fun listenerMinHourSecurityZone() {
        showTimePicker { selectedTime ->
            txtMinHourSecureZone?.text = Tools.toEditable(selectedTime)
        }
    }

    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()

        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            val timeSelected = SimpleDateFormat("HH:mm").format(cal.time)
            onTimeSelected(timeSelected) // devolver el valor aquí
        }

        TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }


    private fun changevisiblityGroup() {
        if (chkSecurityZone?.isChecked == true) {
            lblMinHour?.visibility=View.VISIBLE
            lblMaxHour?.visibility=View.VISIBLE
            txtMinHourSecureZone?.visibility=View.VISIBLE
            txtMaxHourSecureZone?.visibility=View.VISIBLE
        } else {
            lblMinHour?.visibility=View.INVISIBLE
            lblMaxHour?.visibility=View.INVISIBLE
            txtMinHourSecureZone?.visibility=View.INVISIBLE
            txtMaxHourSecureZone?.visibility=View.INVISIBLE

        }
    }


    private fun initilizeSpinnerSpEvents() {
        var items:Array<String>

        // Obtengo los valores del array de strings.xml
        items = resources.getStringArray(R.array.spinner_events)

        for (item in items) {
            listSpEvents.add(StateSpinner(item,false))
        }

        spEventsAdapter = SpinnerMultipleAdapter(this, 0, listSpEvents)
        spEvents?.adapter = spEventsAdapter

    }

    private fun inititlizeSpinnerSpPriority() {
        var items:Array<String>

        // Obtengo los valores del array de strings.xml
        items = resources.getStringArray(R.array.spinner_priority)

        spPriorityAdapter = SpinnerSimpleAdapter(this, android.R.layout.simple_spinner_item, items)
        spPriority?.adapter = spPriorityAdapter

    }

    override fun onCheckboxClicked(position: Int, isChecked: Boolean) {
        val indexCheckBoxDweelTime=3

        if(position==indexCheckBoxDweelTime) {
            changeVisibiblityDwellTime(isChecked)
            //marco una bandera que indica que el area es dwelltime
            isDwellTime = isChecked
        }
    }

    fun enableVisiblityDwellTime() {
        txtDwellTime?.isEnabled = false

        txtDwellTime?.setTextColor(Color.BLACK)
        txtDwellTime?.setBackgroundResource(R.drawable.editext_enabled)

    }
    private fun changeVisibiblityDwellTime(isChecked: Boolean) {
        txtDwellTime?.isEnabled = isChecked

        if (isChecked) {
            txtDwellTime?.setTextColor(Color.BLACK)
            txtDwellTime?.setBackgroundResource(R.drawable.editext_enabled)
            return
        } else {
            txtDwellTime?.setTextColor(Color.WHITE)
            txtDwellTime?.setBackgroundResource(R.drawable.editext_disabled)
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
        var thereEventSelected = false
        var dataAreaGeofAux=DataAreaGeofAux()


        if(chkSecurityZone?.isChecked==true){
            //si es zona segura entonces seteo el time range
            if(txtMinHourSecureZone?.text.toString().isEmpty() || txtMaxHourSecureZone?.text.toString().isEmpty()){
                Toast.makeText(this,"Debe ingresar el horario normal de seguridad",Toast.LENGTH_SHORT).show()
                return
            }
            dataAreaGeofAux.secZoneTimeRange?.let {
                it.min_hour=txtMinHourSecureZone?.text.toString()
                it.max_hour=txtMaxHourSecureZone?.text.toString()
            }
        }else{
            //si no es area segura entonces secZoneTimeRange es null
            dataAreaGeofAux.secZoneTimeRange=null
        }

        val resultIntent= Intent(this,ConfigDefineAreasFragment::class.java)


        with(dataAreaGeofAux) {

            //me fijo que datos estan en la pantalla
            with(entityAreaGeofence) {
                //como la posicion seleccionada empieza en 0, entonces le sumo 1 para que coincida con el valor de la BD
                id_priority = spPriority?.selectedItemPosition?.plus(1) ?: 0
                description = txtDescription?.text.toString()

                val security_zone = chkSecurityZone?.isChecked == true

                if (!security_zone)
                    id_type_area = Definition.TYPE_AREA_ID_NORMAL
                else
                    id_type_area = Definition.TYPE_AREA_ID_SECURITY_ZONE
            }



            //me fijo que eventos estan seleccionados en el spinner
            for (event in listSpEvents) {
                if (event.selected) {
                    dataAreaGeofAux.listIdEventSelected.add(listSpEvents.indexOf(event))
                    thereEventSelected=true
                }
            }

            //pregunto si el area es dwelltime
            if (isDwellTime) {
                if (txtDwellTime?.text.toString() != 0.toString() &&
                    txtDwellTime.toString().isNotEmpty()
                ) {
                    val dwellTimeInMillis=Tools.convertMinutesToMillis(txtDwellTime?.text.toString().toLong())
                    dataAreaGeofAux.secZoneDwellTime.dwell_time = dwellTimeInMillis
                }else{
                    Toast.makeText(this@PropertiesGeofenceActivity,"Debe ingresar el tiempo de permanencia mayor a 0",Toast.LENGTH_SHORT).show()
                    return
                }
            }
        }

        Log.d(Definition.TAG_DEBUG, "listado de eventos$listSpEvents")

        if(!thereEventSelected){
            Toast.makeText(this,"Debe seleccionar al menos un evento",Toast.LENGTH_SHORT).show()
            return
        }
        //retorno al fragement ConfigGeofence que se presiono el boton ok y
        //ademas le envio el objeto EntityAreaGeofence con los datos ingresados
        resultIntent.putExtra(Definition.INTENT_DATA_NEW_AREA_GEOF,dataAreaGeofAux)
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

        cmdSavGeofence?.setOnClickListener(null)
        cmdCancelGeofence?.setOnClickListener(null)
        chkSecurityZone?.setOnClickListener(null)
        txtMinHourSecureZone?.setOnClickListener(null)
        txtMaxHourSecureZone?.setOnClickListener(null)

        spEvents=null
        spPriority=null

        txtDescription=null
        txtDwellTime=null
        chkSecurityZone=null
        cmdSavGeofence=null
        cmdSavGeofence=null
        cmdCancelGeofence=null
        txtMinHourSecureZone=null
        txtMaxHourSecureZone=null

        Log.d(Definition.TAG_DEBUG,"Ondestroy PropertiesGeofenceActvity")

    }




}