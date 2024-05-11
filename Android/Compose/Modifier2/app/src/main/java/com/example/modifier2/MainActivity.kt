package com.example.modifier2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.modifier2.ui.theme.Modifier2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
                ButtonText()
            }
        }

    @Preview(showBackground = true, widthDp = 400, heightDp = 200)
    @Composable
    private fun ButtonText()
    {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            Text(text = "Hello world", modifier = Modifier
                    //OJO los modiifer se aplican de arriba para abajo.
                    .clickable {  }
                    .background(color = Color.Cyan)//Entonces pimero se aplica el fondo Cian.
                    .border(width=2.dp,color=Color.Blue) //Luego a ese fondo se aplica a ese fondo el borde azul.Por lo que si aplico el border lineas mas abajo se va a aplicar mal
                    .padding(horizontal = 16.dp, vertical = 8.dp)//Luego a ese borde azul se aplica padding

                    .background(color = Color.LightGray)//Luego al padding anterior se aplica el fondo gris
                    .border(width=2.dp,color=Color.Red) //Luego al fondo gris se aplica el bordo rojo
                    .padding(8.dp)//Finalmanete al borde rojo se aplica el padding
            )
        }
    }
}

