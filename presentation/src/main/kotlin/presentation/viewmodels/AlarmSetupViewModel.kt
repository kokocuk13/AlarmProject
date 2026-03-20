package presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.models.Alarm
import domain.models.SavedBarcode
import domain.usecases.CreateAlarmParams
import domain.usecases.CreateAlarmUseCase
import domain.usecases.GetAlarmsUseCase
import domain.usecases.GetSavedBarcodesUseCase
import domain.usecases.SaveScannedBarcodeUseCase
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AlarmUiState {
    data object Idle    : AlarmUiState()
    data object Loading : AlarmUiState()
    data object Success : AlarmUiState()
    data class  Error(val message: String) : AlarmUiState()
}

class AlarmSetupViewModel(
    private val createAlarmUseCase: CreateAlarmUseCase,
    private val getAlarmsUseCase:   GetAlarmsUseCase,
    private val getSavedBarcodesUseCase: GetSavedBarcodesUseCase,
    private val saveScannedBarcodeUseCase: SaveScannedBarcodeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AlarmUiState>(AlarmUiState.Idle)
    val uiState = _uiState.asStateFlow()

    val savedBarcodes: StateFlow<List<SavedBarcode>> =
        getSavedBarcodesUseCase.invoke().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    /** Загружает один будильник по id для экрана редактирования. */
    suspend fun loadAlarm(id: Long): Alarm? =
        getAlarmsUseCase().first().find { it.id == id }

    fun save(
        time:     LocalTime,
        shakes:   Int,
        name:     String      = "Будильник",
        days:     List<Int>   = emptyList(),
        taskType: String      = "SHAKE",
        barcodeValue: String? = null,
        alarmId:  Long        = 0L
    ) {
        viewModelScope.launch {
            _uiState.value = AlarmUiState.Loading
            val result = createAlarmUseCase.invoke(
                CreateAlarmParams(
                    time           = time,
                    difficultyLevel = shakes,
                    name           = name,
                    days           = days,
                    taskType       = taskType,
                    barcodeValue   = barcodeValue,
                    alarmId        = alarmId
                )
            )
            _uiState.value = if (result.isSuccess) AlarmUiState.Success
                             else AlarmUiState.Error("Не удалось сохранить будильник")
        }
    }

    fun saveScannedBarcode(codeValue: String, alias: String = codeValue) {
        viewModelScope.launch {
            saveScannedBarcodeUseCase.invoke(codeValue = codeValue, alias = alias)
        }
    }
}
