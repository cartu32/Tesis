package com.example.comunicationwearmobile.ui.view.activities.menu_option

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.common.SharedVariables
import com.example.comunicationwearmobile.ui.utils.Helpers.AlarmHelper
import com.example.comunicationwearmobile.ui.utils.broadcast.AlarmDailyForChecksBroadcastReceiver
import java.util.Locale

class ConfigActivity: AppCompatActivity() {

    private var cmdSaveConfig: Button? = null
    private var cmdCancelConfig: Button? = null

    private var cmdTimeAlarmBetweenChecks: Button? = null

    private var isChangedAlarmBetweenChecks=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuration)

        cmdSaveConfig = findViewById<Button>(R.id.cmdSaveConfig)
        cmdCancelConfig = findViewById<Button>(R.id.cmdCancelConfig)
        cmdTimeAlarmBetweenChecks = findViewById<Button>(R.id.cmdTimeAlarmForCkecks)


        cmdTimeAlarmBetweenChecks?.text = String.format(Locale.getDefault(), "%02d:%02d", SharedVariables.hourAlarmBetweenCheck, SharedVariables.minuteAlramBetweenCheck)

        cmdCancelConfig?.setOnClickListener {listenerCmdCancelConfig()}
        cmdSaveConfig?.setOnClickListener {listenerCmdSaveConfig()}
        cmdTimeAlarmBetweenChecks?.setOnClickListener{listenerCmdTimeAlarmForChecks()}
        cmdSaveConfig?.isEnabled=false

        configActionBar()
    }

    private fun listenerCmdSaveConfig() {
        if(saveTimeAlarmBetweebChecks())
            finish()
        else
            Toast.makeText(this@ConfigActivity,"No se realizaron cambios en la alarma",Toast.LENGTH_SHORT).show()

    }

    private fun saveTimeAlarmBetweebChecks(): Boolean {
        with(SharedVariables) {
            val alarmHelper=AlarmHelper()

            val resultSetAlarm= alarmHelper.setAlarmAfterOfTime(
                    this@ConfigActivity,
                    Definition.ALARM_ID_BETWEEN_CHECKS,
                    hourAlarmBetweenCheck,
                    minuteAlramBetweenCheck,
                    Definition.ACTION_ALARM_FOR_CHECKS,
                    AlarmDailyForChecksBroadcastReceiver::class.java
                )

            if(resultSetAlarm) {
                Log.d(Definition.TAG_DEBUG, "Alarma de checkeo configurada correctamente")
                Toast.makeText(this@ConfigActivity, "Alarma de  checkeo configurada correctamente", Toast.LENGTH_SHORT).show()
                return true
            }else{
                Toast.makeText(this@ConfigActivity,"No se pudo configurar la alarma",Toast.LENGTH_SHORT).show()
                return false
            }

        }
    }


    private fun listenerCmdCancelConfig() {
        finish()
    }


    private fun configActionBar() {
        val actionBar = supportActionBar
        actionBar?.title = "AbuMonitor"
        actionBar?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        val textColor = SpannableString(actionBar?.title ?: "")
        textColor.setSpan(ForegroundColorSpan(Color.WHITE), 0, textColor.length, 0)
        actionBar?.title = textColor
    }

    private fun listenerCmdTimeAlarmForChecks() {
        showCustomTimePicker { hour,minute->
            SharedVariables.hourAlarmBetweenCheck=hour
            SharedVariables.minuteAlramBetweenCheck=minute

            isChangedAlarmBetweenChecks=true
            cmdSaveConfig?.isEnabled=true
            cmdTimeAlarmBetweenChecks?.text = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
        }

    }

    private fun showCustomTimePicker(
        minHour: Int = 0,
        maxHour: Int = 6,
        minMinute: Int = 3,
        maxMinute: Int = 59,
        onTimeSet: (hour: Int, minute: Int) -> Unit
    ) {
        val view = layoutInflater.inflate(R.layout.dialog_timerpicker, null)
        val npHour = view.findViewById<NumberPicker>(R.id.npHour)
        val npMinute = view.findViewById<NumberPicker>(R.id.npMinute)

        // Configurar límites de hora
        npHour.minValue = minHour
        npHour.maxValue = maxHour
        npHour.value = minHour // valor inicial

        // Configurar minutos (0–59)
        npMinute.minValue = minMinute
        npMinute.maxValue = maxMinute
        npMinute.value = minMinute

        AlertDialog.Builder(this)
            .setTitle("Seleccionar el tiempo")
            .setView(view)
            .setPositiveButton("OK") { _, _ ->
                val hour = npHour.value
                val minute = npMinute.value
                onTimeSet(hour, minute)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

}

