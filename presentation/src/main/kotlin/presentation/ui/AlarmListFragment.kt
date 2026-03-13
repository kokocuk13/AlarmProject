// Реализация перемещена в модуль app.
// Файл: app/src/main/java/presentation/ui/AlarmListFragment.kt
package presentation.ui

import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import presentation.R

class AlarmListFragment : Fragment() {

    private lateinit var adapter: AlarmAdapter
    private val alarmItems = mutableListOf<AlarmItem>()

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

        adapter = AlarmAdapter(alarmItems)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        updateEmptyState(emptyText, recyclerView)

        // Получаем результат от AlarmSetupFragment
        setFragmentResultListener("alarm_created") { _, bundle ->
            val time = bundle.getString("time") ?: "00:00"
            val label = bundle.getString("label") ?: "Будильник"
            val days = bundle.getString("days") ?: "Однократно"
            adapter.addAlarm(AlarmItem(time, label, days, true))
            updateEmptyState(emptyText, recyclerView)
        }

        fab.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_setup)
        }
    }

    private fun updateEmptyState(emptyText: TextView, recyclerView: RecyclerView) {
        if (alarmItems.isEmpty()) {
            emptyText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}