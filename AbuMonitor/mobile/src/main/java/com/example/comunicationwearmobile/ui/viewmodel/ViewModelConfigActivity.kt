package com.example.comunicationwearmobile.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.common.SharedVariables
import com.example.comunicationwearmobile.ui.model.repository.RepositoryConfigAppSPref
import com.example.comunicationwearmobile.ui.utils.Helpers.Alarm.AlarmHelper
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.GeofenceScheduleHelper
import com.example.comunicationwearmobile.ui.utils.Tools
import com.example.comunicationwearmobile.ui.utils.broadcast.AlarmBroadcastReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

data class dataNextAlarm(
    var hb:Int=0,
    var mb:Int=0,
    var hn:Int=0,
    var mn:Int=0,
    var millisBetweenCheck:Long=0,
    var millisNextAlarm:Long=0
)

data class dataTimeReminder(
    var hb:Int=0,
    var mb:Int=0,
    var millisRemember:Long=0
)
class ConfigViewModel(app: Application) : AndroidViewModel(app) {

    // UI directa (sin Mediator): la actualizamos nosotros
    private val repo by lazy { RepositoryConfigAppSPref.getInstance(app) }
    private var geofenceScheduleHelper= GeofenceScheduleHelper(app.applicationContext)
    private var hourBetweenCheck:Int=0
    private var minuteBetweenCheck:Int=0

    private var hourNextAlarm:Int=0
    private var minuteNextalarm:Int=0

    private var hourRemember:Int=0
    private var minuteRemember:Int=0

    private val _nameUser = MutableLiveData<String?>()
    val nameUser: LiveData<String?> = _nameUser

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
    private var isChangeNameUser=false

    private var updatingProgrammatically=false
    // ------------------ Lógica ------------------

    fun loadConfiguration(){
        viewModelScope.launch {
            loadTimeBetweenChecks()
            loadTimeRememberAppointment()
            loadNameUser()
        }
    }

    suspend fun loadNameUser() {
        val name = withContext(Dispatchers.IO){repo.getNameUser()}
        isChangeNameUser=false

        updatingProgrammatically =true
        _nameUser.value=name
        updatingProgrammatically =false

    }

    suspend fun loadTimeRememberAppointment() {
        val hourRemeber= withContext(Dispatchers.IO){repo.getTimeRememberAppointment()}
        val (hr,mr)=Tools.getHourMinOfParcial(hourRemeber)

        hourRemember=hr
        minuteRemember=mr
        updateTimeTextRemember(hr,mr)

        isChangedRemeber=false
        computeSaveEnabled()
    }

    private suspend fun getTimeNextAlarm(): Long {
        //pregunto si es la primera vez que se abre la app
        if(SharedVariables.isOpenAppFirsTime){
            SharedVariables.isOpenAppFirsTime=false
            return SharedVariables.timeAlarmChecksFirstTime
        }else
            return repo.getTimeNextAlarm()
    }

    private suspend fun getTimeBetweenChecks():Long{
        return repo.getTimeBetweenChecks()
    }

    private suspend  fun loadTimeBetweenChecks() {
        val millisBetweenCheck = withContext(Dispatchers.IO) { getTimeBetweenChecks() }
        val millisNextAlarm = withContext(Dispatchers.IO) { getTimeNextAlarm() }

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
        hourRemember    = h
        minuteRemember  = m
        updateTimeTextRemember(h,m)
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

    fun save(nameUser:String) {
        viewModelScope.launch {
            // Si no hay cambios, no hacemos nada
            if (!isChangedBetween && !isChangedRemeber && !isChangeNameUser)
                return@launch

            var allOk = true

            val dataNextAlarm=calculateDataNextAlarm()
            val dataTimeReminder=calculateTimeReminder()

            if(nameUser.isEmpty()) {
                _toastMessage.value = "Por favor ingrese un nombre de usuario"
                return@launch
            }
            if (dataNextAlarm.millisBetweenCheck>dataTimeReminder.millisRemember){
                _toastMessage.value = "Por favor elija un tiempo mayor al tiempo de chequeo para recordar"
                return@launch
            }

            if (isChangedRemeber) {
                allOk = allOk && saveTimeRememberInternal(dataTimeReminder)
            }

            if(isChangeNameUser){
                allOk = allOk && saveNameUserInternal(nameUser)
            }

            // Recalcula el estado del botón guardar, etc.
            computeSaveEnabled()

            // Solo disparo el finish si TODO salió bien
            if (allOk) {
                _toastMessage.value = "Configuracion guardada correctamente"
                _finishEvent.value = true
            }
        }
    }

    suspend fun saveNameUserInternal(nameUser: String): Boolean {
        return try {

            withContext(Dispatchers.IO) {
                repo.saveNameUser(nameUser)
            }
            true
        } catch (t: Throwable) {
            _toastMessage.value = "Error al guardar: ${t.message ?: "desconocido"}"
            false
        }
    }

    private fun calculateTimeReminder(): dataTimeReminder {
        val dataTimeReminder=dataTimeReminder()

        with(dataTimeReminder){
            hb = hourRemember
            mb = minuteRemember

            millisRemember = Tools.getTimeInMillis(hb, mb)

        }
        return dataTimeReminder
    }

    private suspend fun saveTimeRememberInternal(dataTimeReminder: dataTimeReminder): Boolean {
        return try {

            withContext(Dispatchers.IO) {
                repo.saveTimeRememberAppointment(dataTimeReminder.millisRemember)
            }

            updateTimeTextRemember(dataTimeReminder.hb, dataTimeReminder.mb)
            true
        } catch (t: Throwable) {
            _toastMessage.value = "Error al guardar: ${t.message ?: "desconocido"}"
            false
        }
    }

    private fun calculateDataNextAlarm():dataNextAlarm{
        var dataNextAlarm=dataNextAlarm()

        with(dataNextAlarm) {
            hb = hourBetweenCheck
            mb = minuteBetweenCheck

            // cálculo del intervalo entre chequeos
            millisBetweenCheck = Tools.getTimeInMillis(hb, mb)
            val aux = System.currentTimeMillis() + millisBetweenCheck

            millisNextAlarm = Tools.extractHourOfDateInMillis(aux)
            val (hnAux, mnAux) = Tools.getHourMinOfParcial(millisNextAlarm)
            hn=hnAux
            mn=mnAux
        }
        return dataNextAlarm
    }

    fun onToastShown() { _toastMessage.value = null }
    fun onFinishConsumed() { _finishEvent.value = false }

    // ------------------ Helpers ------------------

    private fun updateTimeTextCheck(h: Int, m: Int) {
        //_timeTextCheck.value = String.format(Locale.getDefault(), "%02d:%02d", h, m)
        _timeTextCheck.value = String.format(Locale.getDefault(), "%d horas y %d minutos", h, m)
    }

    private fun updateTimeTextRemember(h: Int, m: Int) {
        _timeTextRemember.value = String.format(Locale.getDefault(), "%d horas y %d minutos", h, m)
    }

    private fun updateTimeNextAlarm(h: Int, m: Int) {
        _timeTextNextAlarm.value = String.format(Locale.getDefault(), "%02d:%02d", h, m)
    }
    private fun computeSaveEnabled() {
        _saveEnabled.value = (isChangeNameUser == true) ||
                             (isChangedBetween == true) ||
                             (isChangedRemeber == true)
    }

    fun markAsChangedNameUser() {
        if(!updatingProgrammatically) {
            isChangeNameUser = true
            computeSaveEnabled()
        }
    }


}
