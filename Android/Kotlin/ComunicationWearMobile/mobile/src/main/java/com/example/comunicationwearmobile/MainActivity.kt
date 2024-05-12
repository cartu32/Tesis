package com.example.comunicationwearmobile

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.comunicationwearmobile.ui.MainActivityJava

class MainActivity : ComponentActivity() {
    var mainActivity: MainActivityJava? = null

    @RequiresApi(Build.VERSION_CODES.S)
    val PERMISSON = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //aca va el codigo de la activity
        setContentView(R.layout.activity_main)

        mainActivity = MainActivityJava(this)
        checkPermissionGiven()
    }

    @SuppressLint("NewApi")
    private fun checkPermissionGiven() {


        val permissionsToRequest = mutableListOf<String>()

        for (permission in PERMISSON) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(permission)
            }
        }

        if (permissionsToRequest.isEmpty()) {
            // Todos los permisos necesarios están otorgados
            // Aquí puedes realizar la acción que requiere permisos
            showToast("Permisos otorgados")
        } else {
            // Al menos un permiso no está otorgado
            // Solicitar los permisos que faltan
            launchMultiPermission(permissionsToRequest.toTypedArray())
        }
    }

    @SuppressLint("NewApi")
    private fun launchMultiPermission(toTypedArray: Array<String>) {
        requestMultiplePermissions.launch(toTypedArray)
    }
    private val requestMultiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        var allPermissionsGranted = true  // Variable para rastrear si se otorgaron todos
        permissions.entries.forEach {
            if (!it.value) {
                allPermissionsGranted = false
                showToast("Permiso no otorgado: ${it.key}")
                Log.e("DEBUG", "${it.key} = ${it.value}")
            }

        }
        if (allPermissionsGranted) {
            showToast("Todos los permisos otorgados")
        }
    }
    private fun showToast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()

    }
}


