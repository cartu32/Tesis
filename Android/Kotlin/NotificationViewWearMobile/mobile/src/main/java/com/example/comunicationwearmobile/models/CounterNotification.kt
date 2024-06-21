package com.example.comunicationwearmobile.models

class CounterNotification {
    companion object {
        var idNotification: Int = 0
            private set  // Esta línea hace que la variable sea de solo lectura fuera del companion object


        fun incrementCounter() {
            idNotification++
        }

        fun getCounter(): Int {
            return idNotification
        }
    }
}
