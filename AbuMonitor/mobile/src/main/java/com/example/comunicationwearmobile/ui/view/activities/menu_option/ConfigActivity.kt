package com.example.comunicationwearmobile.ui.view.activities.menu_option

import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.common.SharedVariables
import com.example.comunicationwearmobile.ui.utils.Helpers.AlarmHelper
import com.example.comunicationwearmobile.ui.utils.broadcast.AlarmDailyActivateGeofReceiver
import java.util.Calendar
import java.util.Locale

class ConfigActivity: AppCompatActivity() {

    private var cmdDateAlarmActivateGeof: Button? = null
    private var cmdDateAlarmCheckAssistance: Button? = null
    private var cmdSaveConfig: Button? = null
    private var cmdCancelConfig: Button? = null


    private var isChangedAlarmActivateGeof=false
    private var isChangedAlarmCheckAssistance=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuration)

        cmdDateAlarmActivateGeof= findViewById<Button>(R.id.cmdDateAlarmActivateGeof)
        cmdDateAlarmCheckAssistance= findViewById<Button>(R.id.cmdDateAlarmCheckAssistance)
        cmdSaveConfig = findViewById<Button>(R.id.cmdSaveConfig)
        cmdCancelConfig = findViewById<Button>(R.id.cmdCancelConfig)

        cmdCancelConfig?.setOnClickListener {listenerCmdCancelConfig()}
        cmdSaveConfig?.setOnClickListener {listenerCmdSaveConfig()}
        cmdDateAlarmActivateGeof?.setOnClickListener {listenerCmdDateAlarmActivateGeof()}
        cmdDateAlarmCheckAssistance?.setOnClickListener {listenerCmdDateAlarmCheckAssistance()}

        //seteo el texto del boton para configurar la alarma para activar geofence segun el valor cargado.
        cmdDateAlarmActivateGeof?.text = String.format(Locale.getDefault(), "%02d:%02d", SharedVariables.hourDailyActivateGeofence, SharedVariables.minuteDailyActivateGeofence)
        cmdDateAlarmCheckAssistance?.text = String.format(Locale.getDefault(), "%02d:%02d", SharedVariables.hourDailyCheckAssitance, SharedVariables.minuteDailyCheckAssitance)

        cmdSaveConfig?.isEnabled=false

        configActionBar()
    }

    private fun listenerCmdSaveConfig() {
        if(saveHourAlarmActivateGeof() || saveHourAlarmCheckAssistance())
            finish()
        else
            Toast.makeText(this@ConfigActivity,"No se realizaron cambios en la alarma",Toast.LENGTH_SHORT).show()

    }

    private fun saveHourAlarmActivateGeof(): Boolean {
        with(SharedVariables) {
            val alarmHelper=AlarmHelper()

            if (isChangedAlarmActivateGeof) {
                alarmHelper.cancelAlarm(
                    this@ConfigActivity,
                    alarmIdActivateGeofence,
                    Definition.ACTION_ALARM_DAILY_ACTIVATION_GEOF,
                    AlarmDailyActivateGeofReceiver::class.java
                )

                Log.d(Definition.TAG_DEBUG, "Alarma Activate Geofence cancelada")

                alarmIdActivateGeofence =
                    alarmHelper.setDailyAlarm(
                        this@ConfigActivity,
                        hourDailyActivateGeofence,
                        minuteDailyActivateGeofence,
                        Definition.ACTION_ALARM_DAILY_ACTIVATION_GEOF,
                        AlarmDailyActivateGeofReceiver::class.java
                    )

                Log.d(Definition.TAG_DEBUG, "Alarma Activate Geofence configurada correctamente")
                Toast.makeText(this@ConfigActivity, "Alarma de activacion configurada correctamente", Toast.LENGTH_SHORT).show()
                return true
            }
            //Toast.makeText(this@ConfigActivity,"No se pudo guardar la hora de la alarma",Toast.LENGTH_SHORT).show()
        }
        return false
    }

    private fun saveHourAlarmCheckAssistance(): Boolean {
        with(SharedVariables) {
            val alarmHelper=AlarmHelper()

            if (isChangedAlarmCheckAssistance) {
                alarmHelper.cancelAlarm(
                    this@ConfigActivity,
                    alarmIdCheckAssitance,
                    Definition.ACTION_ALARM_DAILY_CHECK_ASSISTANCE,
                    AlarmDailyActivateGeofReceiver::class.java
                )

                Log.d(Definition.TAG_DEBUG, "Alarma de chekear asistencia cancelada")

                alarmIdActivateGeofence =
                    alarmHelper.setDailyAlarm(
                        this@ConfigActivity,
                        hourDailyCheckAssitance,
                        minuteDailyCheckAssitance,
                        Definition.ACTION_ALARM_DAILY_CHECK_ASSISTANCE,
                        AlarmDailyActivateGeofReceiver::class.java
                    )

                Log.d(Definition.TAG_DEBUG, "Alarma de chekear asistencia configurada corrctamente")
                Toast.makeText(this@ConfigActivity, "Alarma de checkeo configurada correctamente", Toast.LENGTH_SHORT).show()
                return true
            }
            //Toast.makeText(this@ConfigActivity,"No se pudo guardar la hora de la alarma",Toast.LENGTH_SHORT).show()
        }
        return false
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

    private fun listenerCmdDateAlarmActivateGeof() {
        showTimePicker { hour,minute->
            SharedVariables.hourDailyActivateGeofence=hour
            SharedVariables.minuteDailyActivateGeofence=minute

            isChangedAlarmActivateGeof=true
            cmdSaveConfig?.isEnabled=true
            cmdDateAlarmActivateGeof?.text = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
        }

    }

    private fun listenerCmdDateAlarmCheckAssistance() {
        showTimePicker { hour,minute->
            SharedVariables.hourDailyCheckAssitance=hour
            SharedVariables.minuteDailyCheckAssitance=minute

            isChangedAlarmCheckAssistance=true
            cmdSaveConfig?.isEnabled=true
            cmdDateAlarmCheckAssistance?.text = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
        }

    }

    private fun showTimePicker(onTimeSet: (hour:Int,minute:Int) -> Unit) {
        val cal = Calendar.getInstance()

        TimePickerDialog(this, { _, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            onTimeSet(hour,minute)
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }
}

