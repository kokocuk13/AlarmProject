package presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import presentation.R
import presentation.di.PresentationDependencies
import com.google.android.material.textfield.TextInputEditText
import presentation.viewmodels.AlarmSetupViewModel
import presentation.viewmodels.AlarmUiState
import kotlinx.coroutines.launch
import java.time.LocalTime

class AlarmSetupFragment : Fragment() {

    private val viewModel: AlarmSetupViewModel by viewModels {
        PresentationDependencies.alarmSetupViewModelFactory
    }

    private var isShakeSelected = true
    private val selectedDays = mutableSetOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alarm_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val hourPicker = view.findViewById<NumberPicker>(R.id.hourPicker)
        val minutePicker = view.findViewById<NumberPicker>(R.id.minutePicker)

        hourPicker.minValue = 0
        hourPicker.maxValue = 23
        minutePicker.minValue = 0
        minutePicker.maxValue = 59

        val dayButtons = listOf<Button>(
            view.findViewById(R.id.dayMon),
            view.findViewById(R.id.dayTue),
            view.findViewById(R.id.dayWed),
            view.findViewById(R.id.dayThu),
            view.findViewById(R.id.dayFri),
            view.findViewById(R.id.daySat),
            view.findViewById(R.id.daySun)
        )

        selectedDays.add(0)
        updateDayButtons(dayButtons)

        dayButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (selectedDays.contains(index)) {
                    selectedDays.remove(index)
                } else {
                    selectedDays.add(index)
                }
                updateDayButtons(dayButtons)
            }
        }

        val shakeCard = view.findViewById<LinearLayout>(R.id.shakeCard)
        val barcodeCard = view.findViewById<LinearLayout>(R.id.barcodeCard)
        val shakeCountContainer = view.findViewById<LinearLayout>(R.id.shakeCountContainer)

        shakeCard.setOnClickListener {
            isShakeSelected = true
            updateDismissCards(shakeCard, barcodeCard)
            shakeCountContainer.visibility = View.VISIBLE
        }

        barcodeCard.setOnClickListener {
            isShakeSelected = false
            updateDismissCards(shakeCard, barcodeCard)
            shakeCountContainer.visibility = View.GONE
        }

        val seekBar = view.findViewById<SeekBar>(R.id.shakeCountSeekBar)
        seekBar.max = 40
        seekBar.progress = 20

        val saveButton = view.findViewById<Button>(R.id.saveButton)
        val alarmNameInput = view.findViewById<TextInputEditText>(R.id.alarmNameInput)

        saveButton.setOnClickListener {
            val time = LocalTime.of(hourPicker.value, minutePicker.value)
            val shakes = if (isShakeSelected) seekBar.progress else 0
            val name = alarmNameInput.text?.toString()?.takeIf { it.isNotBlank() } ?: "Будильник"
            viewModel.save(time, shakes, name)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is AlarmUiState.Success -> {
                        Toast.makeText(context, "Будильник сохранён!", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    is AlarmUiState.Error -> {
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    }
                    is AlarmUiState.Loading -> {
                        saveButton.isEnabled = false
                    }
                    else -> {
                        saveButton.isEnabled = true
                    }
                }
            }
        }
    }

    private fun updateDayButtons(buttons: List<Button>) {
        buttons.forEachIndexed { index, button ->
            if (selectedDays.contains(index)) {
                button.setBackgroundColor(android.graphics.Color.parseColor("#6B4EFF"))
                button.setTextColor(android.graphics.Color.WHITE)
            } else {
                button.setBackgroundColor(android.graphics.Color.parseColor("#F0F0F0"))
                button.setTextColor(android.graphics.Color.parseColor("#333333"))
            }
        }
    }

    private fun updateDismissCards(shakeCard: LinearLayout, barcodeCard: LinearLayout) {
        if (isShakeSelected) {
            shakeCard.setBackgroundResource(R.drawable.dismiss_card_selected)
            barcodeCard.setBackgroundResource(R.drawable.dismiss_card_normal)
        } else {
            shakeCard.setBackgroundResource(R.drawable.dismiss_card_normal)
            barcodeCard.setBackgroundResource(R.drawable.dismiss_card_selected)
        }
    }
}
