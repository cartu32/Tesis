package com.example.abumonitor.ui.view

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.abumonitor.R
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.datasource.local.AbuMonitorDatabase
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.ui.viewmodel.ViewModelFactory
import com.example.abumonitor.ui.viewmodel.ViewmodelAreaGeofence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Time

class MainActivity : AppCompatActivity() {
    private lateinit var viewmodelAreaGeofence: ViewmodelAreaGeofence
    private lateinit var factory: ViewModelFactory
    private lateinit var cmdDelete:Button
    private lateinit var cmdModify:Button
    private val TAG = "ViewmodelMain"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configView()
        configObserverLivedata()

     }

    private fun configObserverLivedata() {
        //asoscio el viewmodelAreaGeofence usando el factory a la view del MainActivity
        factory=Definition.factory
        viewmodelAreaGeofence=ViewModelProvider(this,factory)[ViewmodelAreaGeofence::class.java]

        configObserverShowMessage()
        configObserverInsertRegister()
        configObserverListJoinAreaGeofence()
    }

    private fun configObserverListJoinAreaGeofence() {
        viewmodelAreaGeofence.listJoinAreaGeofence.observe(this){ listJoin->
            listJoin.forEach{
                Log.i(TAG,"latitude:${it.latitude}\\n" +
                               "longitude:${it.longitude} \\n" +
                               "meters:${it.meters} \\n" +
                               "color:${it.name_color} \\n\\n")
            }

        }
    }

    private fun configObserverInsertRegister() {
        viewmodelAreaGeofence.isInitialized.observe(this){isInititliazed->
            if (isInititliazed){
                insertNewAreaGeofence()
                consultAreaGeofence()
            }
        }
    }

    private fun consultAreaGeofence() {
        viewmodelAreaGeofence.getAreaWithId(1)
    }

    private fun configObserverShowMessage() {
        //Se definene los observer de los livedata generados en el viewmodelAreaGeofence
        viewmodelAreaGeofence.showMessageView.observe(this){msg->
            showMessage(msg)
        }
    }

    fun showMessage(msg:String){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun insertNewAreaGeofence() {
        val entityAreaGeofence=EntityAreaGeofence(
            latitude = -34.681680,
            longitude = -58.554367,
            meters = 100,
            id_type_area = 1,
            id_color = 1,
            id_event = 2,
            security_zone = false,
            dwell_time = Time.valueOf("00:10:00"),
            id_contact = 2,
            id_priority = 1
        )
        viewmodelAreaGeofence.insertAreaGeofence(entityAreaGeofence)
        val entityAreaGeofence2=EntityAreaGeofence(
            latitude = -34.681650,
            longitude = -58.554200,
            meters = 300,
            id_type_area = 2,
            id_color = 2,
            id_event = 1,
            security_zone = true,
            dwell_time = Time.valueOf("00:50:00"),
            id_contact = 1,
            id_priority = 1
        )
        viewmodelAreaGeofence.insertAreaGeofence(entityAreaGeofence2)
    }


    private fun configView(){
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left , systemBars.top , systemBars.right , systemBars.bottom)
            insets
        }

        cmdDelete=findViewById<Button>(R.id.cmdDelete)
        cmdModify=findViewById<Button>(R.id.cmdModify)

        cmdDelete.setOnClickListener(listenerButton)
        cmdModify.setOnClickListener(listenerButton)
    }

    private fun updateAreaGeofence() {
        var id_area_modifiy = 2
        val entityAreaGeofence=EntityAreaGeofence(
            id_area = id_area_modifiy,
            latitude =  -34.4567787,
            longitude = -58.4546999,
            meters = 500,
            id_type_area = 3,
            id_color = 3,
            id_event = 3,
            security_zone = true,
            dwell_time = Time.valueOf("01:00:00"),
            id_contact = 1,
            id_priority = 3
        )
        viewmodelAreaGeofence.updateAreaGeofence(entityAreaGeofence)
    }

    // Crear un listener compartido
    val listenerButton = View.OnClickListener { view ->
        val idAreaDelete = 1
        when (view.id) {
            R.id.cmdDelete -> viewmodelAreaGeofence.deleteAreaGeofence(idAreaDelete)
            R.id.cmdModify -> updateAreaGeofence()
        }
    }
}