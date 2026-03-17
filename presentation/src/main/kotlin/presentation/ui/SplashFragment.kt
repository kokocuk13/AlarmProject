package presentation.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import presentation.R

class SplashFragment : Fragment() {

    private val splashHandler = Handler(Looper.getMainLooper())
    private val navigateRunnable = Runnable {
        if (!isAdded) return@Runnable

        val navController = findNavController()
        if (navController.currentDestination?.id != R.id.splashFragment) return@Runnable

        val prefs = requireContext().getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        val onboardingShown = prefs.getBoolean("onboarding_shown", false)

        if (onboardingShown) {
            navController.navigate(R.id.action_splash_to_home)
        } else {
            navController.navigate(R.id.action_splash_to_onboarding)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        splashHandler.postDelayed(navigateRunnable, 1500)
    }

    override fun onDestroyView() {
        splashHandler.removeCallbacks(navigateRunnable)
        super.onDestroyView()
    }
}
