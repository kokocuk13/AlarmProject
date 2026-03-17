package presentation.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.alarmproject.R
import com.example.alarmproject.di.AppModule
import domain.models.BarcodeTask
import domain.models.ShakeTask
import kotlinx.coroutines.launch
import presentation.viewmodels.AlarmSetupViewModel
import presentation.viewmodels.AlarmUiState
import java.time.LocalTime

class AlarmSetupFragment : Fragment() {

    private val viewModel: AlarmSetupViewModel by viewModels {
        AppModule.provideAlarmSetupViewModelFactory()
    }

    private var isShakeSelected = true
    private val selectedDays    = mutableSetOf<Int>()
    private var editingAlarmId  = -1L
    private var dataLoaded      = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_alarm_setup, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editingAlarmId = arguments?.getLong("alarmId", -1L) ?: -1L

        val hourPicker     = view.findViewById<NumberPicker>(R.id.hourPicker)
        val minutePicker   = view.findViewById<NumberPicker>(R.id.minutePicker)
        val shakeCard      = view.findViewById<LinearLayout>(R.id.shakeCard)
        val barcodeCard    = view.findViewById<LinearLayout>(R.id.barcodeCard)
        val shakeContainer = view.findViewById<LinearLayout>(R.id.shakeCountContainer)
        val seekBar        = view.findViewById<SeekBar>(R.id.shakeCountSeekBar)
        val nameInput      = view.findViewById<EditText>(R.id.alarmNameInput)
        val saveButton     = view.findViewById<Button>(R.id.saveButton)
        val dayButtons     = listOf<Button>(
            view.findViewById(R.id.dayMon), view.findViewById(R.id.dayTue),
            view.findViewById(R.id.dayWed), view.findViewById(R.id.dayThu),
            view.findViewById(R.id.dayFri), view.findViewById(R.id.daySat),
            view.findViewById(R.id.daySun)
        )

        hourPicker.minValue   = 0;  hourPicker.maxValue   = 23
        minutePicker.minValue = 0;  minutePicker.maxValue = 59
        forcePickerBlack(hourPicker)
        forcePickerBlack(minutePicker)
        seekBar.min = 1; seekBar.max = 50; seekBar.progress = 20

        if (editingAlarmId != -1L && !dataLoaded) {
            viewLifecycleOwner.lifecycleScope.launch {
                val alarm = viewModel.loadAlarm(editingAlarmId)
                if (alarm != null) {
                    dataLoaded = true
                    hourPicker.value   = alarm.time.hour
                    minutePicker.value = alarm.time.minute
                    if (!alarm.name.isNullOrBlank()) nameInput.setText(alarm.name)
                    selectedDays.clear()
                    selectedDays.addAll(alarm.days)
                    when (alarm.task) {
                        is BarcodeTask -> { isShakeSelected = false; shakeContainer.visibility = View.GONE }
                        is ShakeTask   -> {
                            isShakeSelected = true
                            shakeContainer.visibility = View.VISIBLE
                            seekBar.progress = (alarm.task as ShakeTask).requiredShakes.coerceIn(1, 50)
                        }
                        else -> {}
                    }
                }
                updateDayButtons(dayButtons)
                updateDismissCards(shakeCard, barcodeCard)
            }
        } else {
            selectedDays.add(0)
            updateDayButtons(dayButtons)
            shakeContainer.visibility = View.VISIBLE
            updateDismissCards(shakeCard, barcodeCard)
        }

        dayButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (selectedDays.contains(index)) selectedDays.remove(index) else selectedDays.add(index)
                updateDayButtons(dayButtons)
            }
        }

        shakeCard.setOnClickListener {
            isShakeSelected = true
            updateDismissCards(shakeCard, barcodeCard)
            shakeContainer.visibility = View.VISIBLE
        }
        barcodeCard.setOnClickListener {
            isShakeSelected = false
            updateDismissCards(shakeCard, barcodeCard)
            shakeContainer.visibility = View.GONE
        }

        saveButton.setOnClickListener {
            val name = nameInput.text?.toString()?.trim()?.takeIf { it.isNotBlank() } ?: "Будильник"
            viewModel.save(
                time     = LocalTime.of(hourPicker.value, minutePicker.value),
                shakes   = if (isShakeSelected) seekBar.progress.coerceAtLeast(1) else 0,
                name     = name,
                days     = selectedDays.toList(),
                taskType = if (isShakeSelected) "SHAKE" else "BARCODE",
                alarmId  = if (editingAlarmId != -1L) editingAlarmId else 0L
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is AlarmUiState.Success -> {
                        Toast.makeText(context, "Будильник сохранён!", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    is AlarmUiState.Error   -> {
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                        saveButton.isEnabled = true
                    }
                    is AlarmUiState.Loading -> saveButton.isEnabled = false
                    else                    -> saveButton.isEnabled = true
                }
            }
        }
    }

    private fun forcePickerBlack(picker: NumberPicker) {
        try {
            val f = NumberPicker::class.java.getDeclaredField("mSelectorWheelPaint")
            f.isAccessible = true
            (f.get(picker) as? android.graphics.Paint)?.color = Color.BLACK
        } catch (_: Exception) {}
        try {
            val f = NumberPicker::class.java.getDeclaredField("mInputText")
            f.isAccessible = true
            (f.get(picker) as? android.widget.EditText)?.setTextColor(Color.BLACK)
        } catch (_: Exception) {}
        for (i in 0 until picker.childCount) {
            val child = picker.getChildAt(i)
            if (child is android.widget.EditText) child.setTextColor(Color.BLACK)
        }
        picker.invalidate()
        picker.post {
            try {
                val f = NumberPicker::class.java.getDeclaredField("mSelectorWheelPaint")
                f.isAccessible = true
                (f.get(picker) as? android.graphics.Paint)?.color = Color.BLACK
            } catch (_: Exception) {}
            for (i in 0 until picker.childCount) {
                val child = picker.getChildAt(i)
                if (child is android.widget.EditText) child.setTextColor(Color.BLACK)
            }
            picker.invalidate()
        }
    }

    private fun updateDayButtons(buttons: List<Button>) {
        buttons.forEachIndexed { index, button ->
            if (selectedDays.contains(index)) {
                button.setBackgroundColor(Color.parseColor("#6B4EFF"))
                button.setTextColor(Color.WHITE)
            } else {
                button.setBackgroundColor(Color.parseColor("#F0F0F0"))
                button.setTextColor(Color.parseColor("#333333"))
            }
        }
    }

    private fun updateDismissCards(shakeCard: LinearLayout, barcodeCard: LinearLayout) {
        shakeCard.setBackgroundResource(
            if (isShakeSelected) R.drawable.dismiss_card_selected else R.drawable.dismiss_card_normal
        )
        barcodeCard.setBackgroundResource(
            if (isShakeSelected) R.drawable.dismiss_card_normal else R.drawable.dismiss_card_selected
        )
    }
}
