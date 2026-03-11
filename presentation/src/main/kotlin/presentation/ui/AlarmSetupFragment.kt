package presentation.ui

import java.time.LocalTime
import presentation.viewmodels.AlarmSetupViewModel
import presentation.viewmodels.AlarmUiState

class AlarmSetupFragment(private val viewModel: AlarmSetupViewModel) {

    fun onSaveClicked() {
        println("UI: Пользователь нажал 'Сохранить'")
        viewModel.save(LocalTime.now(), 10)
    }

    fun observeState(state: AlarmUiState) {
        when (state) {
            is AlarmUiState.Loading -> {
                println("UI: [ПОКАЗЫВАЕМ PROGRESS BAR] Сохранение...")
            }
            is AlarmUiState.Success -> {
                println("UI: [SUCCESS] Будильник установлен! Закрываем экран.")
            }
            is AlarmUiState.Error -> {
                println("UI: [ОШИБКА] Сообщение: ${state.message}")
            }
            is AlarmUiState.Idle -> {
                println("UI: Экран готов к работе")
            }
        }
    }
}
