package com.example.comunicationwearmobile.ui.utils.Helpers.Geofences

import android.content.Context
import android.util.Log
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.repository.RepositoryAreaDB
import com.example.comunicationwearmobile.ui.model.pojo.JoinAreaGeofence
import com.example.comunicationwearmobile.ui.model.repository.RepositoryScheduleAssistance
import com.example.comunicationwearmobile.ui.model.repository.RepositorySecurityZoneSPref
import com.example.comunicationwearmobile.ui.utils.Helpers.Notification.NotificationManager
import com.example.comunicationwearmobile.ui.utils.Tools
import com.example.shared_library.SharedData
import com.google.android.gms.location.Geofence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

object GeofenceEventProcessorHelper {

    // Mutex por área para evitar carreras (ENTER/EXIT en paralelo del mismo id)
    private val areaMutexes: ConcurrentHashMap<Long, Mutex> = ConcurrentHashMap()
    private fun mutexFor(areaId: Long): Mutex = areaMutexes.getOrPut(areaId) { Mutex() }


    private lateinit var appContext: Context


    fun init(context: Context) {
        appContext = context.applicationContext
    }

    suspend fun handleEvent(triggeringIds: MutableList<Long>, transition: Int) {
        if (!::appContext.isInitialized) {
            Log.e(Definition.TAG_DEBUG, "GeofenceEventProcessorHelper no fue inicializado. Llamá init(context) antes de usarlo")
            return
        }

        try {
            processGeofenceEvent(triggeringIds, transition)
        } catch (e: TimeoutCancellationException) {
            Log.e(Definition.TAG_DEBUG, "Tiempo máximo excedido procesando evento de geofence", e)
        } catch (t: Throwable) {
            Log.e(Definition.TAG_DEBUG, "Error procesando evento de geofence", t)
        }
    }

    private suspend fun processGeofenceEvent(triggeringIds: List<Long>, transition: Int) {
        val repository = RepositoryAreaDB(appContext)

        for (idAreaGeofence in triggeringIds) {
            mutexFor(idAreaGeofence).withLock {
                val areaGeof = repository.getJoinAreaGeofence(idAreaGeofence) ?: return@withLock


                Log.d(Definition.TAG_DEBUG, "transicion: $transition")

                when (areaGeof.areaGeofence.id_type_area) {
                    Definition.TYPE_AREA_ID_NORMAL -> analizeNormalZone(areaGeof, transition)
                    Definition.TYPE_AREA_ID_SECURITY_ZONE -> analizeSecurityZone(areaGeof, transition)
                    Definition.TYPE_AREA_ID_ASSISTANCE -> analizeAssistanceZone(areaGeof.areaGeofence.id_area, transition,areaGeof.areaGeofence.latitude,areaGeof.areaGeofence.longitude)
                }
            }

            //esto evita que el map crezca para siempre cada vez que se crean y borran areas
            areaMutexes.remove(idAreaGeofence)
        }
    }

    suspend fun analizeNormalZone(areaGeof: JoinAreaGeofence?, transition: Int) {
        val msg = areaGeof?.areaGeofence?.let {
            NotificationManager.createMsg(transition, it.description, areaGeof.secDwellTimeZone?.dwell_time ?: 0)
        } ?: return

        NotificationManager.determineRecipientByPriority(
            areaGeof.areaGeofence.id_priority,
            msg,
            areaGeof.areaGeofence.latitude,
            areaGeof.areaGeofence.longitude
        )
    }

    private suspend fun analizeSecurityZone(areaGeof: JoinAreaGeofence, transition: Int) {
        when (transition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> processEnterSecurityZone(
                areaGeof.areaGeofence.description,
                areaGeof.areaGeofence.latitude,
                areaGeof.areaGeofence.longitude
            )
            Geofence.GEOFENCE_TRANSITION_EXIT -> processExitSecurityZone(
                areaGeof.areaGeofence.description,
                areaGeof.securityZoneTimeRange?.min_hour,
                areaGeof.securityZoneTimeRange?.max_hour,
                areaGeof.areaGeofence.latitude,
                areaGeof.areaGeofence.longitude
            )
        }
    }

    private suspend fun analizeAssistanceZone(idArea: Long, transition: Int, lat: String, lon: String) {
        when (transition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> processEnterAssistenceZone(idArea)
            Geofence.GEOFENCE_TRANSITION_EXIT -> proccessExitAssistanceZone(idArea, lat, lon)
        }
    }

    private suspend fun processEnterAssistenceZone(idArea: Long) {
        var respUpdate:Int
        val repositoryScheduleAssistance = RepositoryScheduleAssistance(appContext)
        val entityAssistance = repositoryScheduleAssistance.getAssistanceWithAreaId(idArea)
            ?: run {
                Log.w(Definition.TAG_DEBUG, "El area no se encuentra activada idArea=$idArea")
                return
            }

        with(entityAssistance) {
            if (went_appointment) {
                Log.d(Definition.TAG_DEBUG, "Ya asistio a la cita")
                return
            }

            if (!Tools.isToday(date_hour_appointment)) {
                Log.e(Definition.TAG_DEBUG, "Error en la fecha de la cita")
                return
            }

            if (!Tools.isTimeEnterAssistanceCorrect(date_hour_appointment)) {
                Log.d(Definition.TAG_DEBUG, "Se descarta la entrada porque no esta dentro del horario de la cita")
                return
            }

            date_hour_enter_assistance = System.currentTimeMillis()

            withContext(Dispatchers.IO) {
                respUpdate = repositoryScheduleAssistance.updateScheduleAssistance(entityAssistance)
            }
            if (respUpdate == 1) Log.d(Definition.TAG_DEBUG, "Hora de entrada de la cita actualizada")
            else Log.e(Definition.TAG_DEBUG, "Error no se pudo actualizar la cita")
        }
    }

    private suspend fun proccessExitAssistanceZone(idArea: Long, lat: String, lon: String) {
        var respUpdate:Int
        val minuteInMillis = 60000L

        val repositoryScheduleAssistance = RepositoryScheduleAssistance(appContext)
        val entityAssistance = repositoryScheduleAssistance.getAssistanceWithAreaId(idArea)
            ?: run {
                Log.w(Definition.TAG_DEBUG, "El area no se encuentra activada idArea=$idArea")
                return
            }


        with(entityAssistance) {
            if (went_appointment) {
                Log.d(Definition.TAG_DEBUG, "Ya asistio a la cita")
                return
            }

            if (date_hour_enter_assistance == 0L) {
                Log.d(Definition.TAG_DEBUG, "La persona todavia no ingreso a la zona de asistencia en el horario agendado")
                return
            }

            val hourExit = System.currentTimeMillis()
            val timeInAssitanceZone = (hourExit - date_hour_enter_assistance) / minuteInMillis

            if (timeInAssitanceZone < Definition.TIME_MIN_IN_ASSISTANCE_ZONE) {
                Log.d(Definition.TAG_DEBUG, "Se descarta la salida porque estuvo menos de ${Definition.TIME_MIN_IN_ASSISTANCE_ZONE} minutos")
                return
            }

            Log.d(Definition.TAG_DEBUG, "La persona asistio a la cita, estuvo mas de ${Definition.TIME_MIN_IN_ASSISTANCE_ZONE} minutos en la zona de asistencia")

            date_hour_exit_assistance = hourExit
            went_appointment = true

            withContext(Dispatchers.IO) {
                respUpdate =repositoryScheduleAssistance.updateScheduledAssitanceAndDesactivateArea(entityAssistance)
            }

            if (respUpdate == 1) {
                val msg = SharedData.MsgNotification().apply {
                    typeNotification = SharedData.TypeNotification.Alert
                    title = "Notificacion de Asistencia!"
                    message = "ha asistido a la cita de $description"
                    hour = Tools.getHour(LocalTime.now())
                    date = Tools.getDate(LocalDate.now())
                }
                NotificationManager.notifyUserPriorityBaja(msg, lat, lon)

                PlayServiceGeofenceStrategyHelper.desactivateGeofence(appContext, id_area.toString())
                Log.d(Definition.TAG_DEBUG, "Hora de salida de la cita actualizada")
            } else {
                Log.e(Definition.TAG_DEBUG, "Error no se pudo actualizar la cita")
            }
        }
    }

    private suspend fun processEnterSecurityZone(description: String, lat: String, lon: String) {
        val msg = SharedData.MsgNotification().apply {
            typeNotification = SharedData.TypeNotification.Alert
            title = "¡Alerta de Seguridad!"
            message = "ha entrado en la zona segura $description"
            hour = Tools.getHour(LocalTime.now())
            date = Tools.getDate(LocalDate.now())
        }


       RepositorySecurityZoneSPref.getInstance(appContext).saveEnteredHour(System.currentTimeMillis())


        NotificationManager.notifyUserPriorityBaja(msg, lat, lon)
    }

    private suspend fun processExitSecurityZone(
        description: String,
        minHour: String?,
        maxHour: String?,
        lat: String,
        lon: String
    ) {
        val prefs = RepositorySecurityZoneSPref.getInstance(appContext)

        //Lectura protegida
        val entryHour = prefs.getEnteredHour()
        if (entryHour == -1L) return

        val exitHour = System.currentTimeMillis()
        val durationMin = Duration.ofMillis(exitHour - entryHour).toMinutes()
        Log.d(Definition.TAG_DEBUG, "entra en processExitSecurityZone")

        if (durationMin < Definition.TIME_MIN_CIRCUMSTANTIAL_DURATION_SECURITY_ZONE) {
            Log.d(Definition.TAG_DEBUG, "No cumplio el quantum. Salida descartada. durationMin=$durationMin")
            return
        }

        val msgSMS: String = if (durationMin < Definition.TIME_MAX_CIRCUMSTANTIAL_DURATION_SECURITY_ZONE) {
            "ha salido inesperadamente de la zona segura $description"
        } else {
            val exitTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(exitHour))
            val isOutOfRange = Tools.isOutsideTimeRange(exitTime, minHour.toString(), maxHour.toString())
            if (isOutOfRange) {
                "ha salido de la zona segura $description fuera del rango horario normal"
            } else {
                "ha salido de la zona segura $description dentro del rango horario normal"
            }
        }

        val msg = NotificationManager.createMsgSecurityZone(msgSMS)
        NotificationManager.notifyUserPriorityBaja(msg, lat, lon)

        prefs.clearSharedPreferences()

        Log.d(Definition.TAG_DEBUG, msgSMS)
    }
}
