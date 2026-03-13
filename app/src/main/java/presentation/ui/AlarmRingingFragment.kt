package presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.alarmproject.R

class AlarmRingingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alarm_ringing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name = arguments?.getString("name") ?: "Будильник"
        val hour = arguments?.getInt("hour") ?: 7
        val minute = arguments?.getInt("minute") ?: 0
        
        view.findViewById<TextView>(R.id.ringingTitle).text = 
            String.format("%s\n%02d:%02d", name, hour, minute)

        view.findViewById<Button>(R.id.dismissButton).setOnClickListener {
            // В мокапе сразу переходим на "успех"
            findNavController().navigate(R.id.alarmSuccessFragment)
        }
    }
}
