package com.example.appbarscaffold

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appbarscaffold.ui.ui.MyMoviesApp
import com.example.appbarscaffold.ui.ui.screen.detail.DetailScreen
import com.example.appbarscaffold.ui.ui.screen.main.MainScreen

class MainActivity : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            //Aplica el theme
            MyMoviesApp{
                //creamos un controlador que permite manjear la naavegacion
                //y usamos remember para que no se duplique cuando se actualiza la pantalla.
                //Asi siempre uso el mismo navcontroller
                val navController= rememberNavController()
                //Navhost define el grafo de navegacion, además de definir cual es la pantalla incial
                // y cuales son las pantallas en la que se puede navegar.
                //el destino lo llamamos main
                NavHost(navController = navController, startDestination = "main"){
                    //Navhost nos va a pintar el composable correspondiente en la pantall
                    //segun el estado en qe estemos
                    composable("main")//le decimos que la ruta de navegacion va ser main
                    {
                        // le decimos cual es composable que queremos que nos pinte cuando estamos
                        //en esa ruta de navegacion ("main"). En este caso se pintara MainScreen
                        MainScreen(navController)
                    }
                    composable(
                        route = "detail/{mediaId}",
                        arguments = listOf(
                            navArgument("mediaId") { type = NavType.IntType}) //aca les digo que tipo de argumento les voy a pasar. En este caso mediaId del tipo Int.
                    ){
                        //aca recupero recibo el parametro mediaID y se lo envio a la pantalla DetailScreen
                        backStackEntry ->
                        val id=backStackEntry.arguments?.getInt("mediaId")
                        requireNotNull(id)
                        DetailScreen(id)

                    }
                }
            }
        }
    }
}





