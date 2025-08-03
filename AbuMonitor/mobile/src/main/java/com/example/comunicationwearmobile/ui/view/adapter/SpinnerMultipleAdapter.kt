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
import com.example.comunicationwearmobile.ui.utils.interfaces.OnCheckboxClickListener

class SpinnerMultipleAdapter(context: Context, resource: Int, private val listState: ArrayList<StateSpinner>) :
    ArrayAdapter<StateSpinner>(context, resource, listState) {

    private var isFromView = false
    private var checkboxesEnabled:Boolean=true

    private val onCheckboxClickListener: OnCheckboxClickListener = context as OnCheckboxClickListener

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val rowView = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_items, parent, false).also {
            val newHolder = ViewHolder(
                mTextView = it.findViewById(R.id.text),
                mCheckBox = it.findViewById(R.id.checkbox)
            )
            it.tag = newHolder
        }

        holder = rowView.tag as ViewHolder

        holder.mTextView.text = listState[position].title

        isFromView = true
        holder.mCheckBox.isChecked = listState[position].selected
        isFromView = false

        holder.mCheckBox.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
        holder.mCheckBox.tag = position
        holder.mCheckBox.isEnabled = checkboxesEnabled

        holder.mCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            val pos = buttonView.tag as Int
            if (!isFromView) {
                listState[pos].selected = isChecked
                onCheckboxClickListener.onCheckboxClicked(pos, isChecked)
            }
        }

        return rowView
    }

    fun setSelectedItemsByPositions(indices: List<Int>) {
        listState.forEachIndexed { index, item ->
            item.selected = index in indices
        }
        notifyDataSetChanged()
    }

    fun modifyVisibilityCheckBox(state: Boolean) {
        checkboxesEnabled=state
        notifyDataSetChanged()
    }

    private data class ViewHolder(
        val mTextView: TextView,
        val mCheckBox: CheckBox
    )
}
