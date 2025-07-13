package com.example.comunicationwearmobile.ui.view.activities.calendar_assistance

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.abumonitor.data.model.EntityScheduledAssistance
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.utils.Tools
import com.example.comunicationwearmobile.ui.viewmodel.AssistanceViewModelFactory
import com.example.comunicationwearmobile.ui.viewmodel.ViewModelCalendarAssistance
import java.util.Calendar

class AssistanceAddActivity : AppCompatActivity() {

    private val viewModel: ViewModelCalendarAssistance by viewModels { AssistanceViewModelFactory(application) }
    private var dateMillis: Long = 0
    private var hour:Long=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assistance_add)

        dateMillis = intent.getLongExtra("date", 0)

        val txtTitle = findViewById<EditText>(R.id.txtTitle)
        val txtDescription = findViewById<EditText>(R.id.txtDescription)
        val txtDate = findViewById<EditText>(R.id.txtDate)
        val cmdHourAppointment = findViewById<Button>(R.id.cmdHourDate)
        val cmdSave = findViewById<Button>(R.id.cmdCreateAreaGeof)

        val dateString = Tools.getMillisToDate(dateMillis)

        txtDate.setText(dateString)

        cmdHourAppointment.setOnClickListener {
            showTimePicker { millis ->
                hour = millis
                cmdHourAppointment.text = Tools.formatHour(millis)
            }
        }

        cmdSave.setOnClickListener {
            val assistance = EntityScheduledAssistance(
                title = txtTitle.text.toString(),
                description = txtDescription.text.toString(),
                date_appointment = dateMillis,
                hour_appointment = hour,
            )
            viewModel.insert(assistance)
            finish()
        }
    }

    private fun showTimePicker(onTimeSet: (Long) -> Unit) {
        val cal = Calendar.getInstance()
        TimePickerDialog(this, { _, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            onTimeSet(cal.timeInMillis)
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }

} 