package presentation.viewmodels

import domain.usecases.CreateAlarmUseCase
import domain.usecases.CreateAlarmParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.time.LocalTime


sealed class AlarmUiState {
    object Idle : AlarmUiState()
    object Loading : AlarmUiState()
    object Success : AlarmUiState()
    data class Error(val message: String) : AlarmUiState()
}

class AlarmSetupViewModel(private val createAlarmUseCase: CreateAlarmUseCase) {

    private val _uiState = MutableStateFlow<AlarmUiState>(AlarmUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val viewModelScope = CoroutineScope(Dispatchers.Main)


    fun save(time: LocalTime, shakes: Int) {
        viewModelScope.launch {
            _uiState.value = AlarmUiState.Loading

            val params = CreateAlarmParams(time, "SHAKE", shakes)
            val result = createAlarmUseCase.invoke(params)

            _uiState.value = if (result.isSuccess) {
                AlarmUiState.Success
            } else {
                AlarmUiState.Error("Не удалось сохранить будильник")
            }
        }
    }
}
