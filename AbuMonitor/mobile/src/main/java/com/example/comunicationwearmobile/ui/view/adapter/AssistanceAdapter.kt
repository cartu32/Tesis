package com.example.comunicationwearmobile.ui.view.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.abumonitor.data.model.EntityScheduledAssistance
import com.example.comunicationwearmobile.R
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Color
import android.widget.ImageView


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

        private val stateDateLinear:LinearLayout=itemView.findViewById(R.id.stateDateLinearLayout)
        private val title: TextView = itemView.findViewById(R.id.eventTitle)
        private val time: TextView = itemView.findViewById(R.id.eventTime)
        private val eventState:TextView=itemView.findViewById(R.id.eventState)
        private val imgEventState: ImageView =itemView.findViewById(R.id.eventStateIcon)


        init{

            val drawable = GradientDrawable()
            drawable.setColor(Color.WHITE)
            drawable.setStroke(4, Color.BLACK)
            drawable.cornerRadius = 16f
            stateDateLinear.background = drawable
        }

        fun bind(assistance: EntityScheduledAssistance) {
            val date = Date(assistance.date_hour_appointment)
            val format = SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.getDefault())

            //calculo cual es la hora en que la cita se desactiva
            val endTimeAppointmentActivate=assistance.date_hour_appointment+assistance.time_duration_activation_appointment

            title.text = assistance.description
            time.text = format.format(date)
            itemView.setOnClickListener { onClick(assistance) }
            setEventState(assistance.went_appointment, endTimeAppointmentActivate)
        }

        private fun setEventState(wentAssistance: Boolean, endTimeAppointmentActivate: Long )
        {
            when {
                wentAssistance -> {
                    eventState.text = "Cita asistida"
                    imgEventState.setImageResource(R.drawable.ic_check_circle_24)
                }
                endTimeAppointmentActivate < System.currentTimeMillis() -> {
                    eventState.text = "Cita no asistida"
                    imgEventState.setImageResource(R.drawable.ic_event_busy)
                }
                else -> {
                    eventState.text = "Cita pendiente"
                    imgEventState.setImageResource(R.drawable.ic_schedule_24)
                }
            }

        }

    }
} 