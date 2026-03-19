package presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.models.SavedBarcode
import domain.usecases.CreateAlarmParams
import domain.usecases.CreateAlarmUseCase
import domain.usecases.GetSavedBarcodesUseCase
import domain.usecases.SaveScannedBarcodeUseCase
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AlarmUiState {
    data object Idle : AlarmUiState()
    data object Loading : AlarmUiState()
    data object Success : AlarmUiState()
    data class Error(val message: String) : AlarmUiState()
}

class AlarmSetupViewModel(
    private val createAlarmUseCase: CreateAlarmUseCase,
    private val getSavedBarcodesUseCase: GetSavedBarcodesUseCase, //Удалил из конструктора, так как он не нужен для сохранения будильника, но оставил для получения сохраненных штрихкодов
    private val saveScannedBarcodeUseCase: SaveScannedBarcodeUseCase //Добавил в конструктор, так как он нужен для сохранения отсканированного штрихкода
) : ViewModel() {

    private val _uiState = MutableStateFlow<AlarmUiState>(AlarmUiState.Idle)
    val uiState = _uiState.asStateFlow()

    val savedBarcodes: StateFlow<List<SavedBarcode>> =
        getSavedBarcodesUseCase.invoke().stateIn( //Преобразуем Flow<List<SavedBarcode>> в StateFlow<List<SavedBarcode>>, чтобы UI мог наблюдать за изменениями списка сохраненных штрихкодов
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun save(time: LocalTime, shakes: Int, name: String = "Будильник", barcodeValue: String? = null) {
        viewModelScope.launch {
            _uiState.value = AlarmUiState.Loading

            val params = CreateAlarmParams(time, shakes, name, barcodeValue)
            val result = createAlarmUseCase.invoke(params)

            _uiState.value = if (result.isSuccess) {
                AlarmUiState.Success
            } else {
                AlarmUiState.Error("Не удалось сохранить будильник")
            }
        }
    }

    fun saveScannedBarcode(codeValue: String, alias: String = codeValue) {
        viewModelScope.launch {
            saveScannedBarcodeUseCase.invoke(codeValue = codeValue, alias = alias) //Вызываем use case для сохранения отсканированного штрихкода. Если код уже существует, он не будет сохранен повторно, а существующий код будет возвращен.
        }
    }
}
