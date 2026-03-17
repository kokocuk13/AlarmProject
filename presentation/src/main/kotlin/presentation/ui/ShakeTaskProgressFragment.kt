package presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import domain.repository.IShakeSensor
import presentation.R
import presentation.di.PresentationDependencies

class ShakeTaskProgressFragment : Fragment() {

    private var shakeSensor: IShakeSensor? = null
    private var requiredShakes: Int = 20
    private var currentShakes: Int = 0

    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requiredShakes = arguments?.getInt(ARG_REQUIRED_SHAKES, 20)?.coerceAtLeast(1) ?: 20
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_shake_task_progress, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = view.findViewById(R.id.shakeProgressBar)
        progressText = view.findViewById(R.id.shakeProgressText)

        progressBar.max = requiredShakes
        updateProgressUi()

        shakeSensor = PresentationDependencies.provideShakeSensor()
        shakeSensor?.start {
            if (!isAdded) return@start
            requireActivity().runOnUiThread {
                currentShakes += 1
                updateProgressUi()
                if (currentShakes >= requiredShakes) {
                    completeTask()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        shakeSensor?.stop()
    }

    private fun updateProgressUi() {
        progressBar.progress = currentShakes.coerceAtMost(requiredShakes)
        progressText.text = "$currentShakes / $requiredShakes"
    }

    private fun completeTask() {
        shakeSensor?.stop()
        findNavController().navigate(R.id.action_shake_to_success)
    }

    companion object {
        const val ARG_REQUIRED_SHAKES = "required_shakes"
    }
}
