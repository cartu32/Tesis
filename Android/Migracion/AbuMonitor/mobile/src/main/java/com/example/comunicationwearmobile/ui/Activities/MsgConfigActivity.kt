package com.example.comunicationwearmobile.ui.Activities

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.Interface.InterfaceMainAct
import com.example.comunicationwearmobile.common.PermissionManager
import com.example.comunicationwearmobile.common.Utils
import com.example.comunicationwearmobile.presenter.notificationWear.MsgConfigPresenter
import com.example.shared_library.SharedData
import java.text.SimpleDateFormat

@Suppress("NAME_SHADOWING")
class MsgConfigActivity : AppCompatActivity(), InterfaceMainAct {

    private var cmdSendWear: Button? = null
    private var txtMsgFromWear: TextView? = null

    private var txtMsgTitle: EditText?= null
    private var txtMsg: EditText?= null
    private var txtDate:TextView?=null
    private var txtTime: TextView?=null
    private var spTypeNotification:Spinner?=null

    private var permissionManager: PermissionManager? =null
    private var mainActivityPresenter: MsgConfigPresenter? =null

    private var  selectedItemSP:SharedData.TypeNotification = SharedData.TypeNotification.WithoutNotifications
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //aca va el codigo de la activity
        setContentView(R.layout.activity_sms_config)

        cmdSendWear    = findViewById<Button>(R.id.cmd
        txtMsgFromWear = findViewById<TextView>(R.id.txtMsgFromWear)
        txtMsgTitle    = findViewById<EditText>(R.id.txtMsgTitle)
        txtMsg         = findViewById<EditText>(R.id.txtMsg)
        txtDate        = findViewById<TextView>(R.id.txtDate)
        txtTime        = findViewById<TextView>(R.id.txtClock)
        spTypeNotification = findViewById<Spinner>(R.id.spTypeNotification)


        permissionManager= PermissionManager(this)
        mainActivityPresenter= MsgConfigPresenter(this)

        loadSpiner()

        txtDate?.setOnClickListener(listenerDate)
        txtTime?.setOnClickListener(listenerTime)
        cmdSendWear?.setOnClickListener(listenerButton)
        spTypeNotification?.onItemSelectedListener = listenerSpinner


        permissionManager!!.checkPermissionGiven()
    }

    private fun loadSpiner() {
        var arrayList:ArrayList<String> = ArrayList()

        arrayList.add("Seleccione una opcion")
        arrayList.add(SharedData.TypeNotification.Alert.name)
        arrayList.add(SharedData.TypeNotification.Reminder.name)

        var adapter:ArrayAdapter<String> = ArrayAdapter(this,android.R.layout.simple_spinner_item,arrayList)
        adapter.setDropDownViewResource(android.R.layout.select_dialog_item)
        spTypeNotification?.adapter = adapter

        spTypeNotification?.setSelection(0, false)

    }

    // Configura el listener para el Spinner
    private val listenerSpinner = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            if (position == 0) {
                return
            }

            // Acción cuando un ítem es seleccionado
            selectedItemSP = SharedData.TypeNotification.valueOf(parent.getItemAtPosition(position).toString())
            Toast.makeText(parent.context, "Seleccionado: $selectedItemSP", Toast.LENGTH_SHORT).show()
        }

        override fun onNothingSelected(parent: AdapterView<*>) {
            // Acción cuando nada es seleccionado
        }
    }

    @SuppressLint("SimpleDateFormat")
    private val listenerTime= View.OnClickListener {
        val selecetedTime = Calendar.getInstance()
        val hour   =  selecetedTime.get(Calendar.HOUR_OF_DAY)
        val minute =  selecetedTime.get(Calendar.MINUTE)

        val listener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            selecetedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
            selecetedTime.set(Calendar.MINUTE,minute)

            txtTime?.text  = SimpleDateFormat("HH:mm").format(selecetedTime.time)
        }
        TimePickerDialog(this, listener, hour, minute, true).show()
    }

    @SuppressLint("SetTextI18n")
    private val listenerDate= View.OnClickListener {
        val selectedCalendar = Calendar.getInstance()
        val year  = selectedCalendar.get(Calendar.YEAR)
        val month = selectedCalendar.get(Calendar.MONTH)
        val day   = selectedCalendar.get(Calendar.DAY_OF_MONTH)

        val listener = DatePickerDialog.OnDateSetListener{
            view: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->

            txtDate?.text = "$dayOfMonth/${month+1}/$year"
        }
        val datePickerDialog= DatePickerDialog(this,listener,year,month,day)

        datePickerDialog.show()
    }



    private val listenerButton = View.OnClickListener {
       val msgNotification:SharedData.MsgNotification

       msgNotification = SharedData.MsgNotification(
           title = txtMsgTitle?.text.toString(),
           message = txtMsg?.text.toString(),
           typeNotification =selectedItemSP,
           date = txtDate?.text.toString(),
           hour =txtTime?.text.toString()
       )
       Utils.showToast(this,"Enviando datos a Wear OS")
       mainActivityPresenter?.sendNotificationToWearable(msgNotification)

    }


    override fun showToast(msg: String) {
        Toast.makeText(this,msg, Toast.LENGTH_SHORT).show()
    }

    override fun updateTextBox(msg: String) {
        txtMsgFromWear?.text=msg
    }
}


