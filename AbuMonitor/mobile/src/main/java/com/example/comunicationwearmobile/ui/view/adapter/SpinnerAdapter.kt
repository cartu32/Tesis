package com.example.comunicationwearmobile.ui.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.model.extra.StateSpinner

class SpinnerAdapter(private val mContext: Context, resource: Int, private val listState: ArrayList<StateSpinner>)
    : ArrayAdapter<StateSpinner>(mContext, resource, listState) {

    private var isFromView = false

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        var rowView = convertView

        if (rowView == null) {
            val inflater = LayoutInflater.from(mContext)
            rowView = inflater.inflate(R.layout.spinner_items, parent, false)
            holder = ViewHolder(
                mTextView = rowView.findViewById(R.id.text),
                mCheckBox = rowView.findViewById(R.id.checkbox)
            )
            rowView.tag = holder
        } else {
            holder = rowView.tag as ViewHolder
        }

        holder.mTextView.text = listState[position].title

        // Para evitar que el evento checked se dispare en la creación de la vista
        isFromView = true
        holder.mCheckBox.isChecked = listState[position].selected
        isFromView = false

        holder.mCheckBox.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
        holder.mCheckBox.tag = position

        holder.mCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            val pos = buttonView.tag as Int
            if (!isFromView) {
                listState[pos].selected = isChecked
            }
        }

        return rowView!!
    }

    private data class ViewHolder(
        val mTextView: TextView,
        val mCheckBox: CheckBox
    )
}
