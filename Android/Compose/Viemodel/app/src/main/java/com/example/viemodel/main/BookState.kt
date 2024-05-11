package com.example.viemodel.main

data class BookState (
    val books: List<Book> = emptyList(), // books es una lista  inmutable
    val isLoading: Boolean= false //isLoading es inmutable
)