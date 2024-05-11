package com.example.comunicationwearmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.MainActivityJava

class MainActivity : ComponentActivity() {
    var mainActivity: MainActivityJava? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //aca va el codigo de la activity
        setContentView(R.layout.activity_main)

        mainActivity = MainActivityJava(this)
    }
}