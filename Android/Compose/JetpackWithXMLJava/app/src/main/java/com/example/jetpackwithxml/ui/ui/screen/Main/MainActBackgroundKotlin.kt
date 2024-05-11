package com.example.jetpackwithxml.ui.ui.screen.Main

import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.jetpackwithxml.R

class MainActBackgroundKotlin {
    companion object{
        fun Background(view: View) {
            var cantClick=0
            val txtTexto =view.findViewById<TextView>(R.id.txtTexto)
            val cmdButton =view.findViewById<Button>(R.id.cmdButton)

            cmdButton.setOnClickListener {
                cantClick++;
                listnerButton(txtTexto,cantClick)
            }

        }

        fun listnerButton(txtTexto: TextView, cantClick: Int) {
            txtTexto.text= "Cant Pulsacion Kotlin:° $cantClick"
        }

    }
}