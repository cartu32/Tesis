package com.example.comunicationwearmobile.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.comunicationwearmobile.common.TypeMsg
import com.example.comunicationwearmobile.models.MsgAlertModel
import com.example.comunicationwearmobile.models.MsgAlertState
import kotlinx.coroutines.launch

/*
Los viewmodels se usan para mantener el estado de las variables. Esto sirve mas que nada para cuando
se gira la pantalla, o no se quiere guardar el estado de las mismas sin la necesidad de usar un
objeto bundle
 */
class AlertsViewModel:ViewModel() {
    var state by mutableStateOf(MsgAlertState())
    private set

    init {
        viewModelScope.launch {
            state=state.copy(
                alertsList = listOf(
                    MsgAlertModel("Titulo Prueba","Cuerpo Prueba",TypeMsg.Reminder),
                    MsgAlertModel("Titulo Prueba","Cuerpo Prueba",TypeMsg.Alert),
                )
            )
        }
    }
    fun onBoolClcicked(msgAlert:MsgAlertModel) {

        val index= state.alertsList.indexOf(msgAlert)
        val updateAlert=state.alertsList.toMutableList()

        updateAlert.removeAt(index)
        state=state.copy(
            alertsList = updateAlert)
    }
}
