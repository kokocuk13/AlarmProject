package presentation.ui

import android.app.NotificationManager
import android.content.Context
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
    private var alarmId: Long = -1L

    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requiredShakes = arguments?.getInt(ARG_REQUIRED_SHAKES, 20)?.coerceAtLeast(1) ?: 20
        alarmId = arguments?.getLong(AlarmRingingFragment.ARG_ALARM_ID, -1L) ?: -1L
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
        
        // Останавливаем сервис через делегат, чтобы избежать прямой зависимости между модулями
        PresentationDependencies.stopAlarmService?.invoke()

        if (alarmId != -1L) {
            val notificationManager = 
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(alarmId.toInt())
        }

        findNavController().navigate(R.id.action_shake_to_success)
    }

    companion object {
        const val ARG_REQUIRED_SHAKES = "required_shakes"
    }
}
