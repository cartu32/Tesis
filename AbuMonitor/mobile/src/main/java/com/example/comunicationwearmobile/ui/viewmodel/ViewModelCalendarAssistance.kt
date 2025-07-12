package com.example.comunicationwearmobile.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.abumonitor.data.model.EntityScheduledAssistance
import com.example.comunicationwearmobile.ui.model.repository.RepositoryScheduleAssistance
import kotlinx.coroutines.launch

class ViewModelCalendarAssistance(application: Application) : AndroidViewModel(application) {

    private val repoAssistance=RepositoryScheduleAssistance.getInstance(application.applicationContext,viewModelScope)

    fun getEventsByDate(date: Long): LiveData<List<EntityScheduledAssistance>> = repoAssistance.getEventsByDate(date)
    fun getAllEvents(): LiveData<List<EntityScheduledAssistance>> = repoAssistance.getAllScheduleAssitance()

    fun insert(assistance: EntityScheduledAssistance) = viewModelScope.launch {
        repoAssistance.insertScheduledAssistance(assistance,"8","8",300)
    }
}

class AssistanceViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViewModelCalendarAssistance::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ViewModelCalendarAssistance(app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 