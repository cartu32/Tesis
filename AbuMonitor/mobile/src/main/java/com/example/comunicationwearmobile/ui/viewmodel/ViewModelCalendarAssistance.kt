package com.example.comunicationwearmobile.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.data.model.EntityScheduledAssistance
import com.example.comunicationwearmobile.ui.common.SharedVariables
import com.example.comunicationwearmobile.ui.model.dto.DataAreaGeofAux
import com.example.comunicationwearmobile.ui.model.repository.RepositoryConfigAppSPref
import com.example.comunicationwearmobile.ui.model.repository.RepositoryScheduleAssistance
import com.example.comunicationwearmobile.ui.utils.Helpers.Alarm.AlarmHelper.setNextAlarmAtExactTime
import com.example.comunicationwearmobile.ui.utils.Tools
import com.example.comunicationwearmobile.ui.utils.broadcast.AlarmBroadcastReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class ViewModelCalendarAssistance(application: Application) : AndroidViewModel(application) {

    private val repoAssistance = RepositoryScheduleAssistance.getInstance(application.applicationContext)
    private val repositoryConfigAppSPref=RepositoryConfigAppSPref.getInstance(application.applicationContext)

    private val _idNewAssistance = MutableLiveData<Long>()
    val idNewAssistance: LiveData<Long> get() = _idNewAssistance

    private val _timeDurationAppointment = MutableLiveData<Long>()
    val timeDurationAppointment: LiveData<Long> get() = _timeDurationAppointment

    // MutableLiveData para la fecha seleccionada
    val selectedDateMillis = MutableLiveData<Long>()

    private var timeBetweenChecksSaved: Long = 0




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
        selectedDateMillis
            .distinctUntilChanged()
            .switchMap { date ->
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
        viewModelScope.launch(Dispatchers.IO) {
            val result = handleInsertionDateAssistance(context, assistance, latitude, longitude, meters)

            withContext(Dispatchers.Main) {
                _idNewAssistance.postValue(result)
            }
        }
    }

    // Este método realiza toda la inserción de datos en la base de datos y su activación
    private suspend fun handleInsertionDateAssistance(
        context: Context,
        assistance: EntityScheduledAssistance,
        latitude: String,
        longitude: String,
        meters: Int
    ): Long=SharedVariables.mutexAssistanceDateAlarm.withLock {

        val now = System.currentTimeMillis()
        val startDateAppointment = assistance.date_hour_appointment

        val idAreaAssistance = insertNewScheduleAppointment(latitude, longitude, meters, assistance)

        sheduleNextAppointementAlarm(now, startDateAppointment, context)
        scheduleReminderAlarm(now,startDateAppointment,context)

        return idAreaAssistance

    }

    private suspend fun insertNewScheduleAppointment(
        latitude: String,
        longitude: String,
        meters: Int,
        assistance: EntityScheduledAssistance,
    ): Long {
        //indico en el registro que va a guardarse en la bd que es una cita de asistencia nueva
        assistance.is_new_appointment_assistance=true

        // 1) creo la nueva cita de asitencia y la guardo en la base de datos
        val newAreaGeofAux = createAreaGeof(latitude, longitude, meters)
        val idAreaAssistance = repoAssistance.insertScheduledAssistance(assistance, newAreaGeofAux)
        return idAreaAssistance
    }

    private suspend fun sheduleNextAppointementAlarm(
        now: Long,
        startDateAppointment: Long,
        context: Context,
    ) {
        // 2) Reconsulto mínimos reales de inicio y fin de las citas desde DB (ya con la nueva cita incluida)
        val nextStartNow = repoAssistance.getStartTimeOfNextAppointment(initIntervalAlarma = now)

        // 3) Programo SOLAMENTE si el comienzo del nueva cita quedó siendo la próxima real.
        //    o sea si es la mas chica de todas en el horario de inicio
        if (nextStartNow != null && nextStartNow == startDateAppointment) {
            setNextAlarmAtExactTime(
                context,
                alarmId = Definition.ALARM_ID_FOR_ACTIVATION_AREAS,
                triggerAtMillis = nextStartNow,
                action = Definition.ACTION_ALARM_FOR_ACTIVATION_AREA,
                receiverClass = AlarmBroadcastReceiver::class.java
            )
        }
    }

    private suspend fun scheduleReminderAlarm(now: Long,startDateAppointment:Long,context: Context) {

        val offsetTimeReminder=repositoryConfigAppSPref.getTimeRememberAppointment()
        val timeRelativeReminder=startDateAppointment-offsetTimeReminder
        val nextTimeReminder=repoAssistance.getTimeOfNextReminder(timeCurrentAlarm = now,offsetReminder=offsetTimeReminder)

        // 3) Programo SOLAMENTE si el comienzo del nuevo recordatorio quedó siendo la próxima real.
        //    o sea si es la mas chica de todas en el horario de inicio y mayor que el horario actual
        if (nextTimeReminder != null && nextTimeReminder == timeRelativeReminder && nextTimeReminder>now) {
            setNextAlarmAtExactTime(
                context,
                alarmId = Definition.ALARM_ID_FOR_REMINDER,
                triggerAtMillis = nextTimeReminder,
                action = Definition.ACTION_ALARM_FOR_REMINDER,
                receiverClass = AlarmBroadcastReceiver::class.java
            )
        }

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

    fun getTimeDurationAppointmentSaved() {
        viewModelScope.launch(Dispatchers.IO) {
            var timeBetweenChecks = repositoryConfigAppSPref.getTimeBetweenChecks()
			//incremento el doble de tiempo + 1 minutos de lo que esta configurada la alarma
			// por ejemplo si se ejecuta cada 3 miuntos entonces pongo como inicio 6+1 o sea 7 minutos
            timeBetweenChecks = (timeBetweenChecks * 2) + 60000
            //convierto el tiempo en minutos
            timeBetweenChecksSaved = Tools.convertMillisToMinutes(timeBetweenChecks)

            withContext(Dispatchers.Main) {
                _timeDurationAppointment.postValue(timeBetweenChecksSaved)
            }
        }
    }
    fun checkTimeAppointmentLessThanTimeAlarm(timeEnteredByUser:Long):Pair<Boolean,Long> {
        if (timeEnteredByUser>=timeBetweenChecksSaved)
            return Pair (true,0)
        else
            return Pair (false,timeBetweenChecksSaved)
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

