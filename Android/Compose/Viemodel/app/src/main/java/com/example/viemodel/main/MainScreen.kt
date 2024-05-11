package com.example.viemodel.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier

//Esta funcion es la de la interfaz grafica
@Composable
fun MainScreen(bookViewModel: BookViewModel) {
    //generamos un listado que se actualice cuando tenemos de la bd los libros.
    //O sea despues de los 5 segundos
    val state=bookViewModel.state
    //cargamos el progresbar que se muestre si la variable isLoading es igual a true
    if(state.isLoading){
        Box(modifier=Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            CircularProgressIndicator()
        }
    }
    //aca mostramos el listados despues de 5seg
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        //que los items sea basado en el viewmodel books
        items(state.books){
            Column(modifier=Modifier.fillMaxWidth().clickable {
                bookViewModel.onBoolClicked(it)
            }){
                Text(text=it.title)
                Text(text=it.author)
                Divider()
            }
        }

    }
}

/*
//****ESTA ES LA OPCION SIN EL PROGRRESS BAR*******************
//DONDE EL VIEMODEL MUESTRA EL LISTADO DESPUES DE 5 SEGUNDOS

@Composable
fun MainScreen(bookViewModel: BookViewModel) {

    //generamos un listado que se actualice cuando tenemos de la bd los libros.
    //O sea despues de los 5 segundos
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        //que los items sea basado en el viewmodel books
        items(bookViewModel.books){
            Column(modifier=Modifier.fillMaxWidth()){
                Text(text=it.title)
                Text(text=it.author)
                Divider()
            }
        }

    }
*/