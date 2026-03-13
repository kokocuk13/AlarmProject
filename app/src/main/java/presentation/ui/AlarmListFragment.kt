package presentation.ui

import androidx.recyclerview.widget.DiffUtil
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmproject.R
import com.example.alarmproject.di.AppModule
import com.google.android.material.floatingactionbutton.FloatingActionButton
import domain.models.Alarm
import kotlinx.coroutines.launch
import presentation.viewmodels.AlarmListViewModel

class AlarmListFragment : Fragment() {

    private val viewModel: AlarmListViewModel by viewModels {
        AppModule.provideAlarmListViewModelFactory()
    }

    private lateinit var adapter: AlarmAdapter

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

        adapter = AlarmAdapter(
            onDeleteClick = { alarm ->
                viewModel.deleteAlarm(alarm)
            }
        )
        recyclerView.adapter = adapter

        // Свайп влево — открывает кнопку удаления
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                t: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Сдвиг обрабатывается через onChildDraw; удаление — по нажатию кнопки
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val foreground = (viewHolder as AlarmAdapter.ViewHolder).foregroundView
                // Ограничиваем ход свайпа шириной кнопки удаления
                val limitedDX = if (dX < -300f) -300f else dX
                getDefaultUIUtil().onDraw(
                    c, recyclerView, foreground, limitedDX, dY, actionState, isCurrentlyActive
                )
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)

        // Наблюдаем за живым списком будильников из Room
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.alarms.collect { alarms ->
                    adapter.updateAlarms(alarms)
                    updateEmptyState(emptyText, recyclerView, alarms)
                }
            }
        }

        fab.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_setup)
        }
    }

    private fun updateEmptyState(emptyText: View, recyclerView: View, alarms: List<Alarm>) {
        if (alarms.isEmpty()) {
            emptyText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}

/**
 * Адаптер для списка будильников.
 * Поддерживает обновление списка через [updateAlarms] и удаление по нажатию кнопки.
 */
class AlarmAdapter(
    private val onDeleteClick: (Alarm) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.ViewHolder>() {

    private val alarms = mutableListOf<Alarm>()

    /** Обновляет список будильников с анимацией через DiffUtil. */
    fun updateAlarms(newAlarms: List<Alarm>) {
        val diffResult = DiffUtil.calculateDiff(AlarmDiffCallback(alarms, newAlarms))
        alarms.clear()
        alarms.addAll(newAlarms)
        diffResult.dispatchUpdatesTo(this)
    }

    /** DiffUtil callback для вычисления минимального числа изменений в списке. */
    private class AlarmDiffCallback(
        private val oldList: List<Alarm>,
        private val newList: List<Alarm>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size
        override fun areItemsTheSame(oldPos: Int, newPos: Int) =
            oldList[oldPos].id == newList[newPos].id
        override fun areContentsTheSame(oldPos: Int, newPos: Int) =
            oldList[oldPos] == newList[newPos]
    }

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
        // Кнопка удаления на заднем плане (появляется при свайпе)
        holder.deleteBackground.setOnClickListener { onDeleteClick(alarm) }
    }

    override fun getItemCount() = alarms.size
}
