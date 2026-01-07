package com.example.comunicationwearmobile.ui.view.activities.menu_option

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.viewmodel.ConfigViewModel


class ConfigActivity : AppCompatActivity() {

    private lateinit var vm: ConfigViewModel

    private var cmdSaveConfig: Button? = null
    private var cmdCancelConfig: Button? = null
    private var cmdRememberHour: Button? = null
    private var txtNameUser:EditText?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuration)

        vm = ViewModelProvider(this)[ConfigViewModel::class.java]

        cmdSaveConfig = findViewById(R.id.cmdSaveConfig)
        cmdCancelConfig = findViewById(R.id.cmdCancelConfig)
        cmdRememberHour = findViewById(R.id.cmdRememberHour)
        txtNameUser       = findViewById(R.id.txtNameUser)

        configObservers()
        initConfiguration()

        // Listeners
        cmdCancelConfig?.setOnClickListener { listenerCmdCancel() }
        cmdSaveConfig?.setOnClickListener { listenerCmdSaveConfig() }
        cmdRememberHour?.setOnClickListener{listenerCmdRememberHour()}
        txtNameUser?.addTextChangedListener{listenerChangeNameUser()}

        configActionBar()
    }

    private fun listenerChangeNameUser() {
        vm.markAsChangedNameUser()
    }


    private fun initConfiguration() {
        // Estado inicial
        vm.loadConfiguration()
    }

    private fun listenerCmdSaveConfig() {
        vm.save(txtNameUser?.text.toString())
    }



    private fun listenerCmdRememberHour() {
        showCustomTimePicker(maxHour=Definition.MAX_HOUR_DTPICKER_REMEMBER) {h, m -> vm.onTimePickedRemember(h, m) }
    }
    private fun listenerCmdCancel() {
        finish()
    }

    private fun configObservers() {

        // Observers
        vm.nameUser.observe(this){ text->
            txtNameUser?.setText(text)
        }

        vm.timeTextRemember.observe(this){text->
            cmdRememberHour?.text=text
        }

        vm.saveEnabled.observe(this) { enabled ->
            cmdSaveConfig?.isEnabled = enabled
        }
        vm.toastMessage.observe(this) { msg ->
            msg?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                vm.onToastShown()
            }
        }
        vm.finishEvent.observe(this) { finish ->
            if (finish == true) {
                vm.onFinishConsumed()
                finish()
            }
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

    override fun onDestroy() {
        super.onDestroy()
        vm.onFinishConsumed()
        vm.timeTextCheck.removeObservers(this)
        vm.nameUser.removeObservers(this)
        vm.timeTextNextAlarm.removeObservers(this)
        vm.timeTextRemember.removeObservers(this)
        vm.saveEnabled.removeObservers(this)
        vm.toastMessage.removeObservers(this)
        vm.finishEvent.removeObservers(this)
    }
}

