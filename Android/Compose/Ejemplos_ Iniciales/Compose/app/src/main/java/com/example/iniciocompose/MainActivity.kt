package com.example.iniciocompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.iniciocompose.ui.theme.InicioComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InicioComposeTheme {
                DrawUI()
            }
        }
    }
}




//No se puede pasar parametros a una funcion defindia con preview. Las unica forma es hacer una
//funcion intermedia
@Preview(showBackground = true, name ="text3")
@Composable
fun DrawUI(){
    Surface {
        ExampleComposable("ramon")
    }
}

@Composable
fun ExampleComposable(name:String){
    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
            .background(Color.Yellow)
    ) {
        Text(
            text = "Hello,",
            color= Color.Red,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif

            )
        Text(text = name)
    }

}