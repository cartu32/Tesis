package com.example.viemodel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.viemodel.main.BookViewModel
import com.example.viemodel.main.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Para crear un viewmodel, no se puede hacer dentro de un Compasoble
        val bookViewModel by viewModels<BookViewModel> ()

        setContent {
            MainScreen(bookViewModel)
        }
    }
}
