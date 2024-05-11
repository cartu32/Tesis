package com.example.estadoscompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.estadoscompose.ui.theme.EstadosComposeTheme
import com.example.estadoscompose.ui.theme.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            //MainScreenSinRemember()
            //MainScreenConRemember()
            MainScreenConRememberViewModel(MainViewModel())
        }
    }
}

/* En este ejemplo por más que defina en el boton que se cambie de nombre a juan y lo muestre en el textbox,
  Este va a seguir mostrando Pedro. Esto pasa que cuando presiono el boton, el componenete se vuelve a recomponer.
  Por lo que vuelve a tener el valor Pedro.
 */
@Composable
fun MainScreenSinRemember(){
    var nombre= "Pedro"
    Column{
        Text(text = nombre)
        Button(onClick = {nombre="Juan"}){

        }

    }
}

/*Para que no suceda lo anterior se debe usar la instruccion Remember.
Esta se acuerda del estado del componente antes de hacer la recomposicion.
Enntonces en este caso el estado se crea con Pedro, pero luego al apretar el boton
se cambia a Juan. Pero al momento de recomponerse se acuerda de que se llama Juan
y no se reinicia
 */
@Composable
fun MainScreenConRemember() {
    var nombre by remember {
        mutableStateOf("Pedro")
    }

    /*************Codigo Ejemplo sin ViewModel******************
    la fc Onclick de Displayname se define entre la llaves
    ******************************************************/
    DisplayName(nombre){
        nombre="Juan"

    }

}
/*
lo ideal es que mainScreen cambie el nombre no DisplayName, ya que despues eso lo hace mas adelante un viewmodel
Por eso se pasa como parametro una fc Onclick
 */
@Composable
fun DisplayName(nombre:String,
                onClick:()->Unit) {//al recibir como ultimo parametro una fc, no hace falta pasarla cuando llamamos a la fc DisplayName
    Column{
        Text(text = nombre)
        Button(onClick = {onClick()}){//al presionar a el Boton se llama a la fc Onclick, como si fuera un callback

        }

    }
}

/*
Conel viewmodel creo como un callback, en donde el estado solo lo puede modificar el viewmodel y
se le informa a los suscriptores sus cambios
 */
@Composable
fun MainScreenConRememberViewModel(viewModel: MainViewModel) {

   /*************Codigo Ejemplo con  ViewModel******************
    la fc Onclick de Displayname se define entre la llaves
     ******************************************************/
    DisplayName(nombre=viewModel.state){// en esta liena se suscribe a los cambios del estado
        viewModel.updataName()//aca se realiza el cambio del estado invocando al viewmodel

    }

}