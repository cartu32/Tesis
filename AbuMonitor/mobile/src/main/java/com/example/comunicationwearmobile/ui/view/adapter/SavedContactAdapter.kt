package com.example.comunicationwearmobile.ui.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.abumonitor.data.model.EntityContact
import com.example.comunicationwearmobile.R


class SavedContactAdapter(
    private var contacts: List<EntityContact>,
    private val onLongClick: (EntityContact) -> Unit
) : RecyclerView.Adapter<SavedContactAdapter.SavedContactViewHolder>() {

    inner class SavedContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.txtName)
        val phone = itemView.findViewById<TextView>(R.id.txtPhone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.items_contact, parent, false)
        return SavedContactViewHolder(view)
    }

    override fun getItemCount() = contacts.size

    override fun onBindViewHolder(holder: SavedContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.name.text = contact.name
        holder.phone.text = contact.telephone

        holder.itemView.setOnLongClickListener {
            onLongClick(contact)
            true
        }
    }

    fun updateList(newContacts: List<EntityContact>) {
        contacts = newContacts
        notifyDataSetChanged()
    }
}
