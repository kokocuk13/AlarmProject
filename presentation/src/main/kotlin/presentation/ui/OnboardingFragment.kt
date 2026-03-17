package presentation.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import presentation.R

class OnboardingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.startButton).setOnClickListener {
            // Сохраняем что онбординг был показан
            val prefs = requireContext().getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("onboarding_shown", true).apply()

            findNavController().navigate(R.id.action_onboarding_to_home)
        }
    }
}
