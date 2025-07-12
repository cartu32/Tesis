package com.example.comunicationwearmobile.ui.view.activities.calendar_assistance

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.model.repository.RepositoryScheduleAssistance
import com.example.comunicationwearmobile.ui.utils.Tools
import kotlinx.coroutines.launch
import java.util.*

class AssistanceDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assistance_detail)

        val assistanceId = intent.getIntExtra("assistance_id", -1)
        val repoAssistance= RepositoryScheduleAssistance(this,lifecycleScope)
        val title = findViewById<TextView>(R.id.detailTitle)
        val desc = findViewById<TextView>(R.id.detailDesc)
        val time = findViewById<TextView>(R.id.detailTime)

        lifecycleScope.launch {
            val assistance = repoAssistance.getAssistanceWithId(assistanceId)
            assistance.let {
                val date = Tools.getMillisToDate(it.date_appointment)
                val hour = Tools.formatHour(it.hour_appointment)
                title.text = it.title
                desc.text = it.description
                time.text = "${date} - ${hour}"
            }
        }
    }

}