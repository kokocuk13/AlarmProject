package presentation.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import domain.models.BarcodeTask
import domain.models.ShakeTask
import kotlinx.coroutines.launch
import presentation.R
import presentation.di.PresentationDependencies
import presentation.viewmodels.AlarmSetupViewModel
import presentation.viewmodels.AlarmUiState
import java.time.LocalTime

class AlarmSetupFragment : Fragment() {

    private val viewModel: AlarmSetupViewModel by viewModels {
        PresentationDependencies.alarmSetupViewModelFactory
    }

    private var isShakeSelected = true
    private var selectedBarcodeValue: String? = null
    private var selectedMelodyUri: String? = null
    private val selectedDays = mutableSetOf<Int>()
    private var editingAlarmId = -1L
    private var dataLoaded = false
    private var draftHour: Int? = null
    private var draftMinute: Int? = null

    private val melodyPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            selectedMelodyUri = uri?.toString()
            updateMelodyText()
        }
    }

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
        val barcodeControlsContainer = view.findViewById<LinearLayout>(R.id.barcodeControlsContainer)
        val seekBar        = view.findViewById<SeekBar>(R.id.shakeCountSeekBar)
        val nameInput      = view.findViewById<EditText>(R.id.alarmNameInput)
        val saveButton     = view.findViewById<Button>(R.id.saveButton)
        val dayButtons     = listOf<Button>(
            view.findViewById(R.id.dayMon), view.findViewById(R.id.dayTue),
            view.findViewById(R.id.dayWed), view.findViewById(R.id.dayThu),
            view.findViewById(R.id.dayFri), view.findViewById(R.id.daySat),
            view.findViewById(R.id.daySun)
        )

        hourPicker.minValue = 0; hourPicker.maxValue = 23
        minutePicker.minValue = 0; minutePicker.maxValue = 59
        seekBar.min = 1; seekBar.max = 50; seekBar.progress = 20

        hourPicker.setOnValueChangedListener { _, _, newVal -> draftHour = newVal }
        minutePicker.setOnValueChangedListener { _, _, newVal -> draftMinute = newVal }

        if (editingAlarmId != -1L && !dataLoaded) {
            viewLifecycleOwner.lifecycleScope.launch {
                val alarm = viewModel.loadAlarm(editingAlarmId)
                if (alarm != null) {
                    dataLoaded = true
                    hourPicker.value = alarm.time.hour
                    minutePicker.value = alarm.time.minute
                    draftHour = alarm.time.hour
                    draftMinute = alarm.time.minute
                    if (!alarm.name.isNullOrBlank()) nameInput.setText(alarm.name)
                    selectedDays.clear()
                    selectedDays.addAll(alarm.days)
                    selectedMelodyUri = alarm.melodyUri
                    updateMelodyText()
                    val task = alarm.task
                    when (task) {
                        is BarcodeTask -> {
                            isShakeSelected = false
                            selectedBarcodeValue = task.requiredBarcode
                            view.findViewById<TextView>(R.id.selectedBarcodeText).text = selectedBarcodeValue
                        }
                        is ShakeTask -> {
                            isShakeSelected = true
                            seekBar.progress = task.requiredShakes.coerceIn(1, 50)
                        }
                        else -> {}
                    }
                }
                updateDayButtons(dayButtons)
                applyDismissMethodUi(
                    shakeCard = shakeCard,
                    barcodeCard = barcodeCard,
                    shakeContainer = shakeContainer,
                    barcodeControlsContainer = barcodeControlsContainer
                )
                selectedBarcodeValue?.let { code ->
                    view.findViewById<TextView>(R.id.selectedBarcodeText).text = code
                }
            }
        } else if (draftHour != null && draftMinute != null) {
            hourPicker.value = draftHour!!
            minutePicker.value = draftMinute!!
            updateDayButtons(dayButtons)
            applyDismissMethodUi(
                shakeCard = shakeCard,
                barcodeCard = barcodeCard,
                shakeContainer = shakeContainer,
                barcodeControlsContainer = barcodeControlsContainer
            )
            selectedBarcodeValue?.let { code ->
                view.findViewById<TextView>(R.id.selectedBarcodeText).text = code
            }
            updateMelodyText()
        } else if (!dataLoaded) {
            val now = LocalTime.now()
            hourPicker.value = now.hour
            minutePicker.value = now.minute
            draftHour = now.hour
            draftMinute = now.minute
            selectedDays.add(java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) - 1)
            updateDayButtons(dayButtons)
            applyDismissMethodUi(
                shakeCard = shakeCard,
                barcodeCard = barcodeCard,
                shakeContainer = shakeContainer,
                barcodeControlsContainer = barcodeControlsContainer
            )
            // По умолчанию ставим стандартную мелодию будильника
            selectedMelodyUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString()
            updateMelodyText()
        }

        dayButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (selectedDays.contains(index)) selectedDays.remove(index)
                else selectedDays.add(index)
                updateDayButtons(dayButtons)
            }
        }

        val scanBarcodeButton = view.findViewById<Button>(R.id.scanBarcodeButton)
        val selectedBarcodeText = view.findViewById<TextView>(R.id.selectedBarcodeText)

        shakeCard.setOnClickListener {
            isShakeSelected = true
            applyDismissMethodUi(shakeCard, barcodeCard, shakeContainer, barcodeControlsContainer)
        }
        barcodeCard.setOnClickListener {
            isShakeSelected = false
            applyDismissMethodUi(shakeCard, barcodeCard, shakeContainer, barcodeControlsContainer)
        }

        scanBarcodeButton.setOnClickListener {
            showBarcodePickDialog()
        }

        view.findViewById<LinearLayout>(R.id.melodyRow).setOnClickListener {
            pickMelody()
        }

        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<String>(BarcodeScanFragment.RESULT_SCANNED_BARCODE)
            ?.observe(viewLifecycleOwner) { scannedCode ->
                if (scannedCode.isNullOrBlank()) return@observe
                selectedBarcodeValue = scannedCode
                selectedBarcodeText.text = scannedCode
                isShakeSelected = false
                applyDismissMethodUi(shakeCard, barcodeCard, shakeContainer, barcodeControlsContainer)
                viewModel.saveScannedBarcode(scannedCode)
                findNavController().currentBackStackEntry
                    ?.savedStateHandle
                    ?.remove<String>(BarcodeScanFragment.RESULT_SCANNED_BARCODE)
            }

        saveButton.setOnClickListener {
            val name = nameInput.text?.toString()?.trim()?.takeIf { it.isNotBlank() } ?: "Будильник"
            if (!isShakeSelected && selectedBarcodeValue.isNullOrBlank()) {
                Toast.makeText(context, "Сначала выберите или отсканируйте штрих-код", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.save(
                time     = LocalTime.of(hourPicker.value, minutePicker.value),
                shakes   = if (isShakeSelected) seekBar.progress.coerceAtLeast(1) else 0,
                name     = name,
                days     = selectedDays.toList(),
                taskType = if (isShakeSelected) "SHAKE" else "BARCODE",
                barcodeValue = selectedBarcodeValue,
                alarmId  = if (editingAlarmId != -1L) editingAlarmId else 0L,
                melodyUri = selectedMelodyUri
            )
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
                        saveButton.isEnabled = true
                    }
                    is AlarmUiState.Loading -> saveButton.isEnabled = false
                    else -> saveButton.isEnabled = true
                }
            }
        }
    }

    private fun pickMelody() {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Выберите мелодию")
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedMelodyUri?.let { Uri.parse(it) })
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
        }
        melodyPickerLauncher.launch(intent)
    }

    private fun updateMelodyText() {
        val melodyNameView = view?.findViewById<TextView>(R.id.melodyName) ?: return
        if (selectedMelodyUri == null) {
            melodyNameView.text = getString(R.string.melody_default)
        } else {
            val uri = Uri.parse(selectedMelodyUri)
            val ringtone = RingtoneManager.getRingtone(requireContext(), uri)
            melodyNameView.text = ringtone?.getTitle(requireContext()) ?: "Неизвестная мелодия"
        }
    }

    private fun showBarcodePickDialog() {
        val barcodes = viewModel.savedBarcodes.value
        if (barcodes.isEmpty()) {
            findNavController().navigate(R.id.action_alarm_setup_to_barcode)
            return
        }

        val labels = mutableListOf<String>()
        labels += "Сканировать новый"
        labels += barcodes.map { "${it.alias} (${it.codeValue})" }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Штрих-код")
            .setItems(labels.toTypedArray()) { _, which ->
                if (which == 0) {
                    findNavController().navigate(R.id.action_alarm_setup_to_barcode)
                } else {
                    selectedBarcodeValue = barcodes[which - 1].codeValue
                    view?.findViewById<TextView>(R.id.selectedBarcodeText)?.text = selectedBarcodeValue
                }
            }
            .show()
    }

    private fun updateDayButtons(buttons: List<Button>) {
        buttons.forEachIndexed { index, button ->
            if (selectedDays.contains(index)) {
                button.setBackgroundColor("#6B4EFF".toColorInt())
                button.setTextColor(Color.WHITE)
            } else {
                button.setBackgroundColor("#F0F0F0".toColorInt())
                button.setTextColor("#333333".toColorInt())
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


    private fun applyDismissMethodUi(
        shakeCard: LinearLayout,
        barcodeCard: LinearLayout,
        shakeContainer: LinearLayout,
        barcodeControlsContainer: LinearLayout
    ) {
        updateDismissCards(shakeCard, barcodeCard)
        shakeContainer.visibility = if (isShakeSelected) View.VISIBLE else View.GONE
        barcodeControlsContainer.visibility = if (isShakeSelected) View.GONE else View.VISIBLE
    }
}
