package presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.usecases.CreateAlarmParams
import domain.usecases.CreateAlarmUseCase
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AlarmUiState {
    data object Idle : AlarmUiState()
    data object Loading : AlarmUiState()
    data object Success : AlarmUiState()
    data class Error(val message: String) : AlarmUiState()
}

class AlarmSetupViewModel(private val createAlarmUseCase: CreateAlarmUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow<AlarmUiState>(AlarmUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun save(time: LocalTime, shakes: Int) {
        viewModelScope.launch {
            _uiState.value = AlarmUiState.Loading
            val params = CreateAlarmParams(time, shakes)
            val result = createAlarmUseCase.invoke(params)
            _uiState.value = if (result.isSuccess) {
                AlarmUiState.Success
            } else {
                AlarmUiState.Error("Не удалось сохранить будильник")
            }
        }
    }
}