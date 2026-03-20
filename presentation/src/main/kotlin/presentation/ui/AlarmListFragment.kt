package presentation.ui

import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import domain.models.Alarm
import domain.models.BarcodeTask
import domain.models.ShakeTask
import kotlinx.coroutines.launch
import presentation.R
import presentation.di.PresentationDependencies
import com.google.android.material.floatingactionbutton.FloatingActionButton
import presentation.viewmodels.AlarmListViewModel
import java.util.Locale

class AlarmListFragment : Fragment() {

    private val viewModel: AlarmListViewModel by viewModels {
        PresentationDependencies.alarmListViewModelFactory
    }

    private lateinit var adapter: AlarmAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_alarm_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emptyText    = view.findViewById<TextView>(R.id.emptyText)
        val recyclerView = view.findViewById<RecyclerView>(R.id.alarmsRecyclerView)
        val fab          = view.findViewById<FloatingActionButton>(R.id.addAlarmFab)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = AlarmAdapter(
            onDeleteClick = { alarm -> viewModel.deleteAlarm(alarm) },
            onEditClick   = { alarm ->
                findNavController().navigate(
                    R.id.action_home_to_setup,
                    bundleOf("alarmId" to alarm.id)
                )
            },
            onToggleClick = { alarm, isEnabled ->
                viewModel.toggleAlarm(alarm, isEnabled)
            }
        )
        recyclerView.adapter = adapter

        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) viewModel.deleteAlarm(adapter.getAlarmAt(pos))
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                getDefaultUIUtil().clearView((viewHolder as AlarmAdapter.ViewHolder).foregroundView)
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                getDefaultUIUtil().onDraw(
                    c, recyclerView,
                    (viewHolder as AlarmAdapter.ViewHolder).foregroundView,
                    dX.coerceAtLeast(-300f), dY, actionState, isCurrentlyActive
                )
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.alarms.collect { alarms ->
                    adapter.updateAlarms(alarms)
                    emptyText.visibility    = if (alarms.isEmpty()) View.VISIBLE else View.GONE
                    recyclerView.visibility = if (alarms.isEmpty()) View.GONE   else View.VISIBLE
                }
            }
        }

        fab.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_setup)
        }
    }
}

class AlarmAdapter(
    private val onDeleteClick: (Alarm) -> Unit,
    private val onEditClick:   (Alarm) -> Unit = {},
    private val onToggleClick: (Alarm, Boolean) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.ViewHolder>() {

    private val alarms = mutableListOf<Alarm>()

    fun getAlarmAt(position: Int): Alarm = alarms[position]

    fun updateAlarms(newAlarms: List<Alarm>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = alarms.size
            override fun getNewListSize() = newAlarms.size
            override fun areItemsTheSame(o: Int, n: Int) = alarms[o].id == newAlarms[n].id
            override fun areContentsTheSame(o: Int, n: Int) = alarms[o] == newAlarms[n]
        })
        alarms.clear()
        alarms.addAll(newAlarms)
        diffResult.dispatchUpdatesTo(this)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foregroundView: View   = view.findViewById(R.id.foreground_view)
        val deleteBackground: View = view.findViewById(R.id.delete_background)
        val timeText: TextView     = view.findViewById(R.id.alarmTimeText)
        val nameText: TextView     = view.findViewById(R.id.alarmNameText)
        val daysText: TextView     = view.findViewById(R.id.alarmDaysText)
        val taskBadge: TextView    = view.findViewById(R.id.alarmTaskBadge)
        val switch: SwitchCompat   = view.findViewById(R.id.alarmSwitch)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_alarm, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alarm = alarms[position]
        holder.timeText.text    = String.format(Locale.getDefault(), "%02d:%02d", alarm.time.hour, alarm.time.minute)
        holder.nameText.text    = alarm.name?.takeIf { it.isNotBlank() } ?: "Будильник"
        holder.daysText.text    = formatDays(alarm.days)
        
        holder.switch.setOnCheckedChangeListener(null)
        holder.switch.isChecked = alarm.isEnabled
        holder.switch.setOnCheckedChangeListener { _, isChecked ->
            onToggleClick(alarm, isChecked)
        }

        holder.taskBadge.text   = when (val task = alarm.task) {
            is ShakeTask   -> "Встряска × ${task.requiredShakes}"
            is BarcodeTask -> "Штрих-код"
            else           -> ""
        }
        holder.foregroundView.setOnClickListener   { onEditClick(alarm) }
        holder.deleteBackground.setOnClickListener { onDeleteClick(alarm) }
    }

    override fun getItemCount() = alarms.size

    private fun formatDays(days: List<Int>): String {
        if (days.isEmpty()) return ""
        if (days.size == 7) return "Ежедневно"
        val names = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
        return days.sorted().joinToString(", ") { names.getOrElse(it) { "" } }
    }
}
