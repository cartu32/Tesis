package com.example.composecolumn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun App(){
    /* cremos una variable que almacene el estado mutable, incializada en 0.
        Con Remember se alamacena el valor de esta, si se cierra la activity
     */
    var counter by rememberSaveable{ mutableStateOf(0) }


    /*LazyColumn permite cargar las cosas a medida que se van mostrando en pantalla.
    Esto nos permite hacer un Scrollview agredando items. En compose no existe scrollview,
    entonces con LazyColumn lo simulamos
     */
    LazyColumn(
        modifier= Modifier
            .fillMaxSize()
            .background(Color.Red)
            .padding(bottom = 16.dp)
    ){
       item{
            Image(
                 modifier = Modifier
                     .fillMaxWidth()
                     .height(200.dp),
                 painter= painterResource(id = R.drawable.tux),
                 contentDescription = "logo tux"
                 )
            Row(modifier=Modifier.padding(top= 10.dp, start=10.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.icon_fav) ,
                    contentDescription = "dale like",
                    modifier=Modifier.clickable { counter++ }
                    )
                Text(
                    text = counter.toString(),
                    color= Color.White,
                    modifier=Modifier.padding(start= 10.dp)
                )

            }
            Text(
                 text="Perfil",
                 fontSize = 32.sp,
                 color=Color.White,
                 modifier = Modifier.fillMaxWidth(),
                 textAlign = TextAlign.Center
                 )
            Text(
                 text="Esteban Carnuccio",
                 color=Color.White,
                 modifier=Modifier.padding(top=10.dp,start=10.dp)

                )
            Text(
                text="Docente Universitario",
                color=Color.White,
                modifier=Modifier.padding(start=10.dp)
            )

           LazyRow(
               horizontalArrangement = Arrangement.SpaceBetween,
               modifier = Modifier
                   .fillMaxWidth()
                   .padding(start = 10.dp)
           ) {
               item() {
                   Text(
                       text = "Java",
                       color = Color.White,
                       modifier= Modifier
                           .fillMaxWidth()
                           .padding(end = 10.dp)
                   )
                   Text(
                       text = "C++",
                       color = Color.White,
                       modifier= Modifier
                           .fillMaxWidth()
                           .padding(end = 10.dp)
                   )
                   Text(
                       text = "Python",
                       color = Color.White,
                       modifier= Modifier
                           .fillMaxWidth()
                           .padding(end = 10.dp)
                   )
                   Text(
                       text = "Node.js",
                       color = Color.White,
                       modifier= Modifier
                           .fillMaxWidth()
                           .padding(end = 10.dp)
                   )

               }
           }

        }
    }
}