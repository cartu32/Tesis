package com.example.datepicker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.datepicker.ui.theme.DatePickerTheme
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()

        }
    }


    @Preview(showBackground = true)
    @Composable
    fun MainScreen(){
        val state= rememberDatePickerState()
        var showDialog by remember {mutableStateOf(false)}

        Column(modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Button(onClick = { showDialog = true })
            {
                Text(text = "Mostra Fechas")
            }

            if (showDialog) {
                DatePickerDialog(
                    onDismissRequest =
                    {
                        showDialog = false
                    },//que se ejecuta cuando se presiona la x
                    confirmButton =
                    { //Como confirmButton es un composable se debe definir el button y que se hace cuando se presioan guardar
                        Button(onClick = { showDialog = false })
                        {
                            Text(text = "Cofirmar")
                        }
                    },
                    dismissButton =
                    { //Como confirmButton es un composable se debe definir el button y que se hace cuando se presioan guardar
                        OutlinedButton(onClick = { showDialog = false })
                        {
                            Text(text = "Cancelar")
                        }
                    }

                )
                {   //mostramos el datapicker
                    DatePicker(state = state)
                }

            }
            val date=state.selectedDateMillis
            date?.let {
                val instant = Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate()
                Text(text ="Fecha Seleccionada: $instant")
            }
        }

    }
}

