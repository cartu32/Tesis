package com.example.comunicationwearmobile.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.repository.RepositoryScheduleAlarmSPref
import com.example.comunicationwearmobile.ui.utils.Helpers.AlarmHelper
import com.example.comunicationwearmobile.ui.utils.Tools
import com.example.comunicationwearmobile.ui.utils.broadcast.AlarmDailyForChecksBroadcastReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ConfigViewModel(app: Application) : AndroidViewModel(app) {

    // UI directa (sin Mediator): la actualizamos nosotros
    private val repo by lazy { RepositoryScheduleAlarmSPref.getInstance(app) }

    private var hourBetweenCheck:Int=0
    private var minuteBetweenCheck:Int=0

    private var hourNextAlarm:Int=0
    private var minuteNextalarm:Int=0

    private val _isChanged = MutableLiveData(false)
    val isChanged: LiveData<Boolean> = _isChanged

    private val _isSaving = MutableLiveData(false)
    val isSaving: LiveData<Boolean> = _isSaving

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

    // ------------------ Lógica ------------------

    fun loadTimeBetweenChecks() {
        viewModelScope.launch {
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
            _isChanged.value = false
            computeSaveEnabled()
        }
    }


    fun onTimePicked(h: Int, m: Int) {
        hourBetweenCheck = h
        minuteBetweenCheck = m
        updateTimeTextCheck(h, m)
        _isChanged.value = true
        computeSaveEnabled()
    }

    fun save() {
        if (_isSaving.value == true) return
        viewModelScope.launch {
            _isSaving.value = true
            computeSaveEnabled()
            try {
                val hb = hourBetweenCheck
                val mb = minuteBetweenCheck

                //calclulo el horario de la proxima alarma
                val millisBetweenCheck=Tools.getTimeInMillis(hb, mb)
                val aux=System.currentTimeMillis()+millisBetweenCheck

                val millisNextAlarm=Tools.extractHourOfDateInMillis(aux)
                val (hn,mn)=Tools.getHourMinOfParcial(millisNextAlarm)

                // Persistir solo si hubo cambios
                if (_isChanged.value == true) {

                    withContext(Dispatchers.IO) {
                        repo.saveTimeBetweenChecks(millisBetweenCheck)
                        repo.saveTimeNextAlarm(millisNextAlarm)
                    }
                }
                //cancelo la alarma previa
                AlarmHelper().cancelAlarm(
                    getApplication(),
                    Definition.ALARM_ID_BETWEEN_CHECKS,
                    Definition.ACTION_ALARM_FOR_CHECKS,
                    AlarmDailyForChecksBroadcastReceiver::class.java
                )

                // Programar alarma
                val ok = AlarmHelper().setAlarmAfterOfTime(
                    getApplication(),
                    Definition.ALARM_ID_BETWEEN_CHECKS,
                    hb,
                    mb,
                    Definition.ACTION_ALARM_FOR_CHECKS,
                    AlarmDailyForChecksBroadcastReceiver::class.java
                )

                if (ok) {
                    updateTimeNextAlarm(hn,mn)
                    _toastMessage.value = "Alarma de checkeo configurada correctamente"
                    _finishEvent.value = true
                } else {
                    _toastMessage.value = "No se pudo configurar la alarma"
                }
            } catch (t: Throwable) {
                _toastMessage.value = "Error al guardar: ${t.message ?: "desconocido"}"
            } finally {
                _isSaving.value = false
                computeSaveEnabled()
            }
        }
    }

    fun onToastShown() { _toastMessage.value = null }
    fun onFinishConsumed() { _finishEvent.value = false }

    // ------------------ Helpers ------------------

    private fun updateTimeTextCheck(h: Int, m: Int) {
        _timeTextCheck.value = String.format(Locale.getDefault(), "%02d:%02d", h, m)
    }

    private fun updateTimeNextAlarm(h: Int, m: Int) {
        _timeTextNextAlarm.value = String.format(Locale.getDefault(), "%02d:%02d", h, m)
    }
    private fun computeSaveEnabled() {
        _saveEnabled.value = (_isChanged.value == true) && (_isSaving.value != true)
    }
}
