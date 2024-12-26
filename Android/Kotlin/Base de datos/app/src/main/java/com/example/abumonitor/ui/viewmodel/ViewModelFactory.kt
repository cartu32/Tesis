package com.example.abumonitor.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory(private val creators: Map<Class<out ViewModel> , () -> ViewModel>) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val creator = creators[modelClass] ?: throw IllegalArgumentException("Clase ViewModel desconocida: $modelClass")
        return try {
            @Suppress("UNCHECKED_CAST")
            creator() as T
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}

