package com.example.estadoscomposeotroejemplo

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.NumberPicker.OnValueChangeListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.estadoscomposeotroejemplo.ui.theme.EstadosComposeOtroEjemploTheme
import kotlin.math.ceil

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            //********En Esta codigo se guarda el estado sin hacerlo generico
           // StateSample()

            //****En est codigo se guarda el estado de genericamente para poder usarlo,

            //aca se saca el state afuera. Como remeber devuelve un get y setter, entonces el resultado del getter
            //lo asigno a value. Mientrasque el setter se lo asigno a onValuechanged. Esto se hace en (value, onValueChanged)
            val (value,onValuChanged) = rememberSaveable {mutableStateOf("") }
            //se pasan como parametro value y onValuechanged
            StateSampleOptimizado(value=value , onValueChange=onValuChanged )
        }

    }
}

@Preview(showBackground = true)
@Composable
fun StateSample(){
    //Compose cada vez que hay un cambio en la interfaz, vuelve a graficar el componente modificado.
    //Por lo que al volver a graficarse se va a perder su estoado( valor escrito por el usuario).
    //De esta forma no se va aver lo que escribe el usuario, ya que se vuelve a repintar.
    //Para que esto no suceda hay que indicarle a compose que estado cambio en cada recomposicion, para eso
    //se usar mutablestateof. Además para no perder el valor de la variable en cada recomposicionse debe usar
    //remember.
    // el by y el var es para obtener el valor del getter y setter

    //var text by remember {mutableStateOf("") }

    // en la linea anterior se usa var porque text cambia de valor segun el getter
    //sino seria con val e =

    // val text = remember {mutableStateOf("") }

    //rememberSaveable guarda el text cuando se rota la pantalla
    var text by rememberSaveable {mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(64.dp),
        verticalArrangement = Arrangement.Center
    ){
        TextField(
            value = text,
            onValueChange = { text = it },
            Modifier
                .background(Color.Gray)
                .fillMaxWidth()

        )
        Text(
            text = text,
            Modifier
                .background(Color.Yellow)
                .fillMaxWidth()
                .padding(8.dp)
        )
        
        Button(
            onClick = { text = " " },
            enabled = text.isNotEmpty(),
            modifier = Modifier.align(Alignment.CenterHorizontally)

        ) {
                Text(text = "Clear")
            
        }
    }
}





@Composable
//Como TextField debe recibir como parametro un Stirng y n onvaluechanged, como fc lambda.
//se envia estos como parametros.
fun StateSampleOptimizado(value: String,onValueChange: (String) -> Unit){


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(64.dp),
        verticalArrangement = Arrangement.Center
    ){
        TextField(
            value = value,
            onValueChange = { onValueChange(it) },
            Modifier
                .background(Color.Gray)
                .fillMaxWidth()

        )
        Text(
            text = value,
            Modifier
                .background(Color.Yellow)
                .fillMaxWidth()
                .padding(8.dp)
        )

        Button(
            onClick = { onValueChange( " " ) },
            enabled = value.isNotEmpty(),
            modifier = Modifier.align(Alignment.CenterHorizontally)

        ) {
            Text(text = "Clear")

        }
    }
}