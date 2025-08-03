package com.example.comunicationwearmobile.ui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.data.model.EntityScheduledAssistance
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.model.dto.DataAreaGeofAux
import com.example.comunicationwearmobile.ui.model.repository.RepositoryGeofActivate
import com.example.comunicationwearmobile.ui.model.repository.RepositoryScheduleAssistance
import com.example.comunicationwearmobile.ui.utils.Tools
import kotlinx.coroutines.launch

class ViewModelCalendarAssistance(application: Application) : AndroidViewModel(application) {

    private var repositoryGeofActivate: RepositoryGeofActivate = RepositoryGeofActivate()
    private var repositoryAreaDB: RepositoryAreaDB = RepositoryAreaDB.getInstance(application.applicationContext)
    private val repoAssistance = RepositoryScheduleAssistance.getInstance(application.applicationContext)

    private val _idNewAssistance = MutableLiveData<Long>()
    val idNewAssistance: LiveData<Long> get() = _idNewAssistance

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

    fun insert(
        context: Context,
        assistance: EntityScheduledAssistance,
        latitude: String,
        longitude: String,
        meters: Int
    ) {
        viewModelScope.launch {
            val result = handleInsertionDateAssistance(context, assistance, latitude, longitude, meters)
            _idNewAssistance.postValue(result)
        }
    }

    //este metodo realiza toda la insercion de datos en la base de datos
    private suspend fun handleInsertionDateAssistance(
        context: Context,
        assistance: EntityScheduledAssistance,
        latitude: String,
        longitude: String,
        meters: Int
    ): Long {
        //creo e inserto una nueva area de geofence en la bd
        val dataAreaGeofAux = createAreaGeof(latitude, longitude, meters)
        val idNewArea = insertArea(dataAreaGeofAux)

        //si no se pudo insertar la nueva area en la bd
        if(idNewArea<0) {
            return Definition.ERROR_INSERT_BD_GEOF
        }
        //si se pudo insertar la nueva area en la bd, se inserta la nueva cita de asistencia
        dataAreaGeofAux.entityAreaGeofence.id_area = idNewArea
        assistance.id_area=idNewArea
        val idNewAssistance = insertAssistance(assistance)

        //si no se pudo insertar la nueva cita de asistencia en la bd
        if(idNewAssistance<0) {
            rollbackArea(idNewArea)
            return Definition.ERROR_INSERT_BD_GEOF
        }

        //si se pudo insertar la nueva cita de asistencia en la bd, se activa el geofence
        //me fijo si corresponde a la fecha a partir de mañana. Osea que no sea hoy(la fecha actual)
        if(!Tools.isToday(assistance.date_appointment)){
            Log.d(Definition.TAG_DEBUG,"No se activo el geofence para la cita porque no es de hoy")
            return idNewAssistance
        }
        //
        val geofenceActivated = activateGeofence(context, dataAreaGeofAux)
        //si no se pudo activar el geofence
        if (!geofenceActivated) {
            rollbackArea(idNewArea)
            return Definition.ERROR_ACTIVATE_GEOF
        }
        Log.d(Definition.TAG_DEBUG,"Se activo el geofence para la cita para hoy")

        return idNewAssistance
    }

    private suspend fun insertArea(dataAreaGeofAux: DataAreaGeofAux): Long {
        return repositoryAreaDB.insertAreaGeofence(dataAreaGeofAux)
    }

    private suspend fun insertAssistance(assistance: EntityScheduledAssistance): Long {
        return repoAssistance.insertScheduledAssistance(assistance)
    }

    private suspend fun rollbackArea(areaId: Long) {
        repositoryAreaDB.deleteAreaWithId(areaId)
    }

    private suspend fun activateGeofence(context: Context, dataAreaGeofAux: DataAreaGeofAux): Boolean {
        return repositoryGeofActivate.activateGeofence(context, dataAreaGeofAux)
    }

    // Configura los datos para el área geográfica
    private fun createAreaGeof(latitude: String, longitude: String, meters: Int): DataAreaGeofAux {
        return DataAreaGeofAux().apply {
            entityAreaGeofence = EntityAreaGeofence().apply {
                description = "Area de Asistencia"
                this.latitude = latitude
                this.longitude = longitude
                this.meters = meters
                id_priority = Definition.PRIORITY_ID_LOW
                id_type_area = Definition.TYPE_AREA_ID_ASSISTANCE
            }
            listIdEventSelected = mutableListOf(
                Definition.GEOFENCE_EVENT_ID_ENTER,
                Definition.GEOFENCE_EVENT_ID_EXIT
            )
            secZoneTimeRange = null
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
