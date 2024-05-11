package com.example.appbarscaffold.ui.ui.screen.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/*************************************************************
3° Optimizazión: Crea Composables que definan una panatalla
 **************************************************************
 */

@Composable
fun MainScreen() {
    //Crea la appBar. Osea la barra menu
    Scaffold(topBar= { MainAppBar() })
    { padding ->
        //Crea el listado de las fotos, el listview con lazyGrid
        MediaList(modifier = Modifier.padding(padding))
    }
}