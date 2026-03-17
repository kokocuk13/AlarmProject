package presentation.ui

import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
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
            }
        )
        recyclerView.adapter = adapter

        setupSwipeToDelete(recyclerView)

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

    private fun setupSwipeToDelete(recyclerView: RecyclerView) {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
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
    }
}
