package com.example.modifiers

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.modifiers.ui.theme.ModifiersTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
                MainScreen()
            }
        }
}

@Preview(showBackground = true)
@Composable
fun MainScreen()
{
    Column {
        ShowText1()
        ShowText2()
        ShowText3()
    }

}

fun ShowToast(mContex: Context, msg:String){
    Toast.makeText(mContex,msg,Toast.LENGTH_LONG).show()
}

/*En este ejemplo se hace clickable solo el texto. Para eso se pone al final de text. clickable.
Esto se debe a que los modifiers se aplican a partir del ultimo hasta el primero.O sea
-Primero aplica el clickable
-Luego el padding(15)
-Luego  el .background(Color.Green)
-Luego padding (30.dp)
-Luego .background(Color.Cyan)

 De esta manera solo el clickable el texto, ya que lo demás se aplican despeus
 */
@Composable
fun ShowText2() {
    val mContext= LocalContext.current
    Text(text="Hola como estas", modifier= Modifier
        .background(Color.Cyan)
        .padding(30.dp)
        .background(Color.Green)
        .padding(15.dp)
        .clickable {
            ShowToast(mContext, "Presiono boton1")
        })
}

/*
Aca como clickavble es lo ultimo que se aplica de modiifiers, entonces se aplica a todo lo demás
incluyendo a los background y padding.
 */
@Composable
fun ShowText3() {
    val mContext= LocalContext.current
    Text(text="Hola como estas", modifier= Modifier
        .clickable {
            ShowToast(mContext, "Presiono boton2")
        }
        .background(Color.Cyan)
        .padding(30.dp)
        .background(Color.Green)
        .padding(15.dp)
    )

}
/*
en esta funcion se enviar como parametro un modifier como parametro dentro.
Este se deben enviar desde el main. Si no se envia, este se definira como vacio
y se creara uno de la nada dentro de la funcion.
Esto se hace como buena práctica, por si quiero importar las funciones y componentes como bibliotecas.
 */
@Composable
fun ShowText1(modifier: Modifier=Modifier) {
    Text(text = "Presioname", modifier = modifier
            .clickable {
                Log.i("Mainscreen", "Presiona texto")
            }
            .padding(30.dp)
            .alpha(0.5f)
    )
}

