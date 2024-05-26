package com.example.comunicationwearmobile.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.common.PermissionManager
import com.example.comunicationwearmobile.common.Utils
import com.example.comunicationwearmobile.presenter.MainActivityPresenter
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var cmdSendWear: Button? = null
    private var txtMsgWear: TextView? = null

    private var permissionManager: PermissionManager? =null
    private var mainActivityPresenter: MainActivityPresenter? =null

    private var transcriptionNodeId: String? = null
    companion object{
        private const val TAG = "MainWearActivity"
        private const val MESSAGE_PATH = "/deploy"
    }

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
       // mainActivityPresenter?.sendDataToWearable("/mensaje","key","Esteban")
        sendDataWearable2()

    }
//******************************+
    private fun getNodes(): Collection<String> {
        return Tasks.await(Wearable.getNodeClient(applicationContext).connectedNodes).map { it.id }
    }

    private fun sendDataWearable2()
    {
        lifecycleScope.launch(Dispatchers.IO) {
            transcriptionNodeId = getNodes().first().also { nodeId ->
                val sendTask: Task<*> = Wearable.getMessageClient(applicationContext).sendMessage(
                    nodeId,
                    MESSAGE_PATH,
                    "deploy".toByteArray() //send your desired information here
                ).apply {
                    addOnSuccessListener { Log.d(TAG, "OnSuccess") }
                    addOnFailureListener { Log.d(TAG, "OnFailure") }
                }
            }

        }
    }
}


