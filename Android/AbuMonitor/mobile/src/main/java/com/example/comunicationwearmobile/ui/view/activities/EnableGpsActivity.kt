package com.example.comunicationwearmobile.ui.view.activities

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class EnableGpsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showGpsDialog()
    }

    private fun showGpsDialog() {
        AlertDialog.Builder(this)
            .setMessage("El sistema GPS esta desactivado, ¿Desea activarlo?")
            .setPositiveButton("Si") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
}
