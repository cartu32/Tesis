package com.example.comunicationwearmobile.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.abumonitor.data.model.EntityScheduledAssistance
import com.example.comunicationwearmobile.ui.model.extra.InsertResultAssistance
import com.example.comunicationwearmobile.ui.model.repository.RepositoryGeofActivate
import com.example.comunicationwearmobile.ui.model.repository.RepositoryScheduleAssistance
import kotlinx.coroutines.launch

class ViewModelCalendarAssistance(application: Application) : AndroidViewModel(application) {


    private var repositoryGeofActivate: RepositoryGeofActivate=RepositoryGeofActivate()

    private val _idNewAssistance = MutableLiveData<Long>()
    val idNewAssistance: LiveData<Long> get() = _idNewAssistance

    private val repoAssistance = RepositoryScheduleAssistance
        .getInstance(application.applicationContext, viewModelScope)

    // MutableLiveData para la fecha seleccionada
    val selectedDateMillis = MutableLiveData<Long>()

    /*aca se uso un switchMap para observar los cambios en la fecha seleccionada
    en la view. Esto se hizo para que cada vez que se hace click en una fecha,
    no se generen un observer por cada click. Generando multiples obsever

    Para evitar eso se creo un livedata llamado selectedDateMillis que se
    que se usa para que la activty le avise a viewmodel que se selecciono una fecha nueva.
    el observer de este livedata se crea con switchmap. Luego de este observer se consulta
    la base de datos con getEventsBydate, y una vez que se obtengan los datos, se actualiza
    se usa el livedata eventbyselectdate para avisarle a la activity que se actualizo la lista
    de eventos.

    Entonces de esta manera hay 2 livedata
    1) selectedDateMillis: en endonde la activty le avisa a la viewmodel que se selecciono una fecha
    2) eventsBySelectedDate: en donde la viewmodel actualiza la lista de eventos segun los datos obtenidos de la base de datos
    */
    val eventsBySelectedDate: LiveData<List<EntityScheduledAssistance>> =
        selectedDateMillis.switchMap { date ->
            repoAssistance.getEventsByDate(date)
        }

    fun getAllEvents(): LiveData<List<EntityScheduledAssistance>> =
        repoAssistance.getAllScheduleAssitance()

    fun insert(assistance: EntityScheduledAssistance, latitude: String, longitude: String, meters: Int) {
        viewModelScope.launch {
            var insertResultAssistance: InsertResultAssistance? =null

            insertResultAssistance=repoAssistance.insertScheduledAssistance(assistance, latitude, longitude, meters)

            //si se pudo insertar correctamente la nueva area en la base de datos
            if (insertResultAssistance!=null) {
                //se debe activar el area de geofence para la deteccion del evento
                //FALTA HACER
            }
            _idNewAssistance.postValue(insertResultAssistance?.idAssistance)
        }
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
