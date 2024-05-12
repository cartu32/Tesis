package com.example.comunicationwearmobile.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.common.PermissionManager
import com.example.comunicationwearmobile.common.Utils
import com.example.comunicationwearmobile.presenter.MainActivityPresenter

class MainActivity : ComponentActivity() {

    private var cmdSendWear: Button? = null
    private var txtMsgWear: TextView? = null

    private var permissionManager: PermissionManager? =null
    private var mainActivityPresenter: MainActivityPresenter? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //aca va el codigo de la activity
        setContentView(R.layout.activity_main)

        cmdSendWear = findViewById<Button>(R.id.cmdSendWear)
        txtMsgWear = findViewById<TextView>(R.id.txtMsgWear)

        cmdSendWear?.setOnClickListener(listenerButton)

        permissionManager= PermissionManager(this)
        mainActivityPresenter=MainActivityPresenter(this)

        permissionManager!!.checkPermissionGiven()
    }

    private val listenerButton = View.OnClickListener {
       Utils.showToast(this,"Solicitando Permisos")
        mainActivityPresenter?.sendDataWear()
    }

}


