package com.example.comunicationwearmobile.ui.view.activities.calendar_assistance

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityContact
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.model.repository.RepositoryScheduleAssistance
import com.example.comunicationwearmobile.ui.utils.Tools
import com.example.comunicationwearmobile.ui.viewmodel.AssistanceDetailViewModel
import kotlinx.coroutines.launch
import java.util.*
class AssistanceDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: AssistanceDetailViewModel
    private var id_area_geof: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assistance_detail)

        val assistanceId = intent.getIntExtra("assistance_id", -1)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[AssistanceDetailViewModel::class.java]

        val title = findViewById<TextView>(R.id.detailTitle)
        val desc = findViewById<TextView>(R.id.detailDesc)
        val time = findViewById<TextView>(R.id.detailTime)
        val cmdDelete = findViewById<Button>(R.id.cmdDeleteDate)
        val cmdViewLocation = findViewById<Button>(R.id.cmdViewLocation)

        viewModel.assistanceDetail.observe(this) { assistance ->
            assistance?.let {
                title.text = it.title
                desc.text = it.description
                time.text = "${Tools.getMillisToDate(it.date_appointment)} - ${Tools.formatHour(it.hour_appointment)}"
                id_area_geof = it.id_area
            }
        }

        cmdDelete.setOnClickListener {showAlertDialog(id_area_geof) }

        cmdViewLocation.setOnClickListener {
            Toast.makeText(this, "próximamente", Toast.LENGTH_SHORT).show()
        }

        viewModel.loadAssistanceDetail(assistanceId)
    }

    private fun showAlertDialog(id_area_geof: Long) {
        AlertDialog.Builder(this).apply {
            setTitle("Eliminar cita")
            setMessage("¿Seguro que querés eliminar la cita")
            setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteAreaById(id_area_geof){
                    finish()
                }
                Toast.makeText(this@AssistanceDetailActivity, "Cita eliminada", Toast.LENGTH_SHORT).show()
            }
            setNegativeButton("Cancelar", null)
            show()
        }
    }

}