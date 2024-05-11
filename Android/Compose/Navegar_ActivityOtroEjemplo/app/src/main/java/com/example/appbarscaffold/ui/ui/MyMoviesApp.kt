package com.example.appbarscaffold.ui.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.example.appbarscaffold.ui.ui.theme.AppBarScaffoldTheme


/*************************************************************
1° Optimizazión: Crear un Composable con la base de tu APP
 **************************************************************
 */
//la funcion recibe una funcion Composable atraves del parametro content
//De esta forma vamos a poder editar Composables dentro.
//Esto funcion solo sirve para aplicar el theme, no hace nada mas
@Composable
fun MyMoviesApp(content:@Composable () -> Unit) {
    //aca aplica el theme
    AppBarScaffoldTheme{
        // A surface container using the 'background' color from the theme
        Surface(color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}