package presentation.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmproject.R
import domain.models.Alarm
import domain.models.BarcodeTask
import domain.models.ShakeTask

class AlarmAdapter(
    private val onDeleteClick: (Alarm) -> Unit,
    private val onEditClick:   (Alarm) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.ViewHolder>() {

    private val alarms = mutableListOf<Alarm>()

    fun getAlarmAt(position: Int): Alarm = alarms[position]

    fun updateAlarms(newAlarms: List<Alarm>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = alarms.size
            override fun getNewListSize() = newAlarms.size
            override fun areItemsTheSame(o: Int, n: Int) = alarms[o].id == newAlarms[n].id
            override fun areContentsTheSame(o: Int, n: Int) = alarms[o] == newAlarms[n]
        })
        alarms.clear()
        alarms.addAll(newAlarms)
        diff.dispatchUpdatesTo(this)
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
        holder.timeText.text    = String.format("%02d:%02d", alarm.time.hour, alarm.time.minute)
        holder.nameText.text    = alarm.name?.takeIf { it.isNotBlank() } ?: "Будильник"
        holder.daysText.text    = formatDays(alarm.days)
        holder.switch.isChecked = alarm.isEnabled
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
