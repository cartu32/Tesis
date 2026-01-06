package com.example.comunicationwearmobile.ui.view.adapter;

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.view.setPadding

class SpinnerSimpleAdapter(mContext: Context, resource: Int,items : Array<String>)
    :ArrayAdapter<String>(mContext, resource, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        (view as TextView).setTextColor(Color.BLACK) // Cambia el color del texto seleccionado
        (view as TextView).textSize = 20F
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        (view as TextView).setTextColor(Color.WHITE) // Cambia el color del texto del desplegable
        (view as TextView).textSize = 20F
        (view as TextView).setPadding(15)
        return view
    }

}
