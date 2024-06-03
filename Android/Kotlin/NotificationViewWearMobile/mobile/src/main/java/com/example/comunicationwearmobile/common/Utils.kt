package com.example.comunicationwearmobile.common

import android.widget.Toast
import androidx.activity.ComponentActivity

object Utils
{
    fun showToast(activity: ComponentActivity, s: String) {
        Toast.makeText(activity, s, Toast.LENGTH_SHORT).show()
    }
}