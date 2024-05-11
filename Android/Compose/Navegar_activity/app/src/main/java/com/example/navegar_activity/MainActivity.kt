package com.example.navegar_activity

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.navegar_activity.detail.DetailScreen
import com.example.navegar_activity.home.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController= rememberNavController()//Este es el controlador de navegacion

            NavHost(navController = navController, startDestination = "home" )
            {//este es el encargado de recomponer los componentes de las pantallas

                //aca defino la primer patalla
                composable(route = "home")
                {
                    //Esto dice que cuando empiece la aplicacion arranque en home y se ejceute el mainscreen
                    MainScreen(onButtonClick =
                    { //aca se define la fc callback como parametro
                        navController.navigate("detail/$it") //esto me abre la otra activity
                        //los parametros se pasan como urls. el nombre de la activity/el parametro
                        Log.d("Menu ", it)//it es el string recibido como paramtro
                    })
                }
                //creo la segunda pantalla
                composable( //la 2° activity recibe como parametro lo que lo paso la primera como url
                            //esto lo defino por si le paso como parametr algo que no sea un string
                            route = "detail/{body}",
                            arguments = listOf (
                                        navArgument("body"){
                                        type = NavType.StringType
                                        })
                            )
                {
                    val body=it.arguments?.getString("body")//obtengo el paramtro String
                    DetailScreen(body)
                }
            }


        }
    }
}

