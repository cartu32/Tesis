package com.example.comunicationwearmobile.common

import android.content.Context
import android.widget.Toast
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


fun showToast(mcontext: Context, msg: String){
    Toast.makeText(mcontext,msg, Toast.LENGTH_SHORT).show()
}

fun dateToString(dateTime: LocalDateTime):String{
    val formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val formatterDate=dateTime.format(formatter)

    return formatterDate
}

fun getHour(dateTime: LocalDateTime):String{
    val formatter=DateTimeFormatter.ofPattern("HH:mm")
    val formatterDate=dateTime.format(formatter)

    return formatterDate
}


fun getDate(dateTime: LocalDateTime):String{
    val formatter=DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val formatterDate=dateTime.format(formatter)

    return formatterDate
}