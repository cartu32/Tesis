package com.example.iniciocompose

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            MainScreen()
            MainScreen2()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreen(){
    Column {
        ShowTexto()
        ShowButton()
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreen2(){
  Row(modifier=Modifier.fillMaxSize(),
      verticalAlignment = Alignment.CenterVertically,  //me alinea los objetos en una parte de la pantalla
      horizontalArrangement = Arrangement.SpaceAround ) // me muestra los componentes espcaidado
  {
      Text(text = "Post")
      Text(text = "Followers")
      Text(text = "Following")
  }
}


fun showToast(mcontext: Context, msg: String){
    Toast.makeText(mcontext,msg,Toast.LENGTH_SHORT).show()
}

@Composable
fun ShowButton() {
    val mContext = LocalContext.current

    Button(onClick = {
        // Obtén el contexto actual
        showToast(mContext,"Presiono boton")
        Log.i("Mainscreen", "apretame fue presionado")
    }) {
        Column {
            Text(text = "apretame")
            Text(text = "por favor ")
        }
    }
}

@Composable
fun ShowTexto(){
    Text(
        text = "Esteban",
        color = Color.Blue,
        fontSize = 24.sp,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Cursive, //Tiene algunas se debe configurar en otro lado
        overflow = TextOverflow.Ellipsis, //si no entra el texto en el textvien, Hace que al final se ponga los ...,
        style = MaterialTheme.typography.bodyLarge

    )
}

@Composable
fun Preview(){

}