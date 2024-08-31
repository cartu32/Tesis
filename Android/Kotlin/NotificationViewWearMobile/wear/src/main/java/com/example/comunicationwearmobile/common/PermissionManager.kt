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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

class PermissionManager(activity: ComponentActivity) {
    private val activity:ComponentActivity = activity
    private var continuation: Continuation<Boolean>? = null

    @RequiresApi(Build.VERSION_CODES.S)
    val PERMISSON = arrayOf(
        Manifest.permission.ACTIVITY_RECOGNITION
    )

    @SuppressLint("NewApi")
    suspend fun checkPermissionGiven(): Boolean {
        var permissionGaranted=false

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
            permissionGaranted=true

        } else {
            // Al menos un permiso no está otorgado
            // Solicitar los permisos que faltan
             permissionGaranted=launchMultiPermission(permissionsToRequest.toTypedArray())
        }
        return permissionGaranted
    }
    @SuppressLint("NewApi")
    suspend fun launchMultiPermission(toTypedArray: Array<String>): Boolean {
        return suspendCancellableCoroutine { cont ->
            requestMultiplePermissions.launch(toTypedArray)
            this.continuation = cont
        }
    }


    private val requestMultiplePermissions = activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        var allPermissionsGranted = true

        permissions.entries.forEach {
            if (!it.value) {
                allPermissionsGranted = false
                Log.e("DEBUG", "${it.key} = ${it.value}")
            }
        }

        continuation?.resume(allPermissionsGranted)
    }

}