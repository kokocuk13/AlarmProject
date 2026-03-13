package presentation.ui

import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.alarmproject.R
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.ItemTouchHelper
import domain.models.Alarm
import java.time.LocalTime
import domain.models.ShakeTask

class AlarmListFragment : Fragment() {

    private lateinit var adapter: AlarmAdapter
    private val mockAlarms = mutableListOf(
        Alarm(LocalTime.of(7, 0), true, ShakeTask(10, false), "Зачет"),
        Alarm(LocalTime.of(10, 0), true, ShakeTask(20, false), "Вторая пара"),
        Alarm(LocalTime.of(12, 0), false, ShakeTask(5, false), "Таблетки"),
        Alarm(LocalTime.of(15, 0), false, ShakeTask(15, false), "В деканат")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alarm_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emptyText = view.findViewById<TextView>(R.id.emptyText)
        val recyclerView = view.findViewById<RecyclerView>(R.id.alarmsRecyclerView)
        val fab = view.findViewById<FloatingActionButton>(R.id.addAlarmFab)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = AlarmAdapter(mockAlarms, 
            onItemClick = { alarm ->
                val bundle = Bundle().apply {
                    putInt("hour", alarm.time.hour)
                    putInt("minute", alarm.time.minute)
                    putString("name", alarm.name)
                }
                findNavController().navigate(R.id.action_home_to_ringing, bundle)
            },
            onDeleteClick = { position ->
                mockAlarms.removeAt(position)
                adapter.notifyItemRemoved(position)
                updateEmptyState(emptyText, recyclerView)
            }
        )
        recyclerView.adapter = adapter

        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
            
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // В этой реализации мы не удаляем сразу по свайпу, 
                // а даем ItemTouchHelper отрисовать задний план.
                // Но так как нам нужно нажать на кнопку, мы просто сдвигаем контент.
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                val foregroundView = (viewHolder as AlarmAdapter.ViewHolder).foregroundView
                // Ограничиваем свайп шириной кнопки удаления (примерно 80dp)
                val translationX = if (dX < -300f) -300f else dX
                getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, translationX, dY, actionState, isCurrentlyActive)
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)

        updateEmptyState(emptyText, recyclerView)

        fab.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_setup)
        }
    }

    private fun updateEmptyState(emptyText: View, recyclerView: View) {
        if (mockAlarms.isEmpty()) {
            emptyText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}

class AlarmAdapter(
    private val alarms: List<Alarm>,
    private val onItemClick: (Alarm) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foregroundView: View = view.findViewById(R.id.foreground_view)
        val deleteBackground: View = view.findViewById(R.id.delete_background)
        val timeText: TextView = view.findViewById(R.id.alarmTimeText)
        val nameText: TextView = view.findViewById(R.id.alarmNameText)
        val switch: SwitchCompat = view.findViewById(R.id.alarmSwitch)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alarm = alarms[position]
        holder.timeText.text = String.format("%02d:%02d", alarm.time.hour, alarm.time.minute)
        holder.nameText.text = alarm.name ?: "Будильник"
        holder.switch.isChecked = alarm.isEnabled
        
        holder.foregroundView.setOnClickListener { onItemClick(alarm) }
        holder.deleteBackground.setOnClickListener { 
            onDeleteClick(holder.adapterPosition)
        }
    }

    override fun getItemCount() = alarms.size
}
