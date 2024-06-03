package com.example.comunicationwearmobile.common

import android.content.Context
import android.widget.Toast


fun showToast(mcontext: Context, msg: String){
    Toast.makeText(mcontext,msg, Toast.LENGTH_SHORT).show()
}