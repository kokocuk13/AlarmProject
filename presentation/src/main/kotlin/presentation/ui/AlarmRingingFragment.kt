package presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import presentation.R

class AlarmRingingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alarm_ringing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val alarmId = arguments?.getLong(ARG_ALARM_ID, -1L) ?: -1L
        val name = arguments?.getString("name") ?: "Будильник"
        val hour = arguments?.getInt("hour") ?: 7
        val minute = arguments?.getInt("minute") ?: 0
        val taskType = arguments?.getString(ARG_TASK_TYPE) ?: TASK_SHAKE
        val requiredShakes = arguments?.getInt(ARG_REQUIRED_SHAKES) ?: 20
        val requiredBarcode = arguments?.getString(ARG_REQUIRED_BARCODE)
        
        view.findViewById<TextView>(R.id.ringingTitle).text = 
            String.format("%s\n%02d:%02d", name, hour, minute)

        view.findViewById<Button>(R.id.dismissButton).setOnClickListener {
            val commonBundle = bundleOf(
                ARG_ALARM_ID to alarmId,
                "name" to name,
                "hour" to hour,
                "minute" to minute
            )

            if (taskType.equals(TASK_BARCODE, ignoreCase = true)) {
                commonBundle.putString(ARG_REQUIRED_BARCODE, requiredBarcode)
                findNavController().navigate(
                    R.id.action_ringing_to_barcode,
                    commonBundle
                )
            } else {
                commonBundle.putInt(ARG_REQUIRED_SHAKES, requiredShakes)
                findNavController().navigate(
                    R.id.action_ringing_to_shake,
                    commonBundle
                )
            }
        }
    }

    companion object {
        const val ARG_ALARM_ID = "ALARM_ID"
        const val ARG_TASK_TYPE = "task_type"
        const val ARG_REQUIRED_SHAKES = "required_shakes"
        const val ARG_REQUIRED_BARCODE = "required_barcode"

        private const val TASK_SHAKE = "SHAKE"
        private const val TASK_BARCODE = "BARCODE"
    }
}
