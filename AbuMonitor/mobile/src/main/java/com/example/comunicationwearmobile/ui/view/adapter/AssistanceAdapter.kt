package com.example.comunicationwearmobile.ui.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.abumonitor.data.model.EntityScheduledAssistance
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.utils.Tools
import java.util.*

class AssistanceAdapter(private val onClick: (EntityScheduledAssistance) -> Unit) :
    ListAdapter<EntityScheduledAssistance, AssistanceAdapter.EventViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<EntityScheduledAssistance>() {
        override fun areItemsTheSame(oldItem: EntityScheduledAssistance, newItem: EntityScheduledAssistance) = oldItem.id_assistance == newItem.id_assistance
        override fun areContentsTheSame(oldItem: EntityScheduledAssistance, newItem: EntityScheduledAssistance) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_assistance, parent, false)
        return EventViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EventViewHolder(itemView: View, val onClick: (EntityScheduledAssistance) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.eventTitle)
        private val time: TextView = itemView.findViewById(R.id.eventTime)
        private var currentEvent: EntityScheduledAssistance? = null

        fun bind(assistance: EntityScheduledAssistance) {
            val date = Tools.getMillisToDate(assistance.date_appointment)
            val hour = Tools.formatHour(assistance.hour_appointment)
            currentEvent = assistance
            title.text = assistance.description
            time.text = "${date} - ${hour}"
            itemView.setOnClickListener { onClick(assistance) }
        }

    }
} 