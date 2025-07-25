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
import androidx.lifecycle.ViewModelProvider
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.utils.Tools
import com.example.comunicationwearmobile.ui.viewmodel.AssistanceDetailViewModel

class AssistanceDetailActivity : AppCompatActivity() {
    private var title:TextView?=null
    private var desc:TextView?=null
    private var time:TextView?=null
    private var cmdDelete:Button?=null
    private var cmdViewLocation:Button?=null

    private lateinit var viewModel: AssistanceDetailViewModel
    private var id_area_geof: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assistance_detail)

        val assistanceId = intent.getIntExtra("assistance_id", -1)

        title = findViewById<TextView>(R.id.detailTitle)
        desc = findViewById<TextView>(R.id.detailDesc)
        time = findViewById<TextView>(R.id.detailTime)
        cmdDelete = findViewById<Button>(R.id.cmdDeleteDate)
        cmdViewLocation = findViewById<Button>(R.id.cmdViewLocation)

        configActionBar()
        initViewModel()
        configObserver()

        cmdDelete?.setOnClickListener {cmdDeleteListener() }
        cmdViewLocation?.setOnClickListener { cmdViewLocationListener()}

        viewModel.loadAssistanceDetail(assistanceId)
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
                title?.text = it.title
                desc?.text = it.description
                time?.text = "${Tools.getMillisToDate(it.date_appointment)} - ${Tools.formatHour(it.hour_appointment)}"
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