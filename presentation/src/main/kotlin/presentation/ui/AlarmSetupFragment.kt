package presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import data.repository.AlarmRepositoryImpl
import data.scheduler.AndroidAlarmScheduler
import domain.usecases.CreateAlarmUseCase
import kotlinx.coroutines.launch
import presentation.R
import presentation.viewmodels.AlarmSetupViewModel
import presentation.viewmodels.AlarmUiState
import java.time.LocalTime

class AlarmSetupFragment : Fragment() {

    private lateinit var viewModel: AlarmSetupViewModel
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

        // ViewModel
        val repository = AlarmRepositoryImpl()
        val scheduler = AndroidAlarmScheduler()
        val useCase = CreateAlarmUseCase(repository, scheduler)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AlarmSetupViewModel(useCase) as T
            }
        })[AlarmSetupViewModel::class.java]

        // --- TimePicker ---
        val hourPicker = view.findViewById<NumberPicker>(R.id.hourPicker)
        val minutePicker = view.findViewById<NumberPicker>(R.id.minutePicker)
        hourPicker.minValue = 0
        hourPicker.maxValue = 23
        hourPicker.setFormatter { i -> String.format("%02d", i) }
        minutePicker.minValue = 0
        minutePicker.maxValue = 59
        minutePicker.setFormatter { i -> String.format("%02d", i) }

        // --- Дни недели ---
        val dayButtons = listOf(
            view.findViewById<Button>(R.id.dayMon),
            view.findViewById<Button>(R.id.dayTue),
            view.findViewById<Button>(R.id.dayWed),
            view.findViewById<Button>(R.id.dayThu),
            view.findViewById<Button>(R.id.dayFri),
            view.findViewById<Button>(R.id.daySat),
            view.findViewById<Button>(R.id.daySun)
        )
        selectedDays.add(0) // По умолчанию ПН
        updateDayButtons(dayButtons)
        dayButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (selectedDays.contains(index)) selectedDays.remove(index)
                else selectedDays.add(index)
                updateDayButtons(dayButtons)
            }
        }

        // --- Способ отключения ---
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

        // --- SeekBar ---
        val seekBar = view.findViewById<SeekBar>(R.id.shakeCountSeekBar)
        seekBar.max = 4
        seekBar.progress = 2

        // --- Кнопка Сохранить ---
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        val alarmNameInput = view.findViewById<TextInputEditText>(R.id.alarmNameInput)
        saveButton.setOnClickListener {
            val time = LocalTime.of(hourPicker.value, minutePicker.value)
            val timeStr = String.format("%02d:%02d", hourPicker.value, minutePicker.value)
            val label = alarmNameInput.text?.toString()?.trim() ?: "Будильник"
            val daysStr = if (selectedDays.isEmpty()) "Однократно" else {
                val names = listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС")
                selectedDays.sorted().joinToString(", ") { names[it] }
            }

            // Передаём результат назад в AlarmListFragment
            val bundle = Bundle().apply {
                putString("time", timeStr)
                putString("label", label)
                putString("days", daysStr)
            }
            parentFragmentManager.setFragmentResult("alarm_created", bundle)
            findNavController().navigateUp()
        }

        // --- Состояние ---
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is AlarmUiState.Success -> findNavController().navigateUp()
                    is AlarmUiState.Error -> { /* показать ошибку */ }
                    else -> {}
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