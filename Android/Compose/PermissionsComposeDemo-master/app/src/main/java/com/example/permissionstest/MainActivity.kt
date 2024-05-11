package com.example.permissionstest

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@ExperimentalPermissionsApi
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            //Esto es para pedir un solo permiso
            //RequestPermission(permission = Manifest.permission.READ_CONTACTS)

            //Esto es para pedir varios permisos
            RequestMultiplePermissions(
                permissions = listOf(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.CAMERA,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            )
        }
    }
}