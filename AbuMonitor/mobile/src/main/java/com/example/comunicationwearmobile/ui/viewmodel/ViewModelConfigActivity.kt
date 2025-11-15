package com.example.comunicationwearmobile.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.repository.RepositoryScheduleAlarmSPref
import com.example.comunicationwearmobile.ui.utils.Helpers.AlarmHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.GeofenceScheduleHelper
import com.example.comunicationwearmobile.ui.utils.Tools
import com.example.comunicationwearmobile.ui.utils.broadcast.AlarmDailyForChecksBroadcastReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ConfigViewModel(app: Application) : AndroidViewModel(app) {

    // UI directa (sin Mediator): la actualizamos nosotros
    private val repo by lazy { RepositoryScheduleAlarmSPref.getInstance(app) }
    private var geofenceScheduleHelper=GeofenceScheduleHelper(app.applicationContext)
    private var hourBetweenCheck:Int=0
    private var minuteBetweenCheck:Int=0

    private var hourNextAlarm:Int=0
    private var minuteNextalarm:Int=0

    private var hourRemember:Int=0

    private val _timeTextRemember = MutableLiveData("--")
    val timeTextRemember: LiveData<String> = _timeTextRemember

    // UI directa (sin Mediator): la actualizamos nosotros
    private val _timeTextCheck = MutableLiveData("--:--")
    val timeTextCheck: LiveData<String> = _timeTextCheck

    private val _timeTextNextAlarm = MutableLiveData("--:--")
    val timeTextNextAlarm: LiveData<String> = _timeTextNextAlarm

    private val _saveEnabled = MutableLiveData(false)
    val saveEnabled: LiveData<Boolean> = _saveEnabled

    // Eventos simples
    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    private val _finishEvent = MutableLiveData<Boolean>(false)
    val finishEvent: LiveData<Boolean> = _finishEvent

    private var isChangedBetween=false
    private var isChangedRemeber=false
    // ------------------ Lógica ------------------

    fun loadConfiguration(){
        viewModelScope.launch {
            loadTimeBetweenChecks()
            loadTimeRememberAppointment()
        }
    }

    suspend fun loadTimeRememberAppointment() {
        val hourRemeber= withContext(Dispatchers.IO){repo.getTimeRememberAppointment()}
        val (h,m)=Tools.getHourMinOfParcial(hourRemeber)

        hourRemember=h

        updateTimeTextRemember(h)
        isChangedRemeber=false
        computeSaveEnabled()
    }

    suspend  fun loadTimeBetweenChecks() {
        val millisBetweenCheck = withContext(Dispatchers.IO) { repo.getTimeBetweenChecks() }
        val millisNextAlarm = withContext(Dispatchers.IO) { repo.getTimeNextAlarm() }

        val (hb, mb) = Tools.getHourMinOfParcial(millisBetweenCheck)
        val (hn, mn) = Tools.getHourMinOfParcial(millisNextAlarm)

        hourBetweenCheck = hb
        minuteBetweenCheck = mb
        hourNextAlarm = hn
        minuteNextalarm = mn

        updateTimeTextCheck(hourBetweenCheck, minuteBetweenCheck)
        updateTimeNextAlarm(hourNextAlarm, minuteNextalarm)
        isChangedBetween= false
        computeSaveEnabled()
    }

    fun onTimePickedRemember(h: Int, m: Int) {
        hourRemember = h
        updateTimeTextRemember(h)
        isChangedRemeber = true
        computeSaveEnabled()
    }

    fun onTimePickedBetween(h: Int, m: Int) {
        hourBetweenCheck = h
        minuteBetweenCheck = m
        updateTimeTextCheck(h, m)
        isChangedBetween = true
        computeSaveEnabled()
    }

    fun save() {
        viewModelScope.launch {
            // Si no hay cambios, no hacemos nada
            if (!isChangedBetween && !isChangedRemeber) return@launch

            var allOk = true

            if (isChangedBetween) {
                allOk = allOk && saveAlarmBetweenCheckInternal()
            }

            if (isChangedRemeber) {
                allOk = allOk && saveTimeRememberInternal()
            }

            // Recalcula el estado del botón guardar, etc.
            computeSaveEnabled()

            // Solo disparo el finish si TODO salió bien
            if (allOk) {
                _finishEvent.value = true
            }
        }
    }

    private suspend fun saveTimeRememberInternal(): Boolean {
        return try {
            val hb = hourRemember
            val mb = 0

            val millisRemember = Tools.getTimeInMillis(hb, mb)

            withContext(Dispatchers.IO) {
                repo.saveTimeRememberAppointment(millisRemember)
            }

            updateTimeTextRemember(hourRemember)
            true
        } catch (t: Throwable) {
            _toastMessage.value = "Error al guardar: ${t.message ?: "desconocido"}"
            false
        }
    }


    private suspend fun saveAlarmBetweenCheckInternal(): Boolean {
        return try {
            val hb = hourBetweenCheck
            val mb = minuteBetweenCheck

            // cálculo del intervalo entre chequeos
            val millisBetweenCheck = Tools.getTimeInMillis(hb, mb)
            val aux = System.currentTimeMillis() + millisBetweenCheck

            val millisNextAlarm = Tools.extractHourOfDateInMillis(aux)
            val (hn, mn) = Tools.getHourMinOfParcial(millisNextAlarm)

            // guardo configuración en SharedPreferences
            withContext(Dispatchers.IO) {
                repo.saveTimeBetweenChecks(millisBetweenCheck)
                repo.saveTimeNextAlarm(millisNextAlarm)
            }

            // cancelo alarma previa
            AlarmHelper().cancelAlarm(
                getApplication(),
                Definition.ALARM_ID_BETWEEN_CHECKS,
                Definition.ACTION_ALARM_FOR_CHECKS,
                AlarmDailyForChecksBroadcastReceiver::class.java
            )

            // programo nueva alarma
            val okAlarm = AlarmHelper().setAlarmAfterOfTime(
                getApplication(),
                Definition.ALARM_ID_BETWEEN_CHECKS,
                hb,
                mb,
                Definition.ACTION_ALARM_FOR_CHECKS,
                AlarmDailyForChecksBroadcastReceiver::class.java
            )

            if (okAlarm) {
                // activo/desactivo geofences según la nueva ventana
                geofenceScheduleHelper.activateAndDesactivateGeofenceScheduled()

                // actualizo en la vista el horario de la próxima alarma
                updateTimeNextAlarm(hn, mn)
                _toastMessage.value = "Alarma de checkeo configurada correctamente"
                true
            } else {
                _toastMessage.value = "No se pudo configurar la alarma"
                false
            }

        } catch (t: Throwable) {
            _toastMessage.value = "Error al guardar: ${t.message ?: "desconocido"}"
            false
        }
    }

    fun onToastShown() { _toastMessage.value = null }
    fun onFinishConsumed() { _finishEvent.value = false }

    // ------------------ Helpers ------------------

    private fun updateTimeTextCheck(h: Int, m: Int) {
        _timeTextCheck.value = String.format(Locale.getDefault(), "%02d:%02d", h, m)
    }

    private fun updateTimeTextRemember(h: Int) {
        _timeTextRemember.value = String.format(Locale.getDefault(), "%02d", h)
    }

    private fun updateTimeNextAlarm(h: Int, m: Int) {
        _timeTextNextAlarm.value = String.format(Locale.getDefault(), "%02d:%02d", h, m)
    }
    private fun computeSaveEnabled() {
        _saveEnabled.value = (isChangedBetween == true) || (isChangedRemeber==true)
    }


}
