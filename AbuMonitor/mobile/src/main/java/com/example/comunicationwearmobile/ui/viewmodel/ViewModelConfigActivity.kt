package com.example.comunicationwearmobile.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.model.repository.RepositoryScheduleAlarmSPref
import com.example.comunicationwearmobile.ui.utils.Helpers.AlarmHelper
import com.example.comunicationwearmobile.ui.utils.Tools
import com.example.comunicationwearmobile.ui.utils.broadcast.AlarmDailyForChecksBroadcastReceiver

class ConfigViewModel(app: Application) : AndroidViewModel(app) {

    private val repo by lazy { RepositoryScheduleAlarmSPref.getInstance(app) }

    // Estado base
    private val _hour = MutableLiveData(0)
    val hour: LiveData<Int> = _hour

    private val _minute = MutableLiveData(0)
    val minute: LiveData<Int> = _minute

    private val _isChanged = MutableLiveData(false)
    val isChanged: LiveData<Boolean> = _isChanged

    private val _isSaving = MutableLiveData(false)
    val isSaving: LiveData<Boolean> = _isSaving

    // UI directa (sin Mediator): la actualizamos nosotros
    private val _timeText = MutableLiveData("--:--")
    val timeText: LiveData<String> = _timeText

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
            val millis = withContext(Dispatchers.IO) { repo.getTimeBetweenChecks() }
            val (h, m) = Tools.getHourMinOfParcial(millis)
            _hour.value = h
            _minute.value = m
            updateTimeText(h, m)
            _isChanged.value = false
            computeSaveEnabled()
        }
    }

    fun onTimePicked(h: Int, m: Int) {
        _hour.value = h
        _minute.value = m
        updateTimeText(h, m)
        _isChanged.value = true
        computeSaveEnabled()
    }

    fun save() {
        if (_isSaving.value == true) return
        viewModelScope.launch {
            _isSaving.value = true
            computeSaveEnabled()
            try {
                val h = _hour.value ?: 0
                val m = _minute.value ?: 0

                // Persistir solo si hubo cambios
                if (_isChanged.value == true) {
                    withContext(Dispatchers.IO) {
                        repo.saveTimeBetweenChecks(Tools.getTimeInMillis(h, m))
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
                    h,
                    m,
                    Definition.ACTION_ALARM_FOR_CHECKS,
                    AlarmDailyForChecksBroadcastReceiver::class.java
                )

                if (ok) {
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

    private fun updateTimeText(h: Int, m: Int) {
        _timeText.value = String.format(Locale.getDefault(), "%02d:%02d", h, m)
    }

    private fun computeSaveEnabled() {
        _saveEnabled.value = (_isChanged.value == true) && (_isSaving.value != true)
    }
}
