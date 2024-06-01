package com.example.comunicationwearmobile.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.common.InterfaceMainAct
import com.example.comunicationwearmobile.common.PermissionManager
import com.example.comunicationwearmobile.common.Utils
import com.example.comunicationwearmobile.models.WearableDataListenerService
import com.example.comunicationwearmobile.presenter.MainActivityPresenter
import com.example.shared_library.SharedData

class MainActivity : AppCompatActivity(),InterfaceMainAct {

    private var cmdSendWear: Button? = null
    private var txtMsgFromWear: TextView? = null
    private var txtMsgToWear: EditText?= null

    private var permissionManager: PermissionManager? =null
    private var mainActivityPresenter: MainActivityPresenter? =null
    private var wearableDataListenerService: WearableDataListenerService = WearableDataListenerService(
        this
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //aca va el codigo de la activity
        setContentView(R.layout.activity_main)

        cmdSendWear    = findViewById<Button>(R.id.cmdSendWear)
        txtMsgFromWear = findViewById<TextView>(R.id.txtMsgFromWear)
        txtMsgToWear   = findViewById(R.id.txtMsgToWear)

        permissionManager= PermissionManager(this)
        wearableDataListenerService=WearableDataListenerService(this)
           mainActivityPresenter=MainActivityPresenter(this,wearableDataListenerService)


        cmdSendWear?.setOnClickListener(listenerButton)
        permissionManager!!.checkPermissionGiven()
    }


    private val listenerButton = View.OnClickListener {
        var text="Vacio"

        if(txtMsgToWear!!.text.isNotEmpty())
            text= txtMsgToWear!!.text.toString()

       Utils.showToast(this,"Enviando datos a Wear OS")
       mainActivityPresenter?.sendDataWearable(SharedData.msg_mobile_to_wear,text)

    }


    override fun onDestroy() {
        super.onDestroy()

        mainActivityPresenter?.onCleared()
    }

    override fun showToast(msg: String) {
        Toast.makeText(this,msg, Toast.LENGTH_SHORT).show()
    }

    override fun updateTextBox(msg: String) {
        txtMsgFromWear?.text=msg
    }
}


