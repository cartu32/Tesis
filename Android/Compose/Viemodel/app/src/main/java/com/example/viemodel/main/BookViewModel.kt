package com.example.viemodel.main

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/******* EL VIEMODEL ACA  SIRVE PARA MANTENER EL ESTADO DE BOOK Y ACTUALIZA EL ESTADO
 * DEL VIEWMODEL.
 * CUANDO SE CAMBIA EL VALR DE STATE DEL VIEWMODEL, AUTOMATICAMENTE SE ACTUALIZA LA VISTA
 */

//Los libros el viewmodel los consigue de una base de datos
class BookViewModel: ViewModel() {
    //genero una variable para guardar el estado de listado de books. Por eso se usa MutablesStateof
    var state by mutableStateOf(BookState())
    //al haceer mutable  con var, el books esta puede ser cambiado desde la vista
    //para que eso no pase se usa
    private set

    //para inicializar un listado de libro despues de 5 minuto uso
    init{
        viewModelScope.launch{//lanza una corutina cuando se crea el viewmodel
            //se hace copy para poder asingar a la variable isloading de bookstate su valor
            //ya que si el isloading de bookstate con var se va a poder
            //modificar desde la vista.Pero con copy solamente el viewmodel lo puede modificar
            state=state.copy(
                isLoading = true
            )

            //espera 5 segundos y crea los libros
            delay(5000)

            //se hace copy para poder asingar al listado de state los libros,
            //ya que si el listado de state lo hago mutable o con var se va a poder
            //modificar desde la vista.Pero con copy solamente el viewmodel lo puede modificar
            state=state.copy(
                books= listOf(
                    Book("Clean Code","Robert  Martin"),
                    Book("Refaxctoring","Martin Fowler"),
                    Book("Efective Java","Josha Bloch")
                ),
                isLoading = false
            )

        }

    }

    //Esta funcion recibe el libro clickeado y lo borra del listado
    //ya que la Vista puede mandar eventos  al viewmodel
    fun onBoolClicked(book:Book){
        Log.d("BookViemodel",book.title)

        //obtengo el indice del libro a borrar
        val index=state.books.indexOf(book)
        val updatedBooks=state.books.toMutableList()

        updatedBooks.removeAt(index)

        state=state.copy(
            books= updatedBooks,
            isLoading = false
        )
    }
}

/*
//****ESTA ES LA OPCION SIN EL PROGRRESS BAR*******************
//DONDE EL VIEMODEL MUESTRA EL LISTADO DESPUES DE 5 SEGUNDOS
 */
//Los libros el viewmodel los consigue de una base de datos
class BookViewModel: ViewModel() {
    //genero una variable para guardar el estado de listado de books. Por eso se usa MutablesStateof
    var books by mutableStateOf(listOf<Book>())
        //al haceer mutable  con var, el books esta puede ser cambiado desde la vista
        //para que eso no pase se usa
        private set

    //para inicializar un listado de libro despues de 5 minuto uso
    init{
        viewModelScope.launch{//lanza una corutina cuando se crea el viewmodel
            //espera 5 segundos y crea los libros
            delay(5000)
            books= listOf(
                Book("Clean Code","Robert  Martin"),
                Book("Refaxctoring","Martin Fowler"),
                Book("Efective Java","Josha Bloch")
            )

        }

    }

}*/
