package com.example.comunicationwearmobile.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.abumonitor.data.model.EntityScheduledAssistance
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.model.repository.RepositoryScheduleAssistance
import kotlinx.coroutines.launch

class AssistanceDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repoAssistance = RepositoryScheduleAssistance(application, viewModelScope)
    private val repositoryAreaDB = RepositoryAreaDB(application, viewModelScope)

    private val _assistanceDetail = MutableLiveData<EntityScheduledAssistance>()
    val assistanceDetail: LiveData<EntityScheduledAssistance> get() = _assistanceDetail

    fun loadAssistanceDetail(id: Int) {
        viewModelScope.launch {
            val result = repoAssistance.getAssistanceWithId(id)
            _assistanceDetail.postValue(result)
        }
    }

    fun deleteAreaById(id: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            repositoryAreaDB.deleteAreaWithId(id)
            onComplete()
        }
    }
}
