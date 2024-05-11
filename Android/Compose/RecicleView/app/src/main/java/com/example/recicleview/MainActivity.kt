package com.example.recicleview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.recicleview.ui.theme.RecicleViewTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            //MainScreenSinLazyColumn()
            MainScreenConLazyColumn()
        }
    }
}


@Composable
fun MainScreenSinLazyColumn() {
    //El listado se corta en 40 y no es scrollable
    val nombres =(0..200).map {"$it-martin"}
    Column{
        for(nombre in nombres){
            Text(text = nombre)
        }
    }



}

@Composable
fun MainScreenConLazyColumn(){
    val nombres =(0..200).map {"$it-martin"}

    //Para mostrar listados infinitos se usa lazycolumn o lazyRow

    LazyColumn(modifier = Modifier.fillMaxSize())
    {
        item(){//item permite poner el encabezado al listado scrollable.Si se pone al final del listado item Tambien puede usarse como pie del listado
            Text(text="Nombre", color= Color.Blue, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        }
      /*  items(nombres){nombre-> // Esto serian los items que se repiten
            Text(text = nombre )
        }*/
        itemsIndexed(nombres){ index, nombre->
            if(index%2==0) //Si es par
                Text(text=nombre, color= Color.Red)
            else //Si es impar
                Text(text=nombre, color= Color.Green)
        }
    }

}

@Preview(showBackground = true)
@Composable
fun Preview(){
 //   MainScreenSinLazyColumn()
    MainScreenConLazyColumn()
}
