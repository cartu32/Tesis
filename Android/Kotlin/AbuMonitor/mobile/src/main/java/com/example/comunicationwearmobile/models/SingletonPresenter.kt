package com.example.comunicationwearmobile.models

class SingletonPresenter  {

    companion object {


        @Volatile private var instance: SingletonPresenter? = null // Volatile modifier is necessary
        private var
        init{

        }

        fun getInstance() =
            instance ?: synchronized(this) { // synchronized to avoid concurrency problem
                instance ?: SingletonPresenter().also { instance = it }
            }
    }
}