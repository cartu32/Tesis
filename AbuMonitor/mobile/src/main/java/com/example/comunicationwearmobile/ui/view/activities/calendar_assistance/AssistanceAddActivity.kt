package com.example.comunicationwearmobile.ui.view.activities.calendar_assistance

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityScheduledAssistance
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.utils.Tools
import com.example.comunicationwearmobile.ui.viewmodel.AssistanceViewModelFactory
import com.example.comunicationwearmobile.ui.viewmodel.ViewModelCalendarAssistance
import java.util.Calendar
import java.util.Locale

class AssistanceAddActivity : AppCompatActivity() {

    private val viewModel: ViewModelCalendarAssistance by viewModels { AssistanceViewModelFactory(application) }
    private var dateAppontimentMillis: Long = 0
    private var dateHourAppointment:Long=0

    private var txtTitle:EditText?=null
    private var txtDescription:EditText?=null
    private var txtDate:EditText?=null
    private var txtDesactivationDate:EditText?=null
    private var cmdHourAppointment:Button?=null
    private var cmdCreateAreaGeof:Button?=null

    private var isSetTimeAppointment=false
    private var resultLauncher: ActivityResultLauncher<Intent>?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assistance_add)

        dateAppontimentMillis = intent.getLongExtra("date", 0)

        txtTitle = findViewById<EditText>(R.id.txtTitle)
        txtDescription = findViewById<EditText>(R.id.txtDescription)
        txtDate = findViewById<EditText>(R.id.txtDate)
        txtDesactivationDate = findViewById<EditText>(R.id.txtDesactivationDate)
        cmdHourAppointment = findViewById<Button>(R.id.cmdHourDate)
        cmdCreateAreaGeof = findViewById<Button>(R.id.cmdCreateAreaGeof)

        val dateString = Tools.getMillisToDate(dateAppontimentMillis)

        txtDate?.setText(dateString)
        txtDate?.isEnabled = false

        cmdHourAppointment?.setOnClickListener { onClickListenerCmdHourAppointment() }
        cmdCreateAreaGeof?.setOnClickListener { onClickListenerCmdSave() }

        initComponents()
        configResultLauncher()
        configObservers()
        configActionBar()
    }

    private fun initComponents() {
        val texto = String.format(Locale.getDefault(), "%d", Definition.DEFAULT_TIME_DESACTIVATION_APPOINTMENT)
        txtDesactivationDate?.setText(texto)
    }

    private fun configActionBar() {
        val actionBar = supportActionBar
        actionBar?.title = "AbuMonitor"
        actionBar?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        val textColor = SpannableString(actionBar?.title ?: "")
        textColor.setSpan(ForegroundColorSpan(Color.WHITE), 0, textColor.length, 0)
        actionBar?.title = textColor
    }

    private fun configObservers() {
        viewModel.idNewAssistance.observe(this) {idNewAssitance ->
            if(idNewAssitance!=-1L){
                Toast.makeText(this,"Se guardo correctamente la cita",Toast.LENGTH_SHORT).show()
                finish()
            }else{
                Toast.makeText(this,"ERROR No se pudo guardar correctamente la cita",Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun configResultLauncher() {
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                val data: Intent? = result.data
                val meters = data?.getIntExtra(Definition.INTENT_DATA_METERS, 0)
                val latitude = data?.getStringExtra(Definition.INTENT_DATA_LATITUDE)
                val longitude = data?.getStringExtra(Definition.INTENT_DATA_LONGITUDE)

                val assistance = EntityScheduledAssistance(
                    title = txtTitle?.text.toString(),
                    description = txtDescription?.text.toString(),
                    date_hour_appointment = dateHourAppointment,
                    time_duration_activation_appointment = txtDesactivationDate?.text.toString().toLong(),
                )

                if (meters != null) {
                    if (latitude != null) {
                        if (longitude != null) {
                            viewModel.insert(this,assistance,latitude,longitude,meters)
                            return@registerForActivityResult
                        }
                    }
                }
                Toast.makeText(this,"ERROR No se pudo guardar correctamente la cita",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onClickListenerCmdHourAppointment() {
        showTimePicker { millis ->
            //fusiono la fecha de la cita con la hora seleccionada
            val onlyHourApponinment = Tools.extractHourOfDateInMillis(millis)
            dateHourAppointment=dateAppontimentMillis+onlyHourApponinment

            //muestro la hora seleccionada
            cmdHourAppointment?.text = Tools.getFormatHour(millis)
        }
    }

    private fun onClickListenerCmdSave(){

        if(!isSetTimeAppointment){
            Toast.makeText(this,"Debe seleccionar una hora para la cita",Toast.LENGTH_SHORT).show()
            return
        }
        if(!Tools.isTimeAndDateGreaterThanCurrentDate(dateHourAppointment)) {
            Toast.makeText(this, "La hora seleccionada debe ser mayor a la actual ", Toast.LENGTH_SHORT).show()
            return
        }
        if(txtTitle?.text.toString().isEmpty()){
            Toast.makeText(this,"Debe ingresar un titulo para la cita",Toast.LENGTH_SHORT).show()
            return
        }
        if (txtDescription?.text.toString().isEmpty()) {
            Toast.makeText(this, "Debe ingresar una descripcion para la cita", Toast.LENGTH_SHORT)
                .show()
            return
        }

        var intent = Intent(this, MapsAddAssistance::class.java)
        resultLauncher?.launch(intent)
    }

    private fun showTimePicker(onTimeSet: (Long) -> Unit) {
        val cal = Calendar.getInstance()

        isSetTimeAppointment=true

        TimePickerDialog(this, { _, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            onTimeSet(cal.timeInMillis)
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.idNewAssistance.removeObservers(this)
    }
} 