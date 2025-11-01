package com.example.comunicationwearmobile.ui.view.activities.calendar_assistance

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.common.SharedVariables
import com.example.comunicationwearmobile.ui.utils.Tools
import com.example.comunicationwearmobile.ui.viewmodel.AssistanceDetailViewModel

class AssistanceDetailActivity : AppCompatActivity() {
    private var txtTitle:TextView?=null
    private var txtDesc:TextView?=null
    private var txtInitDateAppointment:TextView?=null
    private var txtFinishDateAppointment:TextView?=null
    private var cmdDelete:Button?=null
    private var cmdViewLocation:Button?=null

    private lateinit var viewModel: AssistanceDetailViewModel
    private var id_area_geof: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assistance_detail)

        val assistanceId = intent.getIntExtra("assistance_id", -1)

        txtTitle = findViewById<TextView>(R.id.txtDetailTitle)
        txtDesc = findViewById<TextView>(R.id.txtDetailDesc)
        txtInitDateAppointment = findViewById<TextView>(R.id.txtInitDateAppointment)
        txtFinishDateAppointment = findViewById<TextView>(R.id.txtFinishDateAppointment)
        cmdDelete = findViewById<Button>(R.id.cmdDeleteDate)
        cmdViewLocation = findViewById<Button>(R.id.cmdViewLocation)

        configActionBar()
        initViewModel()
        configObserver()
        configComponents()

        cmdDelete?.setOnClickListener {cmdDeleteListener() }
        cmdViewLocation?.setOnClickListener { cmdViewLocationListener()}

        viewModel.loadAssistanceDetail(assistanceId)
    }

    private fun configComponents() {
        if(SharedVariables.user== SharedVariables.USER_ADMIN){
            cmdDelete?.isVisible=true
        }else{
            cmdDelete?.isVisible=false
        }
    }

    private fun configActionBar() {
        val actionBar = supportActionBar
        actionBar?.title = "AbuMonitor"
        actionBar?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        val textColor = SpannableString(actionBar?.title ?: "")
        textColor.setSpan(ForegroundColorSpan(Color.WHITE), 0, textColor.length, 0)
        actionBar?.title = textColor
    }

    private fun cmdDeleteListener() {
        showAlertDialog(id_area_geof)
    }

    private fun cmdViewLocationListener() {
        viewModel.getAreaGeofenceById(id_area_geof)
        Toast.makeText(this, "cargando ubicacion en mapa", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("SetTextI18n")
    private fun configObserver() {
        configObserverAssistanceDetail()
        configObserverAreaGeofence()
    }

    private fun configObserverAreaGeofence() {
        viewModel.areaGeofenceData.observe(this) { areaGeofence ->
            areaGeofence?.let {
                val latitude=it.latitude
                val longitude=it.longitude
                val meters=it.meters

                val intent = Intent(this, MapsViewAssistance::class.java).apply {
                    putExtra("latitude", latitude)
                    putExtra("longitude", longitude)
                    putExtra("meters", meters)
                }
                startActivity(intent)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun configObserverAssistanceDetail() {
        viewModel.assistanceDetail.observe(this) { assistance ->
            assistance?.let {
                val finishActivationAppointment=it.date_hour_appointment+it.time_duration_activation_appointment

                txtTitle?.text = it.title
                txtDesc?.text = it.description

                txtInitDateAppointment?.text = "${Tools.getMillisToDate(it.date_hour_appointment)} - ${Tools.getMillisToHourMinutes(it.date_hour_appointment)}"
                txtFinishDateAppointment?.text = "${Tools.getMillisToDate(finishActivationAppointment)} - ${Tools.getMillisToHourMinutes(finishActivationAppointment)}"
                id_area_geof = it.id_area
            }
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[AssistanceDetailViewModel::class.java]
    }

    private fun showAlertDialog(id_area_geof: Long) {
        AlertDialog.Builder(this).apply {
            setTitle("Eliminar cita")
            setMessage("¿Seguro que desea eliminar la cita")
            setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteAreaById(context,id_area_geof){result->
                    showDeleteResult(result)
                }
                Toast.makeText(this@AssistanceDetailActivity, "Cita eliminada", Toast.LENGTH_SHORT).show()
            }
            setNegativeButton("Cancelar", null)
            show()
        }
    }

    private fun showDeleteResult(result: Boolean) {
        if(result)
            finish()
        else
            Toast.makeText(this@AssistanceDetailActivity, "Error al eliminar la cita", Toast.LENGTH_SHORT).show()
    }

}