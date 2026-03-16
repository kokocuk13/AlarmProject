package presentation.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import presentation.R

data class AlarmItem(
    val time: String,
    val label: String,
    val days: String,
    var isEnabled: Boolean
)

class AlarmAdapter(
    private val items: MutableList<AlarmItem>
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    class AlarmViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeText: TextView = view.findViewById(R.id.alarmTime)
        val labelText: TextView = view.findViewById(R.id.alarmLabel)
        val daysText: TextView = view.findViewById(R.id.alarmDays)
        val toggle: Switch = view.findViewById(R.id.alarmToggle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val item = items[position]
        holder.timeText.text = item.time
        holder.labelText.text = item.label
        holder.daysText.text = item.days
        holder.toggle.isChecked = item.isEnabled
        holder.toggle.setOnCheckedChangeListener { _, isChecked ->
            items[position].isEnabled = isChecked
        }
    }

    override fun getItemCount() = items.size

    fun addAlarm(alarm: AlarmItem) {
        items.add(alarm)
        notifyItemInserted(items.size - 1)
    }
}