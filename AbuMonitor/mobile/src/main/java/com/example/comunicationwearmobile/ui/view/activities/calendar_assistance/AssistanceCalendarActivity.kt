package com.example.comunicationwearmobile.ui.view.activities.calendar_assistance

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityScheduledAssistance
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.common.SharedVariables
import com.example.comunicationwearmobile.ui.utils.Tools
import com.example.comunicationwearmobile.ui.view.adapter.AssistanceAdapter
import com.example.comunicationwearmobile.ui.viewmodel.AssistanceViewModelFactory
import com.example.comunicationwearmobile.ui.viewmodel.ViewModelCalendarAssistance
import com.jakewharton.threetenabp.AndroidThreeTen
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import java.util.Calendar

class AssistanceCalendarActivity : AppCompatActivity() {

    private lateinit var calendarView: MaterialCalendarView
    private lateinit var cmdAddEvent: Button
    private lateinit var recyclerView: RecyclerView
    private val viewModel: ViewModelCalendarAssistance by viewModels { AssistanceViewModelFactory(application) }
    private var allEvents: List<EntityScheduledAssistance> = emptyList()
    private lateinit var adapter: AssistanceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)
        setContentView(R.layout.activity_assistance_calendar)

        calendarView = findViewById(R.id.calendarView)
        cmdAddEvent = findViewById(R.id.cmdAddEvent)
        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)

        calendarView.selectedDate = CalendarDay.today()
        viewModel.selectedDateMillis.value = getDateMillis(CalendarDay.today())

        // Inicializa decoradores con el mes actual
        var currentMonth = calendarView.currentDate.month

        configActionBar()
        configListdapter()
        configListeners()
        configObserver(currentMonth)
        configComponents()
        updateMonthDecorators(currentMonth)
    }

    private fun configComponents() {
        if(SharedVariables.user==SharedVariables.USER_ADMIN){
            cmdAddEvent.isEnabled=true
            cmdAddEvent.text=getString(R.string.cmd_add_event)
        }else{
            cmdAddEvent.isEnabled=false
            cmdAddEvent.text=getString(R.string.empty_text)
        }

    }

    private fun configActionBar() {
        val actionBar = supportActionBar
        actionBar?.title = "AbuMonitor"
        actionBar?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        val textColor = SpannableString(actionBar?.title ?: "")
        textColor.setSpan(ForegroundColorSpan(Color.WHITE), 0, textColor.length, 0)
        actionBar?.title = textColor
    }


    private fun configObserver(currentMonth: Int) {
        // Observa los eventos filtrados por fecha seleccionada
        viewModel.eventsBySelectedDate.observe(this) { events ->
            adapter.submitList(events)
        }

        // Observa todos los eventos para actualizar decoradores
        viewModel.getAllEvents().observe(this) { events ->
            allEvents = events
            updateMonthDecorators(currentMonth)
        }

    }

    private fun configListdapter() {
        adapter = AssistanceAdapter { assistance ->
            val intent = Intent(this, AssistanceDetailActivity::class.java)
            intent.putExtra("assistance_id", assistance.id_assistance)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

    }

    private fun getCountItemsRecicleView():Int{
        val totalItems = recyclerView.adapter?.itemCount ?: 0

        Log.d(Definition.TAG_DEBUG, "Total item Recycleview: $totalItems")

        return totalItems
    }

    private fun configListeners() {
        calendarView.setOnMonthChangedListener { _, date ->
            val currentMonth = date.month
            updateMonthDecorators(currentMonth)
        }

        // Manejo de la fecha seleccionada a través del ViewModel
        calendarView.setOnDateChangedListener { _, date, _ ->

            viewModel.selectedDateMillis.value = getDateMillis(date)
        }


        cmdAddEvent.setOnClickListener {
            viewModel.selectedDateMillis.value?.let { millis ->
                val count = getCountItemsRecicleView()
                val max = Definition.COUNT_MAX_DATE_FOR_DAY
                Log.d("DEBUG", "count=$count, max=$max")

                if (count >= max) {
                    Toast.makeText(this, "Se llegó al máximo de citas...", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if(!Tools.isGreaterThanToday(millis)){
                    Toast.makeText(this,"Seleccione una fecha en el futuro",Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val intent = Intent(this, AssistanceAddActivity::class.java)
                intent.putExtra("date", millis)
                startActivity(intent)
            }
        }
    }

    fun updateMonthDecorators(month: Int) {
        calendarView.removeDecorators()
        val datesWithEvents = allEvents.map {
            val localDate = Instant.ofEpochMilli(it.date_hour_appointment.toLong())
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            CalendarDay.from(localDate)
        }.toSet()

        calendarView.addDecorator(CurrentMonthDayDecorator(month))
        calendarView.addDecorator(OtherMonthDayDecorator(month))
        calendarView.addDecorator(EventDecorator(datesWithEvents))
        calendarView.addDecorator(TodayDecorator(this))

    }

    private fun getDateMillis(date: CalendarDay): Long {
        val cal = Calendar.getInstance()
        cal.set(date.year, date.month - 1, date.day, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val onlydateinMillis= Tools.extractDayOfDateInMillis(cal.timeInMillis)
        return onlydateinMillis
    }


}

// Decorador para marcar días con eventos
class EventDecorator(private val dates: Set<CalendarDay>) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean = dates.contains(day)
    override fun decorate(view: DayViewFacade) {
        view.addSpan(ForegroundColorSpan(Color.RED))
    }
}


// Decorador para días del mes actual (negro)
class CurrentMonthDayDecorator(private val currentMonth: Int) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean = day.month == currentMonth
    override fun decorate(view: DayViewFacade) {
        view.addSpan(ForegroundColorSpan(Color.BLACK))
    }
}

// Decorador para días de otros meses (gris)
class OtherMonthDayDecorator(private val currentMonth: Int) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean = day.month != currentMonth
    override fun decorate(view: DayViewFacade) {
        view.addSpan(ForegroundColorSpan(Color.parseColor("#B0B0B0")))
    }
}

class TodayDecorator(context: Context) : DayViewDecorator {
    private val appContext=context.applicationContext
    private val today: CalendarDay = CalendarDay.today()
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day == today
    }
    override fun decorate(view: DayViewFacade) {
        val drawable = ContextCompat.getDrawable(appContext, R.drawable.circle_background)
        view.setBackgroundDrawable(drawable!!)
    }
}
