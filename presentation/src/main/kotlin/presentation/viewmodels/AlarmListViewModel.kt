package presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.models.Alarm
import domain.usecases.DeleteAlarmUseCase
import domain.usecases.GetAlarmsUseCase
import domain.usecases.UpdateAlarmUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана списка будильников.
 * Предоставляет живой список будильников из БД и метод удаления.
 */
class AlarmListViewModel(
    private val getAlarmsUseCase: GetAlarmsUseCase,
    private val deleteAlarmUseCase: DeleteAlarmUseCase,
    private val updateAlarmUseCase: UpdateAlarmUseCase
) : ViewModel() {

    /** Живой StateFlow со списком будильников. Обновляется автоматически при изменениях в БД. */
    val alarms: StateFlow<List<Alarm>> = getAlarmsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    /** Удаляет будильник из БД и отменяет его системное планирование. */
    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            deleteAlarmUseCase(alarm)
        }
    }

    /** Переключает состояние будильника (вкл/выкл) и обновляет системное расписание. */
    fun toggleAlarm(alarm: Alarm, isEnabled: Boolean) {
        if (alarm.isEnabled == isEnabled) return
        viewModelScope.launch {
            updateAlarmUseCase(alarm.copy(isEnabled = isEnabled))
        }
    }
}
