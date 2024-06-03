package com.example.comunicationwearmobile.common

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

class PermissionManager(activity: ComponentActivity) {
    private val activity:ComponentActivity = activity

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

    @SuppressLint("NewApi")
    fun checkPermissionGiven() {


        val permissionsToRequest = mutableListOf<String>()

        for (permission in PERMISSON) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(permission)
            }
        }

        if (permissionsToRequest.isEmpty()) {
            // Todos los permisos necesarios están otorgados
            // Aquí puedes realizar la acción que requiere permisos
            Utils.showToast(activity, "Permisos otorgados")
        } else {
            // Al menos un permiso no está otorgado
            // Solicitar los permisos que faltan
            launchMultiPermission(permissionsToRequest.toTypedArray())
        }
    }

    @SuppressLint("NewApi")
    public fun launchMultiPermission(toTypedArray: Array<String>) {
        requestMultiplePermissions.launch(toTypedArray)
    }
    private val requestMultiplePermissions = activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        var allPermissionsGranted = true  // Variable para rastrear si se otorgaron todos
        permissions.entries.forEach {
            if (!it.value) {
                allPermissionsGranted = false
                Utils.showToast(activity, "Permiso no otorgado: ${it.key}")
                Log.e("DEBUG", "${it.key} = ${it.value}")
            }

        }
        if (allPermissionsGranted) {
            Utils.showToast(activity,"Todos los permisos otorgados")
        }
    }

}